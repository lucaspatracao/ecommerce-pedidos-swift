package com.ecommerce.pedidos.controller;

import com.ecommerce.pedidos.model.FormaPagamento;
import com.ecommerce.pedidos.service.PagamentoService;
import java.util.List;

public class PagamentoController {
    private final PagamentoService service;

    public PagamentoController() {
        this.service = new PagamentoService();
    }

    public void registrar(FormaPagamento formaPagamento) {
        service.registrar(formaPagamento);
    }

    public List<FormaPagamento> listarPagamentos() {
        return service.listarPagamentos();
    }
}
