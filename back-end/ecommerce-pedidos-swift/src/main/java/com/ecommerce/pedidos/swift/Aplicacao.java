package com.ecommerce.pedidos.swift;

import com.ecommerce.pedidos.swift.util.PedidoUtils;

public class Aplicacao {

    public static void main(String[] args) {
        System.out.println("=== EXECUÇÃO DE CASOS DE TESTE MANUAL (AULA 03) ===\n");

        System.out.println("Cenário 1: Pedido Padrão (3 itens, peso 4.2 kg)");
        String[] produtos1 = {"Teclado Mecânico", "Monitor IPS 24", "  mouse gamer  "};
        double[] precos1 = {150.00, 899.90, 79.50};
        int[] quantidades1 = {1, 2, 3};
        executarCasoDeTeste(produtos1, precos1, quantidades1, 4.2);

        System.out.println("\nCenário 2: Pedido Muito Leve (0.3 kg)");
        String[] produtos2 = {"Película Protetora"};
        double[] precos2 = {15.00};
        int[] quantidades2 = {1};
        executarCasoDeTeste(produtos2, precos2, quantidades2, 0.3);

        System.out.println("\nCenário 3: Subtotal Elevado (Acima do teto de R$ 300,00)");
        String[] produtos3 = {"Placa de Vídeo RTX"};
        double[] precos3 = {2500.00};
        int[] quantidades3 = {1};
        executarCasoDeTeste(produtos3, precos3, quantidades3, 2.0);

        System.out.println("\nCenário 4: Desconto no Teto Máximo (Subtotal de R$ 900.00)");
        String[] produtos4 = {"Notebook Estudo"};
        double[] precos4 = {900.00};
        int[] quantidades4 = {1};
        executarCasoDeTeste(produtos4, precos4, quantidades4, 1.8);
    }

    private static void executarCasoDeTeste(String[] produtos, double[] precos, int[] quantidades, double peso) {
        String nPedido = PedidoUtils.gerarNumeroDoPedido();
        double subtotal = PedidoUtils.calcularSubtotal(precos, quantidades);
        double frete = PedidoUtils.calcularFrete(peso, subtotal);
        double desconto = PedidoUtils.calcularDesconto(subtotal);

        String recibo = PedidoUtils.montarRecibo(produtos, precos, quantidades, nPedido, subtotal, frete, desconto);
        System.out.println(recibo);
    }
}