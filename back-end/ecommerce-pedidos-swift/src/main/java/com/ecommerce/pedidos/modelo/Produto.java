package com.ecommerce.pedidos.modelo;

import com.ecommerce.pedidos.excecao.EstoqueInsuficienteException;
import com.ecommerce.pedidos.util.Utils;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Item comercializável do catálogo. Também é dono das regras do seu estoque:
 * ninguém altera {@code quantidadeEmEstoque} diretamente, só pelos métodos
 * {@link #baixarEstoque}, {@link #adicionarEstoque} e {@link #definirEstoque}.
 */
@Entity
public class Produto {
    private static final int TAMANHO_MINIMO_CODIGO = 3;
    private static int totalDeProdutosCriados = 0;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String codigo;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, length = 500)
    private String descricao;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal preco;

    @Column(nullable = false)
    private int quantidadeEmEstoque;

    @Column(nullable = false)
    private boolean ativo;

    /**
     * Bloqueio otimista: o Hibernate inclui "WHERE versao = ?" em cada UPDATE.
     * Se outra transação alterou o produto antes (ex.: duas vendas ao mesmo
     * tempo), o UPDATE não acha a linha e a operação falha com 409, em vez
     * de uma venda apagar a baixa de estoque da outra.
     */
    @Version
    private Long versao;

    protected Produto() {
        // exigido pelo JPA
    }

    public Produto(String codigo, String nome, BigDecimal preco, int quantidadeEmEstoque) {
        this(codigo, nome, null, preco, quantidadeEmEstoque);
    }

    public Produto(String codigo, String nome, double preco, int quantidadeEmEstoque) {
        this(codigo, nome, BigDecimal.valueOf(preco), quantidadeEmEstoque);
    }

    public Produto(String codigo, String nome, String descricao, BigDecimal preco, int quantidadeEmEstoque) {
        this.codigo = validarCodigo(codigo);
        atualizarDados(nome, descricao, preco);
        definirEstoque(quantidadeEmEstoque);
        this.ativo = true;
        totalDeProdutosCriados++;
    }

    public static int getTotalDeProdutosCriados() {
        return totalDeProdutosCriados;
    }

    public Long getId() {
        return id;
    }

    /** Versão do controle de concorrência otimista; o Hibernate a incrementa a cada UPDATE. */
    public Long getVersao() {
        return versao;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public int getQuantidadeEmEstoque() {
        return quantidadeEmEstoque;
    }

    public boolean isAtivo() {
        return ativo;
    }

    /**
     * Atualiza os dados cadastrais. Tudo é validado antes de qualquer
     * atribuição, para o produto nunca ficar "meio atualizado".
     */
    public void atualizarDados(String nome, String descricao, BigDecimal preco) {
        String nomeValidado = Utils.textoObrigatorio(nome, "Nome do produto");
        BigDecimal precoValidado = validarPreco(preco);
        this.nome = nomeValidado;
        this.descricao = descricao == null ? "" : descricao.trim();
        this.preco = precoValidado;
    }

    /** Troca o código do produto (usado quando o cadastro é corrigido). */
    public void alterarCodigo(String novoCodigo) {
        this.codigo = validarCodigo(novoCodigo);
    }

    /** Ajuste de inventário: define o saldo absoluto do estoque. */
    public void definirEstoque(int quantidade) {
        if (quantidade < 0) {
            throw new IllegalArgumentException("Estoque não pode ser negativo, recebido: " + quantidade);
        }
        this.quantidadeEmEstoque = quantidade;
    }

    public void ativar() {
        this.ativo = true;
    }

    public void desativar() {
        this.ativo = false;
    }

    /** Atalho para a API, que envia a disponibilidade como booleano. */
    public void definirDisponibilidade(boolean disponivel) {
        if (disponivel) {
            ativar();
        } else {
            desativar();
        }
    }

    /**
     * Verifica se há unidades suficientes para a quantidade desejada.
     * Só responde "sim" se o produto também estiver ativo no catálogo.
     */
    public boolean temEstoqueDisponivel(int quantidadeDesejada) {
        if (quantidadeDesejada <= 0) {
            return false;
        }
        return ativo && quantidadeEmEstoque >= quantidadeDesejada;
    }

    /** Reduz o estoque após uma venda. */
    public void baixarEstoque(int quantidade) {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser positiva, recebido: " + quantidade);
        }
        if (quantidade > this.quantidadeEmEstoque) {
            throw new EstoqueInsuficienteException(this, quantidade);
        }
        this.quantidadeEmEstoque -= quantidade;
    }

    /** Devolve unidades ao estoque (ex.: item removido ou pedido cancelado). */
    public void adicionarEstoque(int quantidade) {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade para adição ao estoque deve ser positiva.");
        }
        this.quantidadeEmEstoque += quantidade;
    }

    private static String validarCodigo(String codigo) {
        String valor = Utils.textoObrigatorio(codigo, "Código do produto");
        if (valor.length() < TAMANHO_MINIMO_CODIGO) {
            throw new IllegalArgumentException("Código do produto deve ter pelo menos 3 caracteres.");
        }
        return valor;
    }

    private static BigDecimal validarPreco(BigDecimal preco) {
        if (preco == null) {
            throw new IllegalArgumentException("Preço do produto não pode ser nulo.");
        }
        if (preco.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Preço deve ser positivo, recebido: " + preco);
        }
        return preco.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s - R$ %.2f (%d em estoque)", codigo, nome, preco.doubleValue(), quantidadeEmEstoque);
    }
}
