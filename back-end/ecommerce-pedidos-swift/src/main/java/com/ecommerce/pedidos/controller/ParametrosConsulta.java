package com.ecommerce.pedidos.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/** Converte os parâmetros de paginação comuns às listagens da API. */
final class ParametrosConsulta {
    static final int TAMANHO_PADRAO = 20;
    static final int TAMANHO_MAXIMO = 500;

    private ParametrosConsulta() {
    }

    /**
     * Sem {@code pagina} nem {@code tamanho}: devolve tudo (o front atual não
     * pagina). Com um deles, a página começa em 0 e tem 20 itens por padrão.
     */
    static Pageable pagina(Integer pagina, Integer tamanho) {
        if (pagina == null && tamanho == null) {
            return Pageable.unpaged();
        }
        int numero = pagina == null ? 0 : pagina;
        int itens = tamanho == null ? TAMANHO_PADRAO : tamanho;
        if (numero < 0) {
            throw new IllegalArgumentException("Número da página não pode ser negativo.");
        }
        if (itens <= 0) {
            throw new IllegalArgumentException("Tamanho da página deve ser maior que zero.");
        }
        if (itens > TAMANHO_MAXIMO) {
            throw new IllegalArgumentException("Tamanho da página deve ser no máximo " + TAMANHO_MAXIMO + ".");
        }
        return PageRequest.of(numero, itens);
    }
}
