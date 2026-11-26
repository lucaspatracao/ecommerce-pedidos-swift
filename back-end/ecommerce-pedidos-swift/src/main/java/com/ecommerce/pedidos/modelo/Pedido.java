package com.ecommerce.pedidos.modelo;

import com.ecommerce.pedidos.excecao.EstoqueInsuficienteException;
import com.ecommerce.pedidos.excecao.PagamentoRecusadoException;
import com.ecommerce.pedidos.excecao.PedidoInvalidoException;
import com.ecommerce.pedidos.util.PedidoUtils;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Carrinho de compras do cliente e ciclo de vida da venda
 * (ABERTO → PAGO, ou → CANCELADO).
 *
 * <p>Relacionamentos (Aula 07): o pedido pertence a um {@link Cliente}
 * (N–1), tem vários {@link ItemPedido} (1–N) e no máximo uma
 * {@link FormaPagamento} (1–1).</p>
 *
 * <p>As regras ficam aqui dentro: o pedido calcula o próprio total, só aceita
 * itens enquanto está aberto e só pode ser pago uma vez.</p>
 */
public class Pedido {
    private static int totalDePedidosCriados = 0;

    private String numero;
    private Cliente cliente;
    private final List<ItemPedido> itens = new ArrayList<>();
    private LocalDate data;
    private SituacaoPedido situacao;
    private FormaPagamento formaPagamento;

    public Pedido(Cliente cliente) {
        if (cliente == null) {
            throw new IllegalArgumentException("Pedido exige um cliente");
        }
        this.numero = PedidoUtils.gerarNumeroDoPedido();
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

    /** Cópia somente leitura: itens só entram e saem pelos métodos do pedido. */
    public List<ItemPedido> getItens() {
        return Collections.unmodifiableList(new ArrayList<>(itens));
    }

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

    public boolean isAberto() {
        return situacao == SituacaoPedido.ABERTO;
    }

    /**
     * Adiciona um item e dá baixa no estoque do produto. Se o produto já
     * estiver no pedido, só aumenta a quantidade.
     *
     * @throws EstoqueInsuficienteException quando o estoque não basta
     */
    public void adicionarItem(Produto produto, int quantidade) {
        if (produto == null) {
            throw new IllegalArgumentException("Produto é obrigatório");
        }
        if (quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser positiva");
        }
        if (!isAberto()) {
            throw new IllegalStateException("Não é possível adicionar itens a pedido " + situacao);
        }

        produto.baixarEstoque(quantidade);

        for (ItemPedido item : itens) {
            if (item.getProduto().getCodigo().equals(produto.getCodigo())) {
                item.aumentarQuantidade(quantidade);
                return;
            }
        }
        itens.add(new ItemPedido(this, produto, quantidade));
    }

    /** Remove um item e devolve a quantidade ao estoque do produto. */
    public void removerItem(int index) {
        if (!isAberto()) {
            throw new IllegalStateException("Somente pedidos abertos podem remover itens");
        }
        if (index < 0 || index >= itens.size()) {
            throw new IndexOutOfBoundsException("Índice do item inválido: " + index);
        }
        ItemPedido itemRemovido = itens.remove(index);
        itemRemovido.getProduto().adicionarEstoque(itemRemovido.getQuantidade());
    }

    /** Regra de negócio: o total é a soma dos subtotais dos itens. */
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

    /**
     * Paga o pedido cobrando o total na forma de pagamento recebida.
     *
     * <p>O pedido não sabe se é Pix, boleto ou cartão: só chama
     * {@code processar}, e cada subclasse decide se aprova (polimorfismo).</p>
     */
    public void pagar(FormaPagamento pagamento) {
        if (pagamento == null) {
            throw new IllegalArgumentException("Forma de pagamento é obrigatória");
        }
        if (!isAberto()) {
            throw new PedidoInvalidoException("Pedido " + situacao + " não pode ser pago");
        }
        if (itens.isEmpty()) {
            throw new PedidoInvalidoException("Pedido sem itens não pode ser pago");
        }

        if (!pagamento.processar(calcularValorTotal())) {
            throw new PagamentoRecusadoException(pagamento.getDescricao(), "processador retornou recusa");
        }

        this.formaPagamento = pagamento;
        this.situacao = SituacaoPedido.PAGO;
    }

    /**
     * Variante usada pela API: o valor informado no pagamento precisa bater
     * com o total do pedido (evita pagar a menos ou a mais).
     */
    public void pagarCom(FormaPagamento pagamento) {
        if (pagamento == null) {
            throw new IllegalArgumentException("Forma de pagamento é obrigatória");
        }
        if (pagamento.getValor().compareTo(calcularValorTotal()) != 0) {
            throw new IllegalStateException("Valor do pagamento deve ser igual ao total do pedido");
        }
        pagar(pagamento);
    }

    /** Cancela o pedido: devolve o estoque e, se já estava pago, estorna o pagamento. */
    public void cancelar() {
        if (situacao == SituacaoPedido.CANCELADO) {
            throw new IllegalStateException("Pedido já está cancelado");
        }
        for (ItemPedido item : itens) {
            item.getProduto().adicionarEstoque(item.getQuantidade());
        }
        if (formaPagamento != null) {
            formaPagamento.estornar();
        }
        situacao = SituacaoPedido.CANCELADO;
    }

    @Override
    public String toString() {
        return String.format("Pedido: %s | Cliente: %s | Situacao: %s | Total: R$ %.2f",
                numero, cliente.getNome(), situacao, calcularValorTotal().doubleValue());
    }
}
