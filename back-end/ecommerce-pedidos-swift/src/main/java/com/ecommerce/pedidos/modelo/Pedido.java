package com.ecommerce.pedidos.modelo;

import com.ecommerce.pedidos.swift.util.PedidoUtils;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

/**
 * Consolida o carrinho de compras do cliente e gerencia o ciclo comercial e
 * financeiro.
 */
public class Pedido {
    private String numero;
    private Cliente cliente;
    private List<ItemPedido> itens;
    private LocalDate data;
    private SituacaoPedido situacao;
    private static int totalDePedidosCriados = 0;

    public Pedido(Cliente cliente) {
        this.numero = PedidoUtils.gerarNumeroDoPedido();
        this.cliente = cliente;
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

    public List<ItemPedido> getItens() {
        return new ArrayList<>(itens);
    }

    public LocalDate getData() {
        return data;
    }

    public SituacaoPedido getSituacao() {
        return situacao;
    }

    public void setSituacao(SituacaoPedido situacao) {
        this.situacao = situacao;
    }

    /**
     * Tenta adicionar um item ao pedido, dando baixa no estoque do produto
     * correspondente.
     * Devolve false (sem lancar excecao) quando o item e nulo ou o produto nao tem
     * estoque
     * suficiente -- a validacao com excecoes propriamente dita fica para a Aula 09.
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

    public double calcularValorTotal() {
        double total = 0.0;
        for (ItemPedido item : itens) {
            total += item.calcularSubtotal();
        }
        return total;
    }

    @Override
    public String toString() {
        return String.format("Pedido: %s | Cliente: %s | Situacao: %s | Total: R$ %.2f",
                numero, cliente.getNome(), situacao, calcularValorTotal());
    }
}