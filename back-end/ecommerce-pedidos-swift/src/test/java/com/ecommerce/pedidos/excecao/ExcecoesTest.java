package com.ecommerce.pedidos.excecao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ecommerce.pedidos.modelo.Produto;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Hierarquia e dados das exceções de negócio (Aula 09). Sem Spring. */
class ExcecoesTest {

    @Test
    @DisplayName("estoque insuficiente expõe código, quantidade solicitada e disponível")
    void deveExporDadosDeEstoque() {
        Produto produto = new Produto("P-300", "Teclado", new BigDecimal("100.00"), 2);

        EstoqueInsuficienteException erro = assertThrows(EstoqueInsuficienteException.class,
            () -> produto.baixarEstoque(5));

        assertEquals("P-300", erro.getCodigoProduto());
        assertEquals(5, erro.getQuantidadeSolicitada());
        assertEquals(2, erro.getQuantidadeDisponivel());
        assertTrue(erro.getMessage().contains("disponível 2, solicitado 5"));
    }

    @Test
    @DisplayName("toda exceção de negócio é unchecked e herda de NegocioException")
    void deveSeguirHierarquia() {
        assertInstanceOf(RuntimeException.class, new NegocioException("x"));
        assertInstanceOf(ConflitoException.class, new PedidoInvalidoException("x"));
        assertInstanceOf(ConflitoException.class, new PagamentoRecusadoException("Pix", "x"));
        assertInstanceOf(RecursoNaoEncontradoException.class, new ClienteNaoEncontradoException("1"));
        assertInstanceOf(NegocioException.class, new ConflitoException("x"));
    }

    @Test
    @DisplayName("pagamento recusado expõe forma e motivo; não encontrado expõe recurso e identificador")
    void deveExporDetalhes() {
        PagamentoRecusadoException recusado = new PagamentoRecusadoException("Pix", "sem saldo");
        assertEquals("Pix", recusado.getFormaPagamento());
        assertEquals("sem saldo", recusado.getMotivo());
        assertTrue(recusado.getMessage().contains("recusado"));

        ClienteNaoEncontradoException naoEncontrado = new ClienteNaoEncontradoException("89910440261");
        assertEquals("Cliente", naoEncontrado.getRecurso());
        assertEquals("89910440261", naoEncontrado.getIdentificador());
        assertEquals("Cliente não encontrado: 89910440261", naoEncontrado.getMessage());
    }

    @Test
    @DisplayName("exceções preservam a causa original")
    void devePreservarCausa() {
        Throwable causa = new IllegalStateException("raiz");
        assertSame(causa, new NegocioException("x", causa).getCause());
        assertSame(causa, new ConflitoException("x", causa).getCause());
    }
}
