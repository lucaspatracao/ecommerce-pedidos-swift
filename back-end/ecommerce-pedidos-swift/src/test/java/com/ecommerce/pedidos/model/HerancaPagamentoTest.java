package com.ecommerce.pedidos.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class HerancaPagamentoTest {

    @Test
    void deveCriarFuncionarioEFormasDePagamentoComPolimorfismo() {
        Funcionario funcionario = new Funcionario("João", "11122233344", "M-101", "Supervisor");
        assertEquals("João - matrícula M-101", funcionario.getIdentificacao());

        FormaPagamento pix = new Pix(new BigDecimal("150.00"), "cliente@email.com", "CPF");
        FormaPagamento boleto = new Boleto(new BigDecimal("300.00"), LocalDate.now().plusDays(3), "123456789");
        FormaPagamento cartao = new CartaoCredito(new BigDecimal("899.90"), "**** 1234", "Visa", 3);

        assertTrue(pix.processar());
        assertTrue(boleto.processar());
        assertTrue(cartao.processar());

        assertTrue(pix.getResumo().contains("Pix"));
        assertTrue(boleto.getResumo().contains("Boleto"));
        assertTrue(cartao.getResumo().contains("CartaoCredito"));
    }
}
