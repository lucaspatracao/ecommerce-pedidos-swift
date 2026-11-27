package com.ecommerce.pedidos.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Linha do pedido: um produto, a quantidade e o preço praticado.
 *
 * <p>O preço é copiado do produto no momento da compra. Se o produto mudar
 * de preço depois, os pedidos antigos continuam com o valor que foi cobrado.</p>
 *
 * <p>O construtor é de pacote: itens só nascem por {@link Pedido#adicionarItem},
 * que também cuida do estoque.</p>
 */
@Entity
public class ItemPedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Lado dono do relacionamento Pedido 1–N ItemPedido: guarda a coluna pedido_id. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    /** ItemPedido N–1 Produto. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @Column(nullable = false)
    private int quantidade;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precoPraticado;

    protected ItemPedido() {
        // exigido pelo JPA
    }

    ItemPedido(Pedido pedido, Produto produto, int quantidade) {
        if (produto == null) {
            throw new IllegalArgumentException("Produto do item é obrigatório");
        }
        this.pedido = pedido;
        this.produto = produto;
        this.quantidade = validarQuantidade(quantidade);
        this.precoPraticado = produto.getPreco().setScale(2, RoundingMode.HALF_UP);
    }

    ItemPedido(Produto produto, int quantidade) {
        this(null, produto, quantidade);
    }

    public Long getId() {
        return id;
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

    void aumentarQuantidade(int adicional) {
        this.quantidade = validarQuantidade(this.quantidade + adicional);
    }

    /** Regra de negócio: subtotal = preço praticado × quantidade. */
    public BigDecimal calcularSubtotal() {
        return precoPraticado.multiply(BigDecimal.valueOf(quantidade))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private static int validarQuantidade(int quantidade) {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade do item deve ser maior que zero.");
        }
        return quantidade;
    }

    @Override
    public String toString() {
        return String.format("%s (x%d) | Unitario: R$ %.2f | Subtotal: R$ %.2f",
                produto.getNome(), quantidade, precoPraticado.doubleValue(), calcularSubtotal().doubleValue());
    }
}
