package com.ecommerce.pedidos.excecao;

/** A forma de pagamento (ou o gateway) recusou a cobrança. Vira HTTP 409. */
public class PagamentoRecusadoException extends ConflitoException {
    private final String formaPagamento;
    private final String motivo;

    public PagamentoRecusadoException(String formaPagamento, String motivo) {
        super("Pagamento por " + formaPagamento + " recusado: " + motivo);
        this.formaPagamento = formaPagamento;
        this.motivo = motivo;
    }

    public String getFormaPagamento() {
        return formaPagamento;
    }

    public String getMotivo() {
        return motivo;
    }
}
