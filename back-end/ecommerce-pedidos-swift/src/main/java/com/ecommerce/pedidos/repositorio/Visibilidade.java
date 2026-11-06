package com.ecommerce.pedidos.repositorio;

/**
 * Filtro de soft delete. As consultas padrão usam {@link #ATIVOS}; buscar
 * inativos exige pedir explicitamente {@link #INATIVOS} ou {@link #TODOS}.
 */
public enum Visibilidade {
    ATIVOS("ativo = TRUE"),
    INATIVOS("ativo = FALSE"),
    TODOS(null);

    private final String condicao;

    Visibilidade(String condicao) {
        this.condicao = condicao;
    }

    /**
     * Condição SQL fixa (nunca vem de entrada do usuário) sobre a coluna
     * {@code ativo} do alias informado.
     */
    public String condicaoPara(String alias) {
        return condicao == null ? "1 = 1" : alias + "." + condicao;
    }
}
