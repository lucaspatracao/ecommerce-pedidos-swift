package com.ecommerce.pedidos.service;

import com.ecommerce.pedidos.model.FormaPagamento;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PagamentoService {

    private final List<FormaPagamento> pagamentos = new ArrayList<>();

    public void registrar(FormaPagamento pagamento) {
        if (pagamento == null) {
            throw new IllegalArgumentException("Pagamento não pode ser nulo");
        }
        pagamentos.add(pagamento);
    }

    public List<FormaPagamento> listarPagamentos() {
        return Collections.unmodifiableList(pagamentos);
    }

    public void processarTodos() {
        for (FormaPagamento p : pagamentos) {
            p.processar();
        }
    }
}