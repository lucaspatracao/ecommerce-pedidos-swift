package com.ecommerce.pedidos.excecao;

public class PagamentoRecusadoException extends ECommerceException {
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
