package com.ecommerce.pedidos.apoio;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Gera dados únicos para os testes de integração.
 *
 * <p>O H2 de teste é um só para todas as classes da mesma execução, então
 * cada teste cria seus próprios clientes/produtos com CPF e código novos,
 * sem depender da ordem dos testes nem apagar dados dos outros.</p>
 *
 * <p>Os CPFs são SINTÉTICOS: 9 dígitos de um contador + 2 dígitos
 * verificadores calculados. Nenhum CPF real é usado.</p>
 */
public final class DadosDeTeste {

    private static final AtomicInteger SEQUENCIA = new AtomicInteger(100_000);

    private DadosDeTeste() {
    }

    /** CPF válido e diferente a cada chamada (base "900" + sequência). */
    public static String novoCpf() {
        String base = String.format("900%06d", SEQUENCIA.incrementAndGet());
        int d1 = digito(base, 9);
        int d2 = digito(base + d1, 10);
        return base + d1 + d2;
    }

    /** Código de produto único, com prefixo legível no log. */
    public static String novoCodigo(String prefixo) {
        return prefixo + "-" + SEQUENCIA.incrementAndGet();
    }

    private static int digito(String digitos, int quantidade) {
        int soma = 0;
        for (int i = 0; i < quantidade; i++) {
            soma += (digitos.charAt(i) - '0') * (quantidade + 1 - i);
        }
        int resto = (soma * 10) % 11;
        return resto == 10 ? 0 : resto;
    }
}
