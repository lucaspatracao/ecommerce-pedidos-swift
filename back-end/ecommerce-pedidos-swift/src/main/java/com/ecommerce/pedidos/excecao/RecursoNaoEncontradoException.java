package com.ecommerce.pedidos.excecao;

/** O recurso pedido (cliente, produto, pedido...) não existe. Vira HTTP 404. */
public class RecursoNaoEncontradoException extends NegocioException {
    private final String recurso;
    private final String identificador;

    public RecursoNaoEncontradoException(String recurso, String identificador) {
        super(recurso + " não encontrado: " + identificador);
        this.recurso = recurso;
        this.identificador = identificador;
    }

    public String getRecurso() {
        return recurso;
    }

    public String getIdentificador() {
        return identificador;
    }
}
