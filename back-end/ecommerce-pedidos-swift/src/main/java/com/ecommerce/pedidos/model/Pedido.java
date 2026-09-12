package com.ecommerce.pedidos.model;

import com.ecommerce.pedidos.swift.util.PedidoUtils;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Consolida o carrinho de compras do cliente e gerencia o ciclo comercial e
 * financeiro.
 */
public class Pedido {
    private final String numero;
    private final Cliente cliente;
    private final List<ItemPedido> itens;
    private final LocalDate data;
    private SituacaoPedido situacao;
    private static int totalDePedidosCriados = 0;

    public Pedido(Cliente cliente) {
        this.numero = PedidoUtils.gerarNumeroDoPedido();
        this.cliente = Objects.requireNonNull(cliente, "Cliente do pedido não pode ser nulo.");
        this.itens = new ArrayList<>();
        this.data = LocalDate.now();
        this.situacao = SituacaoPedido.ABERTO;
        totalDePedidosCriados++;
    }

    public static int getTotalDePedidosCriados() {
        return totalDePedidosCriados;
    }

    public String getNumero() {
        return numero;
    }

    public Cliente getCliente() {
        return cliente;
    }

    /**
     * Obtém a lista de itens do pedido de forma imutável.
     * @return cópia não modificável da lista de itens
     */
    public List<ItemPedido> getItens() {
        return Collections.unmodifiableList(new ArrayList<>(itens));
    }

    /**
     * Retorna o número total de itens no pedido.
     * @return quantidade de itens
     */
    public int quantidadeDeItens() {
        return itens.size();
    }

    public LocalDate getData() {
        return data;
    }

    public SituacaoPedido getSituacao() {
        return situacao;
    }

    public void setSituacao(SituacaoPedido situacao) {
        if (situacao == null) {
            throw new IllegalArgumentException("Situação do pedido não pode ser nula.");
        }
        this.situacao = situacao;
    }

    /**
     * Tenta adicionar um item ao pedido, dando baixa no estoque do produto
     * correspondente.
     * @param item o item a ser adicionado ao pedido
     * @return true se o item foi adicionado com sucesso; false caso contrário
     */
    public boolean adicionarItem(ItemPedido item) {
        if (item == null) {
            return false;
        }
        Produto produto = item.getProduto();
        if (!produto.temEstoqueDisponivel(item.getQuantidade())) {
            return false;
        }
        itens.add(item);
        produto.baixarEstoque(item.getQuantidade());
        return true;
    }

    /**
     * Remove um item do pedido e devolver a quantidade ao estoque do produto.
     * @param index o índice do item a ser removido
     * @throws IndexOutOfBoundsException se o índice for inválido
     */
    public void removerItem(int index) {
        if (index < 0 || index >= itens.size()) {
            throw new IndexOutOfBoundsException("Índice do item inválido: " + index);
        }
        ItemPedido itemRemovido = itens.remove(index);
        itemRemovido.getProduto().adicionarEstoque(itemRemovido.getQuantidade());
    }

    public BigDecimal calcularValorTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (ItemPedido item : itens) {
            total = total.add(item.calcularSubtotal());
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String toString() {
        return String.format("Pedido: %s | Cliente: %s | Situacao: %s | Total: R$ %.2f",
                numero, cliente.getNome(), situacao, calcularValorTotal().doubleValue());
    }
}