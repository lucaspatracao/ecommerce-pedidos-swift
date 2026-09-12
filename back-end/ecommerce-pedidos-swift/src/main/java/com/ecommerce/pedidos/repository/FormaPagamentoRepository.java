package com.ecommerce.pedidos.repository;

import com.ecommerce.pedidos.model.FormaPagamento;
import java.util.ArrayList;
import java.util.List;

public class FormaPagamentoRepository {
    private final List<FormaPagamento> pagamentos = new ArrayList<>();

    public void adicionar(FormaPagamento formaPagamento) {
        if (formaPagamento == null) {
            throw new IllegalArgumentException("Forma de pagamento não pode ser nula.");
        }
        pagamentos.add(formaPagamento);
    }

    public List<FormaPagamento> listarTodos() {
        return new ArrayList<>(pagamentos);
    }
}
