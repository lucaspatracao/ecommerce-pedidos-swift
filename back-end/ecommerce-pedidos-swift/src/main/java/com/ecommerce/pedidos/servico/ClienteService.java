package com.ecommerce.pedidos.servico;

import com.ecommerce.pedidos.excecao.ClienteNaoEncontradoException;
import com.ecommerce.pedidos.excecao.ConflitoException;
import com.ecommerce.pedidos.modelo.Cliente;
import com.ecommerce.pedidos.util.Utils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class ClienteService {
    private final Map<String, Cliente> clientes = new ConcurrentHashMap<>();

    public List<Cliente> listarTodos() {
        return new ArrayList<>(clientes.values());
    }

    /** @throws ClienteNaoEncontradoException (404) se o CPF não estiver cadastrado */
    public Cliente buscarPorCpf(String cpf) {
        String chave = Utils.textoObrigatorio(cpf, "CPF do cliente");
        Cliente cliente = clientes.get(chave);
        if (cliente == null) {
            throw new ClienteNaoEncontradoException(chave);
        }
        return cliente;
    }

    /** @throws ConflitoException (409) se já existir cliente com o mesmo CPF */
    public Cliente salvar(Cliente cliente) {
        Objects.requireNonNull(cliente, "Cliente não pode ser nulo.");
        if (clientes.putIfAbsent(cliente.getCpf(), cliente) != null) {
            throw new ConflitoException("Já existe cliente cadastrado com o CPF " + cliente.getCpf());
        }
        return cliente;
    }

    public Cliente atualizar(String cpf, Cliente clienteAtualizado) {
        Objects.requireNonNull(clienteAtualizado, "Cliente atualizado não pode ser nulo.");
        Cliente existente = buscarPorCpf(cpf);
        existente.atualizarContato(clienteAtualizado.getNome(), clienteAtualizado.getEmail(),
            clienteAtualizado.getTelefone(), clienteAtualizado.getEndereco());
        return existente;
    }

    public void excluir(String cpf) {
        Cliente existente = buscarPorCpf(cpf);
        clientes.remove(existente.getCpf());
    }

    public long contar() {
        return clientes.size();
    }
}
