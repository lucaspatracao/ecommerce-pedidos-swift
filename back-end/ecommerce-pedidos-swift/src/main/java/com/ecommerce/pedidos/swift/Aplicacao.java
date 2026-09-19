package com.ecommerce.pedidos.swift;

import com.ecommerce.pedidos.model.Cliente;
import com.ecommerce.pedidos.model.Pix;
import com.ecommerce.pedidos.model.Pedido;
import com.ecommerce.pedidos.model.Produto;
import java.math.BigDecimal;

public class Aplicacao {

    public static void main(String[] args) {
        Cliente cliente = new Cliente("Ana Souza", "12345678909", "ana@ecommerce.com", "11987654321", (String) null);

        verificarExcecao("pedido sem cliente", IllegalArgumentException.class, () -> new Pedido(null));

        Pedido pedidoNulo = new Pedido(cliente);
        verificarExcecao("adicionarItem com produto null", IllegalArgumentException.class,
                () -> pedidoNulo.adicionarItem(null, 1));
        verificarExcecao("quantidade zero", IllegalArgumentException.class,
                () -> pedidoNulo.adicionarItem(new Produto("ZERO", "Produto zero", 10.00, 1), 0));
        verificarExcecao("quantidade negativa", IllegalArgumentException.class,
                () -> pedidoNulo.adicionarItem(new Produto("NEG", "Produto negativo", 10.00, 1), -1));
        verificarExcecao("quantidade maior que o estoque", IllegalStateException.class,
                () -> pedidoNulo.adicionarItem(new Produto("EST", "Estoque curto", 10.00, 1), 2));

        verificarExcecao("pagar pedido sem itens", IllegalStateException.class,
            () -> pedidoNulo.pagarCom(new Pix(BigDecimal.ONE, "a@b.com", "EMAIL")));

        Pedido pedidoCancelado = new Pedido(cliente);
        Produto produtoCancelado = new Produto("CAN", "Produto cancelado", 10.00, 2);
        pedidoCancelado.adicionarItem(produtoCancelado, 1);
        pedidoCancelado.cancelar();
        verificarExcecao("pagar pedido cancelado", IllegalStateException.class,
                () -> pedidoCancelado.pagarCom(new Pix(new BigDecimal("10.00"), "a@b.com", "EMAIL")));

        Pedido pedidoLista = new Pedido(cliente);
        pedidoLista.adicionarItem(new Produto("LIS", "Produto da lista", 10.00, 1), 1);
        verificarExcecao("getItens().clear()", UnsupportedOperationException.class,
                () -> pedidoLista.getItens().clear());

        Pedido pedidoTotal = new Pedido(cliente);
        pedidoTotal.adicionarItem(new Produto("TOT", "Produto dez", 10.00, 2), 2);
        pedidoTotal.adicionarItem(new Produto("CIN", "Produto cinco", 5.50, 1), 1);
        verificarCondicao("total calculado manualmente", new BigDecimal("25.50").compareTo(pedidoTotal.calcularValorTotal()) == 0);

        Pedido pedidoRepetido = new Pedido(cliente);
        Produto produtoRepetido = new Produto("REP", "Produto repetido", 10.00, 5);
        pedidoRepetido.adicionarItem(produtoRepetido, 1);
        pedidoRepetido.adicionarItem(produtoRepetido, 2);
        verificarCondicao("item repetido soma a quantidade", pedidoRepetido.getItens().size() == 1
                && pedidoRepetido.getItens().get(0).getQuantidade() == 3);

        Produto produtoEstoque = new Produto("DEV", "Produto devolvido", 10.00, 3);
        Pedido pedidoEstoque = new Pedido(cliente);
        pedidoEstoque.adicionarItem(produtoEstoque, 2);
        pedidoEstoque.cancelar();
        verificarCondicao("cancelar devolve o estoque", produtoEstoque.getQuantidadeEmEstoque() == 3);
    }

    private static void verificarExcecao(String cenario, Class<? extends Throwable> tipoEsperado, Runnable acao) {
        try {
            acao.run();
            System.out.println("FALHOU: " + cenario);
        } catch (Throwable erro) {
            if (tipoEsperado.isInstance(erro)) {
                System.out.println("OK: " + cenario);
            } else {
                System.out.println("FALHOU: " + cenario + " (" + erro.getClass().getSimpleName() + ")");
            }
        }
    }

    private static void verificarCondicao(String cenario, boolean resultado) {
        System.out.println((resultado ? "OK: " : "FALHOU: ") + cenario);
    }
}