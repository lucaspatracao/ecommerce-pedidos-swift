package com.ecommerce.pedidos.swift.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.time.Year;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/** Regras de frete e desconto da Aula 03. */
class PedidoUtilsTest {

    @Test
    @DisplayName("número do pedido segue o formato PED-ano-sequencial")
    void numeroDoPedido() {
        String numero = PedidoUtils.gerarNumeroDoPedido();
        assertTrue(numero.matches("PED-" + Year.now().getValue() + "-\\d{5}"), numero);
    }

    @Test
    @DisplayName("subtotal soma preço x quantidade e ignora entradas inválidas")
    void subtotal() {
        assertEquals(25.0, PedidoUtils.calcularSubtotal(new double[] {10.0, 2.5}, new int[] {2, 2}), 0.001);
        assertEquals(10.0, PedidoUtils.calcularSubtotal(new double[] {10.0, -1}, new int[] {1, 3}), 0.001);
        assertEquals(0.0, PedidoUtils.calcularSubtotal(null, new int[] {1}), 0.001);
        assertEquals(0.0, PedidoUtils.calcularSubtotal(new double[] {1}, new int[] {1, 2}), 0.001);
    }

    @ParameterizedTest(name = "peso {0} kg, subtotal {1} → frete {2}")
    @CsvSource({
        "0,    100, 0",
        "1,    100, 15.0",
        "2.1,  100, 22.5",
        "10,   300, 0",
    })
    @DisplayName("frete: mínimo de R$ 15, R$ 7,50 por quilo e grátis a partir de R$ 300")
    void frete(double peso, double subtotal, double esperado) {
        assertEquals(esperado, PedidoUtils.calcularFrete(peso, subtotal), 0.001);
    }

    @ParameterizedTest(name = "subtotal {0} → desconto {1}")
    @CsvSource({"0, 0", "100, 10", "800, 50"})
    @DisplayName("desconto de 10% limitado a R$ 50")
    void desconto(double subtotal, double esperado) {
        assertEquals(esperado, PedidoUtils.calcularDesconto(subtotal), 0.001);
    }

    @Test
    @DisplayName("normaliza nome e monta o recibo com os totais")
    void recibo() {
        assertEquals("NÃO INFORMADO", PedidoUtils.normalizarNome(" "));
        assertEquals("CANETA", PedidoUtils.normalizarNome(" caneta "));

        String recibo = PedidoUtils.montarRecibo(new String[] {"caneta"}, new double[] {2.0}, new int[] {3},
            "PED-1", 6.0, 15.0, 0.6);
        assertTrue(recibo.contains("Número do Pedido: PED-1"));
        assertTrue(recibo.contains("CANETA"));
        assertTrue(recibo.contains("VALOR TOTAL:"));
        assertTrue(PedidoUtils.montarRecibo(null, null, null, "PED-2", 0, 0, 0).contains("PED-2"));
    }

    @Test
    @DisplayName("classe utilitária não pode ser instanciada")
    void naoInstanciavel() throws Exception {
        Constructor<PedidoUtils> construtor = PedidoUtils.class.getDeclaredConstructor();
        construtor.setAccessible(true);
        InvocationTargetException erro = assertThrows(InvocationTargetException.class, construtor::newInstance);
        assertTrue(erro.getCause() instanceof UnsupportedOperationException);
    }
}
