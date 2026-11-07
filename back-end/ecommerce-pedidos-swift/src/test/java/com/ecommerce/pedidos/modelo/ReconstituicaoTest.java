package com.ecommerce.pedidos.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Recriação de objetos lidos do banco (id, versão, preço histórico). */
class ReconstituicaoTest {
    private final Cliente cliente = new Cliente("Joana", "12312312312", "joana@email.com", "11955554444", "Rua B");

    @Test
    @DisplayName("item reconstituído mantém o preço da venda, não o preço atual do produto")
    void itemMantemPrecoHistorico() {
        Produto produto = new Produto("PRD-HIS", "Caderno", new BigDecimal("25.00"), 3);
        ItemPedido item = ItemPedido.reconstituir(7L, produto, 2, new BigDecimal("19.90"));

        assertEquals(7L, item.getId());
        assertEquals(new BigDecimal("19.90"), item.getPrecoPraticado());
        assertEquals(new BigDecimal("39.80"), item.calcularSubtotal());
        assertThrows(IllegalArgumentException.class, () -> ItemPedido.reconstituir(1L, produto, 1, BigDecimal.ZERO));
    }

    @Test
    @DisplayName("pedido reconstituído preserva número, data e situação sem gerar novo número")
    void pedidoPreservaDados() {
        int criadosAntes = Pedido.getTotalDePedidosCriados();
        Produto produto = new Produto("PRD-REC", "Caneta", new BigDecimal("5.00"), 10);
        ItemPedido item = ItemPedido.reconstituir(1L, produto, 2, new BigDecimal("5.00"));

        Pedido pedido = Pedido.reconstituir(3L, "PED-2026-00001", cliente, LocalDate.of(2026, 1, 10),
            SituacaoPedido.PAGO, List.of(item));

        assertEquals(3L, pedido.getId());
        assertEquals("PED-2026-00001", pedido.getNumero());
        assertEquals(SituacaoPedido.PAGO, pedido.getSituacao());
        assertEquals(new BigDecimal("10.00"), pedido.calcularValorTotal());
        assertEquals(10, produto.getQuantidadeEmEstoque());
        assertEquals(criadosAntes, Pedido.getTotalDePedidosCriados());
        assertTrue(pedido.buscarItemPorId(1L).isPresent());
        assertFalse(pedido.buscarItemPorId(99L).isPresent());
        assertThrows(IllegalArgumentException.class,
            () -> Pedido.reconstituir(1L, " ", cliente, LocalDate.now(), SituacaoPedido.ABERTO, null));
        assertThrows(IllegalArgumentException.class,
            () -> Pedido.reconstituir(1L, "PED-1", null, LocalDate.now(), SituacaoPedido.ABERTO, null));
        assertThrows(IllegalArgumentException.class,
            () -> Pedido.reconstituir(1L, "PED-1", cliente, null, SituacaoPedido.ABERTO, null));
    }

    @Test
    @DisplayName("id, versão e ativo das entidades")
    void identidadeEVersao() {
        Produto produto = new Produto("PRD-VER", "Lápis", new BigDecimal("2.00"), 1);
        produto.setId(5L);
        produto.setVersao(3);
        assertEquals(5L, produto.getId());
        assertEquals(3, produto.getVersao());
        assertThrows(IllegalArgumentException.class, () -> produto.setVersao(-1));

        assertTrue(cliente.isAtivo());
        cliente.setId(9L);
        cliente.setAtivo(false);
        assertEquals(9L, cliente.getId());
        assertFalse(cliente.isAtivo());
    }
}
