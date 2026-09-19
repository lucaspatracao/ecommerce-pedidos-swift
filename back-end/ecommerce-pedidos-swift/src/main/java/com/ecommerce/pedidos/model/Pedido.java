package com.ecommerce.pedidos.model;

import com.ecommerce.pedidos.swift.util.PedidoUtils;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Consolida o carrinho de compras do cliente e gerencia o ciclo comercial e
 * financeiro.
 */
public class Pedido {
    private final String numero;
    private final Cliente cliente;
    private final List<ItemPedido> itens = new ArrayList<>();
    private final LocalDate data;
    private SituacaoPedido situacao;
    private FormaPagamento formaPagamento;
    private static int totalDePedidosCriados = 0;

    public Pedido(Cliente cliente) {
        this.numero = PedidoUtils.gerarNumeroDoPedido();
        if (cliente == null) {
            throw new IllegalArgumentException("Pedido exige um cliente");
        }
        this.cliente = cliente;
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

    public FormaPagamento getFormaPagamento() {
        return formaPagamento;
    }

    /**
     * Tenta adicionar um item ao pedido, dando baixa no estoque do produto
     * correspondente.
     * @param item o item a ser adicionado ao pedido
     * @return true se o item foi adicionado com sucesso; false caso contrário
     */
    public void adicionarItem(Produto produto, int quantidade) {
        if (produto == null) {
            throw new IllegalArgumentException("Produto é obrigatório");
        }
        if (quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade deve ser maior que zero.");
        }
        if (!produto.temEstoqueDisponivel(quantidade)) {
            throw new IllegalStateException("Estoque insuficiente: " + produto.getNome());
        }
        produto.baixarEstoque(quantidade);

        for (ItemPedido item : itens) {
            if (item.getProduto().getCodigo().equals(produto.getCodigo())) {
                item.setQuantidade(item.getQuantidade() + quantidade);
                return;
            }
        }
        itens.add(new ItemPedido(produto, quantidade));
    }

    /**
     * Remove um item do pedido e devolver a quantidade ao estoque do produto.
     * @param index o índice do item a ser removido
     * @throws IndexOutOfBoundsException se o índice for inválido
     */
    public void removerItem(int index) {
        if (situacao != SituacaoPedido.ABERTO) {
            throw new IllegalStateException("Somente pedidos abertos podem remover itens");
        }
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

    public List<ItemPedido> getItensOrdenadosPorSubtotal() {
        return itens.stream()
                .sorted(Comparator.comparing(ItemPedido::calcularSubtotal))
                .toList();
    }

    public void pagarCom(FormaPagamento formaPagamento) {
        if (formaPagamento == null) {
            throw new IllegalArgumentException("Forma de pagamento é obrigatória");
        }
        if (situacao != SituacaoPedido.ABERTO) {
            throw new IllegalStateException("Pedido cancelado ou já pago não pode ser pago");
        }
        if (itens.isEmpty()) {
            throw new IllegalStateException("Pedido sem itens não pode ser pago");
        }
        if (formaPagamento.getValor().compareTo(calcularValorTotal()) != 0) {
            throw new IllegalStateException("Valor do pagamento deve ser igual ao total do pedido");
        }
        if (!formaPagamento.processar()) {
            throw new IllegalStateException("Pagamento recusado");
        }
        this.formaPagamento = formaPagamento;
        this.situacao = SituacaoPedido.PAGO;
    }

    public void cancelar() {
        if (situacao == SituacaoPedido.CANCELADO) {
            throw new IllegalStateException("Pedido já está cancelado");
        }
        for (ItemPedido item : itens) {
            item.getProduto().adicionarEstoque(item.getQuantidade());
        }
        if (formaPagamento != null) {
            formaPagamento.setSituacao(SituacaoPagamento.ESTORNADO);
        }
        situacao = SituacaoPedido.CANCELADO;
    }

    @Override
    public String toString() {
        return String.format("Pedido: %s | Cliente: %s | Situacao: %s | Total: R$ %.2f",
                numero, cliente.getNome(), situacao, calcularValorTotal().doubleValue());
    }
}