package com.ecommerce.pedidos.modelo;

/**
 * Associa um produto a um carrinho de compras, registrando a quantidade de
 * itens
 * e o valor praticado no momento exato da criacao da transacao.
 */
public class ItemPedido {
    private Produto produto;
    private int quantidade;
    private double precoPraticado;

    public ItemPedido(Produto produto, int quantidade) {
        this.produto = produto;
        this.quantidade = quantidade;
        this.precoPraticado = produto.getPreco();
    }

    public Produto getProduto() {
        return produto;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public double getPrecoPraticado() {
        return precoPraticado;
    }

    public double calcularSubtotal() {
        return precoPraticado * quantidade;
    }

    @Override
    public String toString() {
        return String.format("%s (x%d) | Unitario: R$ %.2f | Subtotal: R$ %.2f",
                produto.getNome(), quantidade, precoPraticado, calcularSubtotal());
    }
}