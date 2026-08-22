package com.ecommerce.pedidos.swift.util;

import java.time.Year;
import java.util.Random;

public class PedidoUtils {

    private static final double VALOR_POR_QUILO   = 7.50;
    private static final double FRETE_MINIMO      = 15.00;
    private static final double VALOR_FRETE_GRATIS = 300.00;
    private static final double TAXA_DESCONTO     = 0.10;
    private static final double DESCONTO_MAXIMO   = 50.00;

    private PedidoUtils() {
        throw new UnsupportedOperationException("Esta é uma classe utilitária e não pode ser instanciada.");
    }

    public static String gerarNumeroDoPedido() {
        Random sorteio = new Random();
        int sequencial = sorteio.nextInt(100000);
        int anoAtual = Year.now().getValue();
        return String.format("PED-%d-%05d", anoAtual, sequencial);
    }

    public static double calcularSubtotal(double[] precos, int[] quantidades) {
        if (precos == null || quantidades == null || precos.length != quantidades.length) {
            return 0.0;
        }

        double subtotal = 0.0;
        for (int i = 0; i < precos.length; i++) {
            if (precos[i] >= 0 && quantidades[i] >= 0) {
                subtotal += precos[i] * quantidades[i];
            }
        }
        return subtotal;
    }

    public static double calcularFrete(double pesoEmQuilos, double subtotal) {
        if (pesoEmQuilos <= 0) {
            return 0.0;
        }
        if (subtotal >= VALOR_FRETE_GRATIS) {
            return 0.0;
        }

        double quilosCobrados = Math.ceil(pesoEmQuilos);
        double freteCalculado = quilosCobrados * VALOR_POR_QUILO;
        return Math.max(freteCalculado, FRETE_MINIMO);
    }

    public static double calcularDesconto(double subtotal) {
        if (subtotal <= 0) {
            return 0.0;
        }
        double descontoCalculado = subtotal * TAXA_DESCONTO;
        return Math.min(descontoCalculado, DESCONTO_MAXIMO);
    }

    public static String normalizarNome(String nomeDigitado) {
        if (nomeDigitado == null || nomeDigitado.isBlank()) {
            return "NÃO INFORMADO";
        }
        return nomeDigitado.trim().toUpperCase();
    }

    public static String formatarLinhaDoRecibo(String nome, double preco, int quantidade) {
        String nomeFormatado = normalizarNome(nome);
        return String.format("%-20s R$ %8.2f  x%3d", nomeFormatado, preco, quantidade);
    }

    public static String montarRecibo(String[] produtos, double[] precos, int[] quantidades,
                                      String numeroPedido, double subtotal, double frete, double desconto) {
        StringBuilder sb = new StringBuilder();
        String divisorDeLinha = System.lineSeparator();

        sb.append("==========================================").append(divisorDeLinha);
        sb.append("             RECIBO DO PEDIDO             ").append(divisorDeLinha);
        sb.append("==========================================").append(divisorDeLinha);
        sb.append(String.format("Número do Pedido: %s", numeroPedido)).append(divisorDeLinha);
        sb.append("------------------------------------------").append(divisorDeLinha);

        if (produtos != null && precos != null && quantidades != null) {
            for (int i = 0; i < produtos.length; i++) {
                sb.append(formatarLinhaDoRecibo(produtos[i], precos[i], quantidades[i])).append(divisorDeLinha);
            }
        }

        double valorTotalFinal = subtotal + frete - desconto;

        sb.append("------------------------------------------").append(divisorDeLinha);
        sb.append(String.format("%-23s R$ %8.2f", "Subtotal:", subtotal)).append(divisorDeLinha);
        sb.append(String.format("%-23s R$ %8.2f", "Frete:", frete)).append(divisorDeLinha);
        sb.append(String.format("%-23s R$ %8.2f", "Desconto:", desconto)).append(divisorDeLinha);
        sb.append("------------------------------------------").append(divisorDeLinha);
        sb.append(String.format("%-23s R$ %8.2f", "VALOR TOTAL:", valorTotalFinal)).append(divisorDeLinha);
        sb.append("==========================================").append(divisorDeLinha);

        return sb.toString();
    }
}