package com.ecommerce.pedidos.model;

import java.math.BigDecimal;
/**
 * Associa um produto a um carrinho de compras, registrando a quantidade de
 * itens
 * e o valor praticado no momento exato da criacao da transacao.
 * Imutável após criação (exceto quantidade).
 */
public class ItemPedido {
    private final Produto produto;
    private int quantidade;
    private final BigDecimal precoPraticado;

    ItemPedido(Produto produto, int quantidade) {
        if (produto == null) {
            throw new IllegalArgumentException("Produto do item é obrigatório");
        }
        this.produto = produto;
        setQuantidade(quantidade);
        this.precoPraticado = produto.getPreco().setScale(2, java.math.RoundingMode.HALF_UP);
    }

    public Produto getProduto() {
        return produto;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public BigDecimal getPrecoPraticado() {
        return precoPraticado;
    }

    void setQuantidade(int quantidade) {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade do item deve ser maior que zero.");
        }
        this.quantidade = quantidade;
    }

    public BigDecimal calcularSubtotal() {
        return precoPraticado.multiply(BigDecimal.valueOf(quantidade))
                .setScale(2, java.math.RoundingMode.HALF_UP);
    }

    @Override
    public String toString() {
        return String.format("%s (x%d) | Unitario: R$ %.2f | Subtotal: R$ %.2f",
                produto.getNome(), quantidade, precoPraticado.doubleValue(), calcularSubtotal().doubleValue());
    }
}