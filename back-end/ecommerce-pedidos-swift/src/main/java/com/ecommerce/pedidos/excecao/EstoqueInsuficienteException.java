package com.ecommerce.pedidos.excecao;

import com.ecommerce.pedidos.modelo.Produto;

/**
 * Tentativa de vender mais unidades do que há em estoque. Vira HTTP 409.
 *
 * <p>Guarda os números do problema (código, quantidade pedida e disponível),
 * não o objeto {@code Produto} inteiro: a exceção descreve o erro naquele
 * instante, mesmo que o produto mude depois.</p>
 */
public class EstoqueInsuficienteException extends ConflitoException {
    private final String codigoProduto;
    private final int quantidadeSolicitada;
    private final int quantidadeDisponivel;

    public EstoqueInsuficienteException(Produto produto, int quantidadeSolicitada) {
        super("Estoque insuficiente de " + produto.getNome()
                + ": disponível " + produto.getQuantidadeEmEstoque()
                + ", solicitado " + quantidadeSolicitada);
        this.codigoProduto = produto.getCodigo();
        this.quantidadeSolicitada = quantidadeSolicitada;
        this.quantidadeDisponivel = produto.getQuantidadeEmEstoque();
    }

    public String getCodigoProduto() {
        return codigoProduto;
    }

    public int getQuantidadeSolicitada() {
        return quantidadeSolicitada;
    }

    public int getQuantidadeDisponivel() {
        return quantidadeDisponivel;
    }
}
