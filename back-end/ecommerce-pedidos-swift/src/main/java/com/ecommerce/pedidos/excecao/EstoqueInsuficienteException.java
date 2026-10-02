package com.ecommerce.pedidos.excecao;

import com.ecommerce.pedidos.model.Produto;

public class EstoqueInsuficienteException extends ECommerceException {
    private final Produto produto;
    private final int quantidadeSolicitada;

    public EstoqueInsuficienteException(Produto produto, int quantidade) {
        super("Estoque insuficiente de " + produto.getNome()
                + ": disponível " + produto.getQuantidadeEmEstoque()
                + ", solicitado " + quantidade);
        this.produto = produto;
        this.quantidadeSolicitada = quantidade;
    }

    public Produto getProduto() {
        return produto;
    }

    public int getQuantidadeSolicitada() {
        return quantidadeSolicitada;
    }
}
