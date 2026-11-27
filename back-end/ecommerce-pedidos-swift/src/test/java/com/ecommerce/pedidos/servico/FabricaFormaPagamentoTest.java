package com.ecommerce.pedidos.servico;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.ecommerce.pedidos.dto.PagamentoRequest;
import com.ecommerce.pedidos.modelo.Boleto;
import com.ecommerce.pedidos.modelo.CartaoCredito;
import com.ecommerce.pedidos.modelo.Pix;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** A fábrica é uma classe comum: dá para testar com {@code new}, sem subir o Spring. */
class FabricaFormaPagamentoTest {

    private static final BigDecimal VALOR = new BigDecimal("50.00");
    private final FabricaFormaPagamento fabrica = new FabricaFormaPagamento();

    @Test
    @DisplayName("cria a subclasse certa para cada tipo, sem diferenciar maiúsculas")
    void deveCriarCadaTipo() {
        assertInstanceOf(CartaoCredito.class, fabrica.criar(
            new PagamentoRequest("cartao", VALOR, "**** 1", "Visa", 2, null, null, null, null, "PED-1")));
        assertInstanceOf(Boleto.class, fabrica.criar(
            new PagamentoRequest("BOLETO", VALOR, null, null, null, LocalDate.now().plusDays(1), "123", null, null, "PED-1")));
        assertInstanceOf(Pix.class, fabrica.criar(
            new PagamentoRequest(" PIX ", VALOR, null, null, null, null, null, "chave", "EMAIL", "PED-1")));
    }

    @Test
    @DisplayName("tipo ausente ou desconhecido vira IllegalArgumentException (400)")
    void deveRecusarTipoInvalido() {
        assertThrows(IllegalArgumentException.class, () -> fabrica.criar(null));
        assertThrows(IllegalArgumentException.class, () -> fabrica.criar(
            new PagamentoRequest(" ", VALOR, null, null, null, null, null, null, null, "PED-1")));
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class, () -> fabrica.criar(
            new PagamentoRequest("CHEQUE", VALOR, null, null, null, null, null, null, null, "PED-1")));
        assertEquals("Tipo de pagamento inválido: CHEQUE. Use CARTAO, BOLETO ou PIX.", erro.getMessage());
    }

    @Test
    @DisplayName("cartão sem parcelas é recusado com mensagem clara")
    void deveExigirParcelasNoCartao() {
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class, () -> fabrica.criar(
            new PagamentoRequest("CARTAO", VALOR, "**** 1", "Visa", null, null, null, null, null, "PED-1")));
        assertEquals("Quantidade de parcelas é obrigatória para cartão.", erro.getMessage());
    }
}
