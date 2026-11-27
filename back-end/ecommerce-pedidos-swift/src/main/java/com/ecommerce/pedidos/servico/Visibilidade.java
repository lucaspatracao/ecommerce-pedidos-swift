package com.ecommerce.pedidos.servico;

/**
 * Filtro do soft delete nas listagens: só ativos (padrão), só inativos ou todos.
 *
 * <p>Vem do parâmetro {@code situacao=ativos|inativos|todos} da API.</p>
 */
public enum Visibilidade {
    ATIVOS(Boolean.TRUE),
    INATIVOS(Boolean.FALSE),
    TODOS(null);

    private final Boolean ativo;

    Visibilidade(Boolean ativo) {
        this.ativo = ativo;
    }

    /** Valor da coluna {@code ativo} a filtrar, ou {@code null} para não filtrar. */
    public Boolean filtroAtivo() {
        return ativo;
    }

    /** Converte o texto da API; vazio ou ausente vira {@link #ATIVOS}. */
    public static Visibilidade deTexto(String situacao) {
        if (situacao == null || situacao.isBlank()) {
            return ATIVOS;
        }
        return switch (situacao.trim().toLowerCase()) {
            case "ativos" -> ATIVOS;
            case "inativos" -> INATIVOS;
            case "todos" -> TODOS;
            default -> throw new IllegalArgumentException(
                "Parâmetro situacao inválido. Use ativos, inativos ou todos.");
        };
    }
}
