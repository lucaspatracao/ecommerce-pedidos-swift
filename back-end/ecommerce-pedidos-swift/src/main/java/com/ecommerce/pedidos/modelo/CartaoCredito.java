package com.ecommerce.pedidos.modelo;

import com.ecommerce.pedidos.util.Utils;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/** Pagamento com cartão: é o único que tem limite e permite parcelar. */
@Entity
public class CartaoCredito extends FormaPagamento {
    private static final BigDecimal LIMITE_PADRAO = new BigDecimal("5000.00");
    private static final int MAXIMO_PARCELAS = 12;

    @Column(nullable = false, length = 30)
    private String numeroMascarado;

    @Column(nullable = false, length = 30)
    private String bandeira;

    @Column(nullable = false)
    private int quantidadeDeParcelas;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal limiteDisponivel;

    protected CartaoCredito() {
        // exigido pelo JPA
    }

    public CartaoCredito(BigDecimal valor, String numeroMascarado, String bandeira, int quantidadeDeParcelas) {
        super(valor);
        this.numeroMascarado = Utils.textoObrigatorio(numeroMascarado, "Número mascarado");
        this.bandeira = Utils.textoObrigatorio(bandeira, "Bandeira");
        if (quantidadeDeParcelas <= 0 || quantidadeDeParcelas > MAXIMO_PARCELAS) {
            throw new IllegalArgumentException("Quantidade de parcelas deve estar entre 1 e 12");
        }
        this.quantidadeDeParcelas = quantidadeDeParcelas;
        this.limiteDisponivel = LIMITE_PADRAO;
    }

    public String getNumeroMascarado() {
        return numeroMascarado;
    }

    public String getBandeira() {
        return bandeira;
    }

    public int getQuantidadeDeParcelas() {
        return quantidadeDeParcelas;
    }

    public BigDecimal getLimiteDisponivel() {
        return limiteDisponivel;
    }

    /** Recusa se o valor passar do limite; caso contrário, aprova e consome o limite. */
    @Override
    public boolean processar() {
        if (getValor().compareTo(limiteDisponivel) > 0) {
            recusar();
            return false;
        }
        aprovar();
        limiteDisponivel = limiteDisponivel.subtract(getValor());
        return true;
    }

    @Override
    public String descreverMetodo() {
        return bandeira + " •••• " + numeroMascarado;
    }

    @Override
    public Map<String, Object> dadosEspecificos() {
        Map<String, Object> dados = new LinkedHashMap<>();
        dados.put("numeroMascarado", numeroMascarado);
        dados.put("bandeira", bandeira);
        dados.put("quantidadeDeParcelas", quantidadeDeParcelas);
        dados.put("limiteDisponivel", limiteDisponivel);
        return dados;
    }

    @Override
    public boolean permiteParcelamento() {
        return true;
    }

    @Override
    public String getComprovante() {
        return "COMPROVANTE CARTAO | bandeira=" + bandeira + " | numero=" + numeroMascarado
                + " | parcelas=" + quantidadeDeParcelas + " | valor=R$ " + getValor() + " | data=" + getDataDoPagamento();
    }

    @Override
    public String getDescricao() {
        return "CartaoCredito";
    }

    @Override
    public String getResumo() {
        return super.getResumo() + " (cartão " + bandeira + ", parcelas " + quantidadeDeParcelas + ")";
    }
}
