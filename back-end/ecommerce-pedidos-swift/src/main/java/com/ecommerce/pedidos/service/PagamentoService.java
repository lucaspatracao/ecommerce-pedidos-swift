package com.ecommerce.pedidos.service;

import com.ecommerce.pedidos.model.FormaPagamento;
import com.ecommerce.pedidos.repository.FormaPagamentoRepository;
import java.util.List;

public class PagamentoService {
    private final FormaPagamentoRepository repository;

    public PagamentoService() {
        this.repository = new FormaPagamentoRepository();
    }

    public void registrar(FormaPagamento formaPagamento) {
        repository.adicionar(formaPagamento);
    }

    public List<FormaPagamento> listarPagamentos() {
        return repository.listarTodos();
    }

    public boolean processarTodos() {
        boolean sucesso = true;
        for (FormaPagamento formaPagamento : repository.listarTodos()) {
            sucesso = formaPagamento.processar() && sucesso;
        }
        return sucesso;
    }
}
