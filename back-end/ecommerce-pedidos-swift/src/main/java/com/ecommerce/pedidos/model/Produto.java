package com.ecommerce.pedidos.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Representa um item comercializavel no catalogo e administra o seu estoque
 * fisico.
 */
public class Produto {
    private final String codigo;
    private String nome;
    private String descricao;
    private BigDecimal preco;
    private int quantidadeEmEstoque;
    private boolean ativo;
    private static int totalDeProdutosCriados = 0;

    public Produto(String codigo, String nome, BigDecimal preco, int quantidadeEmEstoque) {
        this.codigo = validarCodigo(codigo);
        setNome(nome);
        setDescricao(null);
        setPreco(preco);
        setQuantidadeEmEstoque(quantidadeEmEstoque);
        this.ativo = true;
        totalDeProdutosCriados++;
    }

    public Produto(String codigo, String nome, double preco, int quantidadeEmEstoque) {
        this(codigo, nome, BigDecimal.valueOf(preco), quantidadeEmEstoque);
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
        if (nome == null) {
            throw new IllegalArgumentException("Nome do produto não pode ser nulo.");
        }
        String nomeValidado = nome.trim();
        if (nomeValidado.isEmpty()) {
            throw new IllegalArgumentException("Nome do produto não pode estar vazio.");
        }
        this.nome = nomeValidado;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao == null ? "" : descricao.trim();
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public void setPreco(BigDecimal preco) {
        if (preco == null) {
            throw new IllegalArgumentException("Preço do produto não pode ser nulo.");
        }
        if (preco.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Preço do produto não pode ser negativo.");
        }
        this.preco = preco.setScale(2, RoundingMode.HALF_UP);
    }

    public int getQuantidadeEmEstoque() {
        return quantidadeEmEstoque;
    }

    public void setQuantidadeEmEstoque(int quantidadeEmEstoque) {
        if (quantidadeEmEstoque < 0) {
            throw new IllegalArgumentException("Quantidade em estoque não pode ser negativa.");
        }
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
        if (quantidadeDesejada <= 0) {
            return false;
        }
        return ativo && quantidadeEmEstoque >= quantidadeDesejada;
    }

    /**
     * Reduz o estoque apos uma venda confirmada.
     */
    public void baixarEstoque(int quantidade) {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade para baixa no estoque deve ser positiva.");
        }
        if (quantidade > quantidadeEmEstoque) {
            throw new IllegalArgumentException("Quantidade solicitada excede o estoque disponível.");
        }
        this.quantidadeEmEstoque -= quantidade;
    }

    /**
     * Adiciona quantidade ao estoque (p.ex., devolução de item).
     * @param quantidade a quantidade a adicionar
     * @throws IllegalArgumentException se a quantidade for menor ou igual a zero
     */
    public void adicionarEstoque(int quantidade) {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade para adição ao estoque deve ser positiva.");
        }
        this.quantidadeEmEstoque += quantidade;
    }

    private String validarCodigo(String codigo) {
        String codigoValidado = validarTextoObrigatorio(codigo, "Código do produto");
        if (codigoValidado.length() < 3) {
            throw new IllegalArgumentException("Código do produto deve ter pelo menos 3 caracteres.");
        }
        return codigoValidado;
    }

    private String validarTextoObrigatorio(String valor, String nomeCampo) {
        Objects.requireNonNull(valor, nomeCampo + " não pode ser nulo.");
        String valorNormalizado = valor.trim();
        if (valorNormalizado.isBlank()) {
            throw new IllegalArgumentException(nomeCampo + " não pode estar em branco.");
        }
        return valorNormalizado;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s - R$ %.2f (%d em estoque)", codigo, nome, preco.doubleValue(), quantidadeEmEstoque);
    }
}