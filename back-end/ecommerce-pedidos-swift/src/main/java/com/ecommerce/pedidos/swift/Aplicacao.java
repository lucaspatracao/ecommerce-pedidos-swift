package com.ecommerce.pedidos.swift;

import com.ecommerce.pedidos.excecao.EstoqueInsuficienteException;
import com.ecommerce.pedidos.excecao.PagamentoRecusadoException;
import com.ecommerce.pedidos.model.Boleto;
import com.ecommerce.pedidos.model.CartaoCredito;
import com.ecommerce.pedidos.model.Cliente;
import com.ecommerce.pedidos.model.Pix;
import com.ecommerce.pedidos.model.Pedido;
import com.ecommerce.pedidos.model.ProcessadorPagamento;
import com.ecommerce.pedidos.model.Produto;
import com.ecommerce.pedidos.model.SituacaoPedido;
import com.ecommerce.pedidos.model.ValeCompras;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class Aplicacao {

    public static void main(String[] args) throws Exception {
        Cliente cliente = new Cliente("Ana Souza", "12345678909", "ana@ecommerce.com", "11987654321", (String) null);

        verificarExcecao("pedido sem cliente", IllegalArgumentException.class, () -> new Pedido(null));

        Pedido pedidoNulo = new Pedido(cliente);
        verificarExcecao("adicionarItem com produto null", IllegalArgumentException.class,
                () -> pedidoNulo.adicionarItem(null, 1));
        verificarExcecao("quantidade zero", IllegalArgumentException.class,
                () -> pedidoNulo.adicionarItem(new Produto("ZERO", "Produto zero", new BigDecimal("10.00"), 1), 0));
        verificarExcecao("quantidade negativa", IllegalArgumentException.class,
                () -> pedidoNulo.adicionarItem(new Produto("NEG", "Produto negativo", new BigDecimal("10.00"), 1), -1));
        verificarExcecao("quantidade maior que o estoque", EstoqueInsuficienteException.class,
                () -> pedidoNulo.adicionarItem(new Produto("EST", "Estoque curto", new BigDecimal("10.00"), 1), 2));

        Pedido pedidoVazio = new Pedido(cliente);
        verificarExcecao("pagar pedido sem itens", IllegalStateException.class,
                () -> pedidoVazio.pagar(new Pix(new BigDecimal("10.00"), "a@b.com", "EMAIL")));

        Pedido pedidoCancelado = new Pedido(cliente);
        Produto produtoCancelado = new Produto("CAN", "Produto cancelado", new BigDecimal("10.00"), 2);
        pedidoCancelado.adicionarItem(produtoCancelado, 1);
        pedidoCancelado.cancelar();
        verificarExcecao("pagar pedido cancelado", IllegalStateException.class,
                () -> pedidoCancelado.pagarCom(new Pix(new BigDecimal("10.00"), "a@b.com", "EMAIL")));

        Pedido pedidoLista = new Pedido(cliente);
        pedidoLista.adicionarItem(new Produto("LIS", "Produto da lista", new BigDecimal("10.00"), 1), 1);
        verificarExcecao("getItens().clear()", UnsupportedOperationException.class,
                () -> pedidoLista.getItens().clear());

        Pedido pedidoTotal = new Pedido(cliente);
        pedidoTotal.adicionarItem(new Produto("TOT", "Produto dez", new BigDecimal("10.00"), 2), 2);
        pedidoTotal.adicionarItem(new Produto("CIN", "Produto cinco", new BigDecimal("5.50"), 1), 1);
        verificarCondicao("total calculado manualmente", new BigDecimal("25.50").compareTo(pedidoTotal.calcularValorTotal()) == 0);

        Pedido pedidoRepetido = new Pedido(cliente);
        Produto produtoRepetido = new Produto("REP", "Produto repetido", new BigDecimal("10.00"), 5);
        pedidoRepetido.adicionarItem(produtoRepetido, 1);
        pedidoRepetido.adicionarItem(produtoRepetido, 2);
        verificarCondicao("item repetido soma a quantidade", pedidoRepetido.getItens().size() == 1
                && pedidoRepetido.getItens().get(0).getQuantidade() == 3);

        Produto produtoEstoque = new Produto("DEV", "Produto devolvido", new BigDecimal("10.00"), 3);
        Pedido pedidoEstoque = new Pedido(cliente);
        pedidoEstoque.adicionarItem(produtoEstoque, 2);
        pedidoEstoque.cancelar();
        verificarCondicao("cancelar devolve o estoque", produtoEstoque.getQuantidadeEmEstoque() == 3);

        verificarExcecao("produto com preço negativo", IllegalArgumentException.class,
                () -> new Produto("PRE", "Produto inválido", new BigDecimal("-1.00"), 1));

        Pedido pedidoPagamentoRecusado = new Pedido(cliente);
        Produto produtoPagamentoRecusado = new Produto("PAG", "Produto do pagamento", new BigDecimal("50.00"), 5);
        pedidoPagamentoRecusado.adicionarItem(produtoPagamentoRecusado, 1);
        try {
            pedidoPagamentoRecusado.pagar(new CartaoCredito(new BigDecimal("50.00"), "**** 0000", "Visa", 1));
        } catch (PagamentoRecusadoException e) {
            verificarCondicao("cartão recusado mantém pedido ABERTO", pedidoPagamentoRecusado.getSituacao() == SituacaoPedido.ABERTO);
        }

        simularLoopPolimorfico(cliente);
    }

    private static void simularLoopPolimorfico(Cliente cliente) {
        List<ProcessadorPagamento> processadores = List.of(
                new Pix(new BigDecimal("50.00"), "ana@ecommerce.com", "EMAIL"),
                new Boleto(new BigDecimal("50.00"), LocalDate.now().plusDays(5), "12345678901234567890"),
                new CartaoCredito(new BigDecimal("50.00"), "**** 1234", "Visa", 3),
                new ValeCompras(new BigDecimal("50.00"), "VALE-001", "Loja Swift")
        );

        for (ProcessadorPagamento processador : processadores) {
            Pedido pedido = new Pedido(cliente);
            try {
                pedido.adicionarItem(new Produto("SIM", "Produto do laço", new BigDecimal("50.00"), 10), 1);
                pedido.pagar(processador);
                String status = pedido.getSituacao().name();
                System.out.printf("OK: %s -> %s | %s%n", processador.getDescricao(), status, processador.getComprovante());
            } catch (PagamentoRecusadoException e) {
                System.out.printf("OK: %s -> %s | %s%n", processador.getDescricao(), pedido.getSituacao(), e.getMessage());
            } catch (EstoqueInsuficienteException e) {
                System.out.printf("FALHOU: estoque insuficiente em laço polimórfico | %s%n", e.getMessage());
            }
        }
    }

    private interface AcaoComExcecao {
        void executar() throws Exception;
    }

    private static void verificarExcecao(String cenario, Class<? extends Throwable> tipoEsperado, AcaoComExcecao acao) {
        try {
            acao.executar();
            System.out.println("FALHOU: " + cenario);
        } catch (Throwable erro) {
            if (tipoEsperado.isInstance(erro)) {
                System.out.println("OK: " + cenario + " -> " + erro.getClass().getSimpleName() + ": " + erro.getMessage());
            } else {
                System.out.println("FALHOU: " + cenario + " (" + erro.getClass().getSimpleName() + ")");
            }
        }
    }

    private static void verificarCondicao(String cenario, boolean resultado) {
        System.out.println((resultado ? "OK: " : "FALHOU: ") + cenario);
    }
}