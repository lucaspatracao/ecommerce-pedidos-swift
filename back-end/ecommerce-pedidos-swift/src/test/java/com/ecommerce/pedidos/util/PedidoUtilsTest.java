package com.ecommerce.pedidos.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.time.Year;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Regras de cálculo da Aula 03 (cenários adaptados da branch feature/crud-completo). */
class PedidoUtilsTest {

    @Test
    @DisplayName("número do pedido segue o formato PED-ano-sequencial")
    void deveGerarNumeroNoFormato() {
        String numero = PedidoUtils.gerarNumeroDoPedido();
        assertTrue(numero.matches("PED-" + Year.now().getValue() + "-\\d{5}"), numero);
    }

    @Test
    @DisplayName("subtotal soma preço x quantidade e ignora entradas inválidas")
    void deveCalcularSubtotal() {
        assertEquals(50.0, PedidoUtils.calcularSubtotal(new double[] {10.0, 5.0, -1.0}, new int[] {4, 2, 3}), 0.001);
        assertEquals(0.0, PedidoUtils.calcularSubtotal(null, new int[] {1}), 0.001);
        assertEquals(0.0, PedidoUtils.calcularSubtotal(new double[] {1.0}, new int[] {1, 2}), 0.001);
    }

    @Test
    @DisplayName("frete: mínimo de R$ 15, R$ 7,50 por quilo e grátis a partir de R$ 300")
    void deveCalcularFrete() {
        assertEquals(15.0, PedidoUtils.calcularFrete(1.0, 50.0), 0.001);
        assertEquals(30.0, PedidoUtils.calcularFrete(3.2, 50.0), 0.001);
        assertEquals(0.0, PedidoUtils.calcularFrete(10.0, 300.0), 0.001);
        assertEquals(0.0, PedidoUtils.calcularFrete(0.0, 50.0), 0.001);
    }

    @Test
    @DisplayName("desconto de 10% limitado a R$ 50")
    void deveCalcularDesconto() {
        assertEquals(10.0, PedidoUtils.calcularDesconto(100.0), 0.001);
        assertEquals(50.0, PedidoUtils.calcularDesconto(1000.0), 0.001);
        assertEquals(0.0, PedidoUtils.calcularDesconto(-5.0), 0.001);
    }

    @Test
    @DisplayName("normaliza nome e monta o recibo com os totais")
    void deveMontarRecibo() {
        assertEquals("NÃO INFORMADO", PedidoUtils.normalizarNome("  "));
        assertEquals("CANETA", PedidoUtils.normalizarNome(" caneta "));
        String recibo = PedidoUtils.montarRecibo(new String[] {"caneta"}, new double[] {2.5}, new int[] {2},
            "PED-1", 5.0, 15.0, 0.5);
        assertTrue(recibo.contains("PED-1"));
        assertTrue(recibo.contains("CANETA"));
        assertTrue(recibo.contains("VALOR TOTAL"));
    }

    @Test
    @DisplayName("classe utilitária não pode ser instanciada")
    void naoDeveInstanciar() throws Exception {
        Constructor<PedidoUtils> construtor = PedidoUtils.class.getDeclaredConstructor();
        construtor.setAccessible(true);
        InvocationTargetException erro = assertThrows(InvocationTargetException.class, construtor::newInstance);
        assertInstanceOf(UnsupportedOperationException.class, erro.getCause());
    }
}
