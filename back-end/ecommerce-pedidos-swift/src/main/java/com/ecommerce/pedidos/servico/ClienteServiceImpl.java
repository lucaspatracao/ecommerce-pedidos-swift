package com.ecommerce.pedidos.servico;

import com.ecommerce.pedidos.dto.ClienteRequest;
import com.ecommerce.pedidos.dto.ClienteResponse;
import com.ecommerce.pedidos.excecao.ClienteNaoEncontradoException;
import com.ecommerce.pedidos.excecao.ConflitoException;
import com.ecommerce.pedidos.modelo.Cliente;
import com.ecommerce.pedidos.modelo.Endereco;
import com.ecommerce.pedidos.repositorio.ClienteRepository;
import com.ecommerce.pedidos.repositorio.PedidoRepository;
import com.ecommerce.pedidos.util.Utils;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementação dos casos de uso de cliente.
 *
 * <p>{@code @Transactional(readOnly = true)} na classe vale para todos os
 * métodos de leitura; os que gravam sobrescrevem com {@code @Transactional}.
 * Dentro de uma transação, alterar um objeto carregado do banco já basta:
 * o Hibernate detecta a mudança e grava no commit ("dirty checking").</p>
 */
@Service
@Transactional(readOnly = true)
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;
    private final PedidoRepository pedidoRepository;

    public ClienteServiceImpl(ClienteRepository clienteRepository, PedidoRepository pedidoRepository) {
        this.clienteRepository = clienteRepository;
        this.pedidoRepository = pedidoRepository;
    }

    @Override
    public List<ClienteResponse> listar(Visibilidade visibilidade, Pageable pagina) {
        return clienteRepository.listar(visibilidade.filtroAtivo(), pagina).stream()
            .map(ClienteResponse::de)
            .toList();
    }

    @Override
    public ClienteResponse buscarPorCpf(String cpf) {
        return ClienteResponse.de(buscarEntidade(cpf, Visibilidade.ATIVOS));
    }

    @Override
    @Transactional
    public ClienteResponse cadastrar(ClienteRequest request) {
        // O construtor valida tudo (CPF, e-mail, telefone) antes de ir ao banco.
        Cliente cliente = new Cliente(request.nome(), request.cpf(), request.email(),
            request.telefone(), request.endereco());
        if (clienteRepository.existsByDocumento(cliente.getCpf())) {
            throw new ConflitoException("Já existe cliente cadastrado com o CPF " + cliente.getCpf());
        }
        if (clienteRepository.existsByEmail(cliente.getEmail())) {
            throw new ConflitoException("Já existe cliente cadastrado com o e-mail " + cliente.getEmail());
        }
        return ClienteResponse.de(clienteRepository.save(cliente));
    }

    @Override
    @Transactional
    public ClienteResponse atualizar(String cpf, ClienteRequest request) {
        Cliente cliente = buscarEntidade(cpf, Visibilidade.ATIVOS);
        // Confere o e-mail ANTES de alterar a entidade: a consulta faria o
        // Hibernate gravar a alteração pendente e o banco recusaria o UPDATE.
        String email = Utils.validarEmail(request.email());
        if (clienteRepository.existsByEmailAndIdNot(email, cliente.getId())) {
            throw new ConflitoException("Já existe cliente cadastrado com o e-mail " + email);
        }
        // O CPF da rota identifica o cliente; o CPF do corpo é ignorado (CPF não muda).
        cliente.atualizarContato(request.nome(), email, request.telefone(),
            Endereco.aPartirDoTexto(request.endereco()));
        return ClienteResponse.de(cliente);
    }

    @Override
    @Transactional
    public void excluir(String cpf) {
        Cliente cliente = buscarEntidade(cpf, Visibilidade.TODOS);
        if (pedidoRepository.existsByCliente(cliente)) {
            throw new ConflitoException("Este cliente possui pedidos e não pode ser excluído. "
                + "Inative-o para tirá-lo das listagens sem perder o histórico.");
        }
        clienteRepository.delete(cliente);
    }

    @Override
    @Transactional
    public void inativar(String cpf) {
        buscarEntidade(cpf, Visibilidade.ATIVOS).inativar();
    }

    @Override
    @Transactional
    public ClienteResponse reativar(String cpf) {
        Cliente cliente = buscarEntidade(cpf, Visibilidade.INATIVOS);
        cliente.reativar();
        return ClienteResponse.de(cliente);
    }

    @Override
    @Transactional
    public ClienteResponse alterarAtivo(String cpf, boolean ativo) {
        if (ativo) {
            return reativar(cpf);
        }
        inativar(cpf);
        return ClienteResponse.de(buscarEntidade(cpf, Visibilidade.TODOS));
    }

    @Override
    public long contar() {
        return clienteRepository.countByAtivoTrue();
    }

    /** Busca pelo CPF (com ou sem máscara) respeitando o filtro do soft delete. */
    private Cliente buscarEntidade(String cpf, Visibilidade visibilidade) {
        String chave = Utils.apenasDigitos(Utils.textoObrigatorio(cpf, "CPF do cliente"));
        Boolean ativo = visibilidade.filtroAtivo();
        return clienteRepository.buscarPorCpf(chave)
            .filter(cliente -> ativo == null || cliente.isAtivo() == ativo)
            .orElseThrow(() -> new ClienteNaoEncontradoException(chave));
    }
}
