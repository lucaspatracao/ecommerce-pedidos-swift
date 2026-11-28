package com.ecommerce.pedidos.ci;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * PROVA DO PIPELINE (Aula 15). Este teste falha DE PROPÓSITO.
 *
 * <p>Existe só na branch {@code test/ci-vermelho}, para mostrar que o GitHub
 * Actions reprova o PR quando um teste quebra. NÃO INTEGRAR na main.</p>
 */
class ProvaPipelineTest {

    @Test
    @DisplayName("falha de propósito: prova de que o pipeline fica vermelho")
    void deveFalharDePropositoParaProvarOPipeline() {
        // Prova do pipeline: 1 nunca é igual a 2, então "mvn verify" falha e o CI fica vermelho.
        assertEquals(1, 2);
    }
}
