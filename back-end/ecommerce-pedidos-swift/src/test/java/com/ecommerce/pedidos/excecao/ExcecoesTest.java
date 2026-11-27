package com.ecommerce.pedidos.excecao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.ecommerce.pedidos.modelo.Produto;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** As exceções de domínio carregam os dados de que quem as trata precisa. */
class ExcecoesTest {

    @Test
    @DisplayName("estoque insuficiente expõe produto e quantidade solicitada")
    void estoqueInsuficiente() {
        Produto produto = new Produto("PRD-EXC", "Mochila", new BigDecimal("90.00"), 2);
        EstoqueInsuficienteException erro = new EstoqueInsuficienteException(produto, 5);

        assertSame(produto, erro.getProduto());
        assertEquals(5, erro.getQuantidadeSolicitada());
        assertEquals("Estoque insuficiente de Mochila: disponível 2, solicitado 5", erro.getMessage());
    }

    @Test
    @DisplayName("pagamento recusado expõe forma e motivo")
    void pagamentoRecusado() {
        PagamentoRecusadoException erro = new PagamentoRecusadoException("Pix", "chave inválida");

        assertEquals("Pix", erro.getFormaPagamento());
        assertEquals("chave inválida", erro.getMotivo());
        assertEquals("Pagamento por Pix recusado: chave inválida", erro.getMessage());
    }

    @Test
    @DisplayName("cliente não encontrado expõe o identificador buscado")
    void clienteNaoEncontrado() {
        ClienteNaoEncontradoException erro = new ClienteNaoEncontradoException("123");

        assertEquals("123", erro.getIdentificador());
        assertEquals("Cliente não encontrado: 123", erro.getMessage());
    }

    @Test
    @DisplayName("exceção raiz preserva a causa")
    void raizPreservaCausa() {
        IllegalStateException causa = new IllegalStateException("origem");
        ECommerceException erro = new ECommerceException("falhou", causa);

        assertSame(causa, erro.getCause());
        assertEquals("falhou", new PedidoInvalidoException("falhou").getMessage());
    }
}
