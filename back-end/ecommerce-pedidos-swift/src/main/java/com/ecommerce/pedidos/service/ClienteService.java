package com.ecommerce.pedidos.service;

import com.ecommerce.pedidos.model.Cliente;
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

    public Cliente buscarPorCpf(String cpf) {
        if (cpf == null || cpf.isBlank()) {
            throw new IllegalArgumentException("CPF do cliente não pode ser vazio.");
        }
        return clientes.get(cpf.trim());
    }

    public Cliente salvar(Cliente cliente) {
        Objects.requireNonNull(cliente, "Cliente não pode ser nulo.");
        String cpf = cliente.getCpf();
        clientes.put(cpf, cliente);
        return cliente;
    }

    public Cliente atualizar(String cpf, Cliente clienteAtualizado) {
        if (cpf == null || cpf.isBlank()) {
            throw new IllegalArgumentException("CPF do cliente não pode ser vazio.");
        }
        Objects.requireNonNull(clienteAtualizado, "Cliente atualizado não pode ser nulo.");

        Cliente clienteExistente = clientes.get(cpf.trim());
        if (clienteExistente == null) {
            throw new IllegalArgumentException("Cliente não encontrado.");
        }

        String novoCpf = clienteAtualizado.getCpf();
        if (!cpf.equalsIgnoreCase(novoCpf)) {
            clientes.remove(cpf.trim());
        }

        clientes.put(novoCpf, clienteAtualizado);
        return clienteAtualizado;
    }

    public boolean excluir(String cpf) {
        if (cpf == null || cpf.isBlank()) {
            throw new IllegalArgumentException("CPF do cliente não pode ser vazio.");
        }
        return clientes.remove(cpf.trim()) != null;
    }

    public long contar() {
        return clientes.size();
    }
}
