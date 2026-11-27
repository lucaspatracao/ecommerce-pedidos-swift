package com.ecommerce.pedidos.repositorio;

/**
 * Parâmetros de paginação das listagens (LIMIT/OFFSET).
 *
 * @param numero  página desejada, começando em 0
 * @param tamanho quantidade máxima de registros por página
 */
public record Pagina(int numero, int tamanho) {
    public static final int TAMANHO_MAXIMO = 500;

    private static final Pagina TODAS = new Pagina(0, Integer.MAX_VALUE);

    public Pagina {
        if (numero < 0) {
            throw new IllegalArgumentException("Número da página não pode ser negativo.");
        }
        if (tamanho <= 0) {
            throw new IllegalArgumentException("Tamanho da página deve ser maior que zero.");
        }
    }

    /** Página pedida pelo cliente da API (tamanho limitado a {@value #TAMANHO_MAXIMO}). */
    public static Pagina de(int numero, int tamanho) {
        if (tamanho > TAMANHO_MAXIMO) {
            throw new IllegalArgumentException("Tamanho da página deve ser no máximo " + TAMANHO_MAXIMO + ".");
        }
        return new Pagina(numero, tamanho);
    }

    /** Sem paginação: devolve todos os registros (comportamento anterior da API). */
    public static Pagina todas() {
        return TODAS;
    }

    public long offset() {
        return (long) numero * tamanho;
    }
}
