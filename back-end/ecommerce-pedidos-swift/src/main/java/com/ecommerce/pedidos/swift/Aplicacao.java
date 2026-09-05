package com.ecommerce.pedidos.swift;

import com.ecommerce.pedidos.model.Cliente;
import com.ecommerce.pedidos.model.ItemPedido;
import com.ecommerce.pedidos.model.Pedido;
import com.ecommerce.pedidos.model.Produto;
import java.math.BigDecimal;

public class Aplicacao {

    public static void main(String[] args) {
        System.out.println("=== DEMONSTRAÇÃO DE VALIDAÇÃO E ENCAPSULAMENTO (AULA 05) ===\n");

        // ========== TESTES DE PRODUTO ==========
        System.out.println(">>> TESTES DO PRODUTO <<<\n");

        System.out.println("--- Produto válido ---");
        Produto produto = new Produto("PROD-100", "Notebook", new BigDecimal("3200.00"), 5);
        System.out.println(produto);

        System.out.println("\n--- Produto inválido: preço negativo ---");
        try {
            new Produto("PROD-101", "Teclado", new BigDecimal("-50.00"), 2);
        } catch (IllegalArgumentException e) {
            System.out.println("Erro: " + e.getMessage());
        }

        System.out.println("\n--- Produto inválido: estoque negativo ---");
        try {
            new Produto("PROD-102", "Monitor", new BigDecimal("899.90"), -1);
        } catch (IllegalArgumentException e) {
            System.out.println("Erro: " + e.getMessage());
        }

        System.out.println("\n--- Tentativa de mudar preço para valor negativo ---");
        try {
            produto.setPreco(new BigDecimal("-100.00"));
        } catch (IllegalArgumentException e) {
            System.out.println("Erro: " + e.getMessage());
        }

        System.out.println("\n--- Código é imutável (sem setter) ---");
        System.out.println("Código do produto: " + produto.getCodigo());
        System.out.println("Não há setCode() disponível - encapsulamento garantido!");

        System.out.println("\n--- Baixa de estoque válida ---");
        produto.baixarEstoque(1);
        System.out.println("Estoque restante após baixa de 1: " + produto.getQuantidadeEmEstoque());

        System.out.println("\n--- Baixa de estoque inválida: quantidade zero ---");
        try {
            produto.baixarEstoque(0);
        } catch (IllegalArgumentException e) {
            System.out.println("Erro: " + e.getMessage());
        }

        System.out.println("\n--- Baixa de estoque inválida: excede disponível ---");
        try {
            produto.baixarEstoque(9999);
        } catch (IllegalArgumentException e) {
            System.out.println("Erro: " + e.getMessage());
        }

        System.out.println("\n--- Adição de estoque (devolução) ---");
        produto.adicionarEstoque(10);
        System.out.println("Estoque após adição de 10: " + produto.getQuantidadeEmEstoque());

        // ========== TESTES DE CLIENTE ==========
        System.out.println("\n\n>>> TESTES DO CLIENTE <<<\n");

        System.out.println("--- Cliente válido ---");
        Cliente cliente = new Cliente("Ana Souza", "12345678909", "ana@ecommerce.com", "11987654321", "Rua das Flores, 10");
        System.out.println(cliente.getIdentificacao());

        System.out.println("\n--- Cliente inválido: e-mail sem '@' ---");
        try {
            new Cliente("Bruno", "98765432100", "bruno.email.com", "11912345678", "Avenida Central, 7");
        } catch (IllegalArgumentException e) {
            System.out.println("Erro: " + e.getMessage());
        }

        System.out.println("\n--- Cliente inválido: e-mail vazio ---");
        try {
            cliente.setEmail("");
        } catch (IllegalArgumentException e) {
            System.out.println("Erro: " + e.getMessage());
        }

        System.out.println("\n--- Cliente inválido: telefone com caracteres não-numéricos ---");
        try {
            cliente.setTelefone("(11) 9999-ABCD");
        } catch (IllegalArgumentException e) {
            System.out.println("Erro: " + e.getMessage());
        }

        System.out.println("\n--- Cliente inválido: endereço nulo ---");
        try {
            cliente.setEndereco(null);
        } catch (IllegalArgumentException e) {
            System.out.println("Erro: " + e.getMessage());
        }

        // ========== TESTES DE PEDIDO ==========
        System.out.println("\n\n>>> TESTES DO PEDIDO <<<\n");

        System.out.println("--- Pedido com item válido ---");
        Pedido pedido = new Pedido(cliente);
        ItemPedido item1 = new ItemPedido(produto, 2);
        pedido.adicionarItem(item1);
        System.out.println("Item adicionado: " + item1);
        System.out.println("Total do pedido: R$ " + pedido.calcularValorTotal());

        System.out.println("\n--- Tentativa de adicionar item com quantidade zero ---");
        try {
            new ItemPedido(produto, 0);
        } catch (IllegalArgumentException e) {
            System.out.println("Erro: " + e.getMessage());
        }

        System.out.println("\n--- Tentativa de adicionar item nulo ---");
        boolean adicionado = pedido.adicionarItem(null);
        System.out.println("Item nulo adicionado? " + adicionado);

        System.out.println("\n--- Proteção da coleção: getItens() retorna imutável ---");
        try {
            pedido.getItens().add(new ItemPedido(produto, 1));
        } catch (UnsupportedOperationException e) {
            System.out.println("Lista protegida! Erro: " + e.getClass().getSimpleName());
        }

        System.out.println("\n--- Proteção da coleção: getItens().clear() não funciona ---");
        try {
            pedido.getItens().clear();
        } catch (UnsupportedOperationException e) {
            System.out.println("Tentativa de clear() bloqueada! Erro: " + e.getClass().getSimpleName());
        }

        System.out.println("\n--- Remoção segura de item via método ---");
        System.out.println("Quantidade de itens antes: " + pedido.quantidadeDeItens());
        pedido.removerItem(0);
        System.out.println("Quantidade de itens depois: " + pedido.quantidadeDeItens());
        System.out.println("Estoque do produto após remoção: " + produto.getQuantidadeEmEstoque());

        System.out.println("\n--- Tentativa de remover item com índice inválido ---");
        try {
            pedido.removerItem(9999);
        } catch (IndexOutOfBoundsException e) {
            System.out.println("Erro: " + e.getMessage());
        }

        System.out.println("\n=== FIM DOS TESTES ===");
    }
}