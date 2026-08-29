package com.ecommerce.pedidos.modelo;

/**
 * Representa um item comercializavel no catalogo e administra o seu estoque
 * fisico.
 */
public class Produto {
    private String codigo;
    private String nome;
    private String descricao;
    private double preco;
    private int quantidadeEmEstoque;
    private boolean ativo;
    private static int totalDeProdutosCriados = 0;

    public Produto(String codigo, String nome, double preco, int quantidadeEmEstoque) {
        this.codigo = codigo;
        this.nome = nome;
        this.preco = preco;
        this.quantidadeEmEstoque = quantidadeEmEstoque;
        this.ativo = true;
        totalDeProdutosCriados++;
    }

    public static int getTotalDeProdutosCriados() {
        return totalDeProdutosCriados;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public double getPreco() {
        return preco;
    }

    public void setPreco(double preco) {
        this.preco = preco;
    }

    public int getQuantidadeEmEstoque() {
        return quantidadeEmEstoque;
    }

    public void setQuantidadeEmEstoque(int quantidadeEmEstoque) {
        this.quantidadeEmEstoque = quantidadeEmEstoque;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    /**
     * Verifica se ha unidades suficientes em estoque para atender a quantidade
     * desejada.
     * O produto so responde "sim" se, alem de ter unidades, tambem estiver ativo no
     * catalogo.
     */
    public boolean temEstoqueDisponivel(int quantidadeDesejada) {
        return ativo && quantidadeEmEstoque >= quantidadeDesejada;
    }

    /**
     * Reduz o estoque apos uma venda confirmada. Ainda sem validacao de quantidade
     * negativa
     * (divida tecnica registrada para a Aula 05).
     */
    public void baixarEstoque(int quantidade) {
        this.quantidadeEmEstoque -= quantidade;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s - R$ %.2f (%d em estoque)", codigo, nome, preco, quantidadeEmEstoque);
    }
}