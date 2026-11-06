package com.ecommerce.pedidos.modelo;

import java.math.BigDecimal;
/**
 * Associa um produto a um carrinho de compras, registrando a quantidade de
 * itens
 * e o valor praticado no momento exato da criacao da transacao.
 * Imutável após criação (exceto quantidade).
 */
public class ItemPedido {
    private Long id;
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

    private ItemPedido(Long id, Produto produto, int quantidade, BigDecimal precoPraticado) {
        if (produto == null) {
            throw new IllegalArgumentException("Produto do item é obrigatório");
        }
        if (precoPraticado == null || precoPraticado.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Preço praticado do item deve ser positivo.");
        }
        this.id = id;
        this.produto = produto;
        setQuantidade(quantidade);
        this.precoPraticado = precoPraticado.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * Recria um item lido do banco com o preço cobrado na época da venda,
     * mesmo que o preço atual do produto seja outro (ou o produto esteja inativo).
     */
    public static ItemPedido reconstituir(Long id, Produto produto, int quantidade, BigDecimal precoPraticado) {
        return new ItemPedido(id, produto, quantidade, precoPraticado);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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