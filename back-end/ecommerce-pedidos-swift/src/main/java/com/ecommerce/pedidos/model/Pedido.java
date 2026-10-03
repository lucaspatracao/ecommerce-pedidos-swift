package com.ecommerce.pedidos.model;

import com.ecommerce.pedidos.excecao.EstoqueInsuficienteException;
import com.ecommerce.pedidos.excecao.PagamentoRecusadoException;
import com.ecommerce.pedidos.swift.util.PedidoUtils;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
    private ProcessadorPagamento formaPagamento;
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
     *
     * @return cópia não modificável da lista de itens
     */
    public List<ItemPedido> getItens() {
        return Collections.unmodifiableList(new ArrayList<>(itens));
    }

    /**
     * Retorna o número total de itens no pedido.
     *
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

    public ProcessadorPagamento getFormaPagamento() {
        return formaPagamento;
    }

    /**
     * Tenta adicionar um item ao pedido, dando baixa no estoque do produto
     * correspondente.
     *
     * @param produto   o produto a ser adicionado ao pedido
     * @param quantidade a quantidade solicitada
     * @throws EstoqueInsuficienteException quando o estoque não basta para atender a venda
     */
    public void adicionarItem(Produto produto, int quantidade) throws EstoqueInsuficienteException {
        if (produto == null) {
            throw new IllegalArgumentException("Produto é obrigatório");
        }
        if (quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser positiva");
        }
        if (situacao != SituacaoPedido.ABERTO) {
            throw new IllegalStateException("Não é possível adicionar itens a pedido " + situacao);
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
     *
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

    public void pagar(ProcessadorPagamento processador) throws PagamentoRecusadoException {
        if (processador == null) {
            throw new IllegalArgumentException("Processador é obrigatório");
        }
        if (situacao != SituacaoPedido.ABERTO) {
            throw new IllegalStateException("Pedido " + situacao + " não pode ser pago");
        }
        if (itens.isEmpty()) {
            throw new IllegalStateException("Pedido sem itens não pode ser pago");
        }

        BigDecimal valorTotal = calcularValorTotal();
        if (!processador.processar(valorTotal)) {
            throw new PagamentoRecusadoException(processador.getDescricao(), "processador retornou recusa");
        }

        this.formaPagamento = processador;
        this.situacao = SituacaoPedido.PAGO;
    }

    public void pagar() throws PagamentoRecusadoException {
        if (formaPagamento == null) {
            throw new IllegalStateException("Pedido não possui forma de pagamento definida");
        }
        pagar(formaPagamento);
    }

    public void pagarCom(FormaPagamento formaPagamento) throws PagamentoRecusadoException {
        if (formaPagamento == null) {
            throw new IllegalArgumentException("Forma de pagamento é obrigatória");
        }
        if (formaPagamento.getValor().compareTo(calcularValorTotal()) != 0) {
            throw new IllegalStateException("Valor do pagamento deve ser igual ao total do pedido");
        }
        pagar((ProcessadorPagamento) formaPagamento);
    }

    public void cancelar() {
        if (situacao == SituacaoPedido.CANCELADO) {
            throw new IllegalStateException("Pedido já está cancelado");
        }
        for (ItemPedido item : itens) {
            item.getProduto().adicionarEstoque(item.getQuantidade());
        }
        if (formaPagamento instanceof FormaPagamento pagamento) {
            pagamento.setSituacao(SituacaoPagamento.ESTORNADO);
        }
        situacao = SituacaoPedido.CANCELADO;
    }

    @Override
    public String toString() {
        return String.format("Pedido: %s | Cliente: %s | Situacao: %s | Total: R$ %.2f",
                numero, cliente.getNome(), situacao, calcularValorTotal().doubleValue());
    }
}