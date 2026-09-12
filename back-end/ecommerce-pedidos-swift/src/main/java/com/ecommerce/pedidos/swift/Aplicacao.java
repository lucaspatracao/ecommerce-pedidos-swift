package com.ecommerce.pedidos.swift;

import com.ecommerce.pedidos.model.Boleto;
import com.ecommerce.pedidos.model.CartaoCredito;
import com.ecommerce.pedidos.model.Cliente;
import com.ecommerce.pedidos.model.FormaPagamento;
import com.ecommerce.pedidos.model.Funcionario;
import com.ecommerce.pedidos.model.Pix;
import com.ecommerce.pedidos.model.Produto;
import com.ecommerce.pedidos.service.PagamentoService;
import java.math.BigDecimal;
import java.time.LocalDate;

public class Aplicacao {

    public static void main(String[] args) {
        System.out.println("=== DEMONSTRAÇÃO DE HERANÇA E POLIMORFISMO (AULA 06) ===\n");

        Cliente cliente = new Cliente("Ana Souza", "12345678909", "ana@ecommerce.com", "11987654321", "Rua das Flores, 10");
        System.out.println(cliente.getIdentificacao());

        Produto produto = new Produto("PROD-100", "Notebook", new BigDecimal("3200.00"), 5);
        System.out.println(produto);

        Funcionario funcionario = new Funcionario("Maria Silva", "98765432100", "F-001", "Analista");
        System.out.println(funcionario.getIdentificacao());

        System.out.println("\n=== DEMONSTRAÇÃO DE HERANÇA DE PAGAMENTO ===");
        FormaPagamento[] pagamentos = {
            new Pix(new BigDecimal("150.00"), "cliente@email.com", "EMAIL"),
            new Boleto(new BigDecimal("300.00"), LocalDate.now().plusDays(3), "123456789"),
            new CartaoCredito(new BigDecimal("899.90"), "**** 1234", "Visa", 3)
        };

        for (FormaPagamento pagamento : pagamentos) {
            System.out.println(pagamento.getResumo());
            pagamento.processar();
        }

        PagamentoService service = new PagamentoService();
        service.registrar(new Pix(new BigDecimal("50.00"), "cliente@novo.com", "EMAIL"));
        service.registrar(new Boleto(new BigDecimal("70.00"), LocalDate.now().plusDays(7), "987654321"));
        service.registrar(new CartaoCredito(new BigDecimal("120.00"), "**** 4444", "Mastercard", 2));
        System.out.println("Pagamentos registrados: " + service.listarPagamentos().size());
        service.processarTodos();
    }
}