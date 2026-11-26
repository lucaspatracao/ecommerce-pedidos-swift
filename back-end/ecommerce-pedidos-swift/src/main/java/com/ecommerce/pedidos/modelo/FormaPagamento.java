package com.ecommerce.pedidos.modelo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

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
 */
public abstract class FormaPagamento implements ProcessadorPagamento {
    private BigDecimal valor;
    private LocalDateTime dataDoPagamento;
    private SituacaoPagamento situacao;

    protected FormaPagamento(BigDecimal valor) {
        definirValor(valor);
        this.dataDoPagamento = LocalDateTime.now();
        this.situacao = SituacaoPagamento.PENDENTE;
    }

    protected FormaPagamento(double valor) {
        this(BigDecimal.valueOf(valor));
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
