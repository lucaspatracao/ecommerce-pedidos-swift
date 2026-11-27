package com.ecommerce.pedidos.controller;

import com.ecommerce.pedidos.repositorio.Pagina;
import com.ecommerce.pedidos.repositorio.Visibilidade;

/** Conversão dos parâmetros de query string comuns às listagens. */
final class ParametrosConsulta {
    static final int TAMANHO_PADRAO = 20;

    private ParametrosConsulta() {
    }

    /** Sem {@code pagina} nem {@code tamanho}: devolve tudo (compatível com o front atual). */
    static Pagina pagina(Integer pagina, Integer tamanho) {
        if (pagina == null && tamanho == null) {
            return Pagina.todas();
        }
        return Pagina.de(pagina == null ? 0 : pagina, tamanho == null ? TAMANHO_PADRAO : tamanho);
    }

    /** {@code situacao=ativos|inativos|todos}; padrão: ativos. */
    static Visibilidade visibilidade(String situacao) {
        if (situacao == null || situacao.isBlank()) {
            return Visibilidade.ATIVOS;
        }
        return switch (situacao.trim().toLowerCase()) {
            case "ativos" -> Visibilidade.ATIVOS;
            case "inativos" -> Visibilidade.INATIVOS;
            case "todos" -> Visibilidade.TODOS;
            default -> throw new IllegalArgumentException(
                "Parâmetro situacao inválido. Use ativos, inativos ou todos.");
        };
    }
}
