package com.ecommerce.pedidos.servico;

import com.ecommerce.pedidos.modelo.Cliente;
import com.ecommerce.pedidos.repositorio.ClienteRepository;
import com.ecommerce.pedidos.repositorio.GerenciadorTransacao;
import com.ecommerce.pedidos.repositorio.Pagina;
import com.ecommerce.pedidos.repositorio.RepositorioException;
import com.ecommerce.pedidos.repositorio.Visibilidade;
import java.sql.Connection;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;

/** Regras de cadastro de clientes, dependentes apenas de interfaces. */
@Service
public class ClienteService {
    private final ClienteRepository clienteRepository;
    private final GerenciadorTransacao transacao;

    public ClienteService(ClienteRepository clienteRepository, GerenciadorTransacao transacao) {
        this.clienteRepository = clienteRepository;
        this.transacao = transacao;
    }

    /** Somente clientes ativos (consulta padrão). */
    public List<Cliente> listarTodos() {
        return listar(Visibilidade.ATIVOS, Pagina.todas());
    }

    public List<Cliente> listar(Visibilidade visibilidade, Pagina pagina) {
        return transacao.executar(conexao -> clienteRepository.listar(conexao, visibilidade, pagina));
    }

    /** Busca um cliente ativo pelo CPF; lança NAO_ENCONTRADO se não existir. */
    public Cliente buscarPorCpf(String cpf) {
        validarCpf(cpf);
        return transacao.executar(conexao -> buscarExistente(conexao, cpf, Visibilidade.ATIVOS));
    }

    public Cliente buscarPorEmail(String email) {
        return transacao.executar(conexao -> clienteRepository.buscarPorEmail(conexao, email)
            .orElseThrow(() -> RepositorioException.naoEncontrado("Cliente", email)));
    }

    public Cliente salvar(Cliente cliente) {
        Objects.requireNonNull(cliente, "Cliente não pode ser nulo.");
        if (cliente.getId() != null) {
            throw new IllegalArgumentException("Novo cliente não deve informar id.");
        }
        return transacao.executarEmTransacao(conexao -> clienteRepository.salvar(conexao, cliente));
    }

    public Cliente atualizar(String cpf, Cliente clienteAtualizado) {
        validarCpf(cpf);
        Objects.requireNonNull(clienteAtualizado, "Cliente atualizado não pode ser nulo.");
        return transacao.executarEmTransacao(conexao -> {
            Cliente existente = buscarExistente(conexao, cpf, Visibilidade.ATIVOS);
            clienteAtualizado.setId(existente.getId());
            clienteAtualizado.setAtivo(existente.isAtivo());
            return clienteRepository.atualizar(conexao, clienteAtualizado);
        });
    }

    /**
     * Exclusão definitiva. Cliente com pedidos é recusado pelo banco
     * (RESTRICT) e vira um erro de negócio (HTTP 409); nesse caso a saída é
     * {@link #inativar(String)}.
     */
    public void excluir(String cpf) {
        validarCpf(cpf);
        transacao.executarEmTransacao(conexao -> {
            clienteRepository.remover(conexao, buscarExistente(conexao, cpf, Visibilidade.TODOS).getId());
            return null;
        });
    }

    public void inativar(String cpf) {
        validarCpf(cpf);
        transacao.executarEmTransacao(conexao -> {
            clienteRepository.inativar(conexao, buscarExistente(conexao, cpf, Visibilidade.ATIVOS).getId());
            return null;
        });
    }

    public Cliente reativar(String cpf) {
        validarCpf(cpf);
        return transacao.executarEmTransacao(conexao -> {
            Cliente inativo = buscarExistente(conexao, cpf, Visibilidade.INATIVOS);
            clienteRepository.reativar(conexao, inativo.getId());
            return buscarExistente(conexao, cpf, Visibilidade.ATIVOS);
        });
    }

    public long contar() {
        return transacao.executar(clienteRepository::contar);
    }

    private Cliente buscarExistente(Connection conexao, String cpf, Visibilidade visibilidade) {
        return clienteRepository.buscarPorCpf(conexao, cpf, visibilidade)
            .orElseThrow(() -> RepositorioException.naoEncontrado("Cliente", cpf));
    }

    private void validarCpf(String cpf) {
        if (cpf == null || cpf.isBlank()) {
            throw new IllegalArgumentException("CPF do cliente não pode ser vazio.");
        }
    }
}
