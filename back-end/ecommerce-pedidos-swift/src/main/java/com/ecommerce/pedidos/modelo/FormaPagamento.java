package com.ecommerce.pedidos.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Base abstrata de todas as formas de pagamento (Aulas 06 a 08).
 *
 * <p>Guarda o que é comum (valor, data, situação) e obriga cada subclasse a
 * implementar {@link #processar()} e {@link #descreverMetodo()}. Quem usa um
 * pagamento conhece só este tipo abstrato: chama {@code processar()} e a
 * JVM executa a versão de {@code Pix}, {@code Boleto} ou {@code CartaoCredito}
 * (polimorfismo). Por isso o sistema não precisa de {@code instanceof} nem de
 * {@code switch} por tipo.</p>
 *
 * <p>A situação só muda pelos métodos de intenção ({@link #aprovar()},
 * {@link #recusar()}, {@link #estornar()}); não há setter público que permita
 * "aprovar" um pagamento de fora.</p>
 *
 * <p>Persistência com {@code InheritanceType.JOINED}: a tabela
 * {@code forma_pagamento} guarda os campos comuns e cada subclasse tem a sua
 * ({@code pix}, {@code boleto}...), ligadas pelo mesmo id. Ao buscar um
 * pagamento, o Hibernate faz o JOIN e devolve o objeto da subclasse certa.</p>
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class FormaPagamento implements ProcessadorPagamento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    @Column(nullable = false)
    private LocalDateTime dataDoPagamento;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private SituacaoPagamento situacao;

    protected FormaPagamento() {
        // exigido pelo JPA
    }

    protected FormaPagamento(BigDecimal valor) {
        definirValor(valor);
        this.dataDoPagamento = LocalDateTime.now();
        this.situacao = SituacaoPagamento.PENDENTE;
    }

    protected FormaPagamento(double valor) {
        this(BigDecimal.valueOf(valor));
    }

    public Long getId() {
        return id;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public LocalDateTime getDataDoPagamento() {
        return dataDoPagamento;
    }

    public SituacaoPagamento getSituacao() {
        return situacao;
    }

    /** Cada forma de pagamento implementa sua própria regra de aprovação. */
    public abstract boolean processar();

    /**
     * Texto curto que identifica o meio usado (ex.: "Visa •••• 1234").
     * Substitui a cadeia de {@code instanceof} que existia no controller.
     */
    public abstract String descreverMetodo();

    /**
     * Atributos próprios da subclasse (ex.: chave e tipo do Pix), na ordem em
     * que devem aparecer no JSON. Permite montar a resposta da API sem
     * perguntar "que tipo de pagamento é este?".
     */
    public abstract Map<String, Object> dadosEspecificos();

    /** Processa cobrando exatamente o valor informado (contrato de {@link ProcessadorPagamento}). */
    @Override
    public boolean processar(BigDecimal valor) {
        definirValor(valor);
        return processar();
    }

    /** Devolve o dinheiro de um pagamento aprovado (usado no cancelamento do pedido). */
    public void estornar() {
        if (situacao != SituacaoPagamento.APROVADO) {
            throw new IllegalStateException("Só é possível estornar pagamento aprovado. Situação atual: " + situacao);
        }
        this.situacao = SituacaoPagamento.ESTORNADO;
    }

    /** Marca o pagamento como aprovado agora. Usado pelas subclasses em {@code processar()}. */
    protected final void aprovar() {
        this.dataDoPagamento = LocalDateTime.now();
        this.situacao = SituacaoPagamento.APROVADO;
    }

    protected final void recusar() {
        this.situacao = SituacaoPagamento.RECUSADO;
    }

    private void definirValor(BigDecimal valor) {
        if (valor == null) {
            throw new IllegalArgumentException("Valor da forma de pagamento não pode ser nulo.");
        }
        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor da forma de pagamento deve ser maior que zero.");
        }
        this.valor = valor.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String getComprovante() {
        return String.format("%s | valor: %s | data: %s | status: %s",
                getDescricao(), getValor(), getDataDoPagamento(), getSituacao());
    }

    @Override
    public String getDescricao() {
        return getClass().getSimpleName();
    }

    @Override
    public boolean permiteParcelamento() {
        return false;
    }

    public String getResumo() {
        return String.format("%s no valor de R$ %s", getDescricao(), valor);
    }
}
