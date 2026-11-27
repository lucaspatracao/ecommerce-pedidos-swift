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
    public List<ClienteResponse> listarTodos() {
        return clienteRepository.findAll().stream().map(ClienteResponse::de).toList();
    }

    @Override
    public ClienteResponse buscarPorCpf(String cpf) {
        return ClienteResponse.de(buscarEntidade(cpf));
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
        return ClienteResponse.de(clienteRepository.save(cliente));
    }

    @Override
    @Transactional
    public ClienteResponse atualizar(String cpf, ClienteRequest request) {
        Cliente cliente = buscarEntidade(cpf);
        // O CPF da rota identifica o cliente; o CPF do corpo é ignorado (CPF não muda).
        cliente.atualizarContato(request.nome(), request.email(), request.telefone(),
            Endereco.aPartirDoTexto(request.endereco()));
        return ClienteResponse.de(cliente);
    }

    @Override
    @Transactional
    public void excluir(String cpf) {
        Cliente cliente = buscarEntidade(cpf);
        if (pedidoRepository.existsByCliente(cliente)) {
            throw new ConflitoException("Cliente possui pedidos e não pode ser excluído.");
        }
        clienteRepository.delete(cliente);
    }

    @Override
    public long contar() {
        return clienteRepository.count();
    }

    private Cliente buscarEntidade(String cpf) {
        // Aceita CPF com ou sem máscara; no banco ele fica só com dígitos.
        String chave = Utils.apenasDigitos(Utils.textoObrigatorio(cpf, "CPF do cliente"));
        return clienteRepository.findByDocumento(chave)
            .orElseThrow(() -> new ClienteNaoEncontradoException(chave));
    }
}
