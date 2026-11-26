package com.ecommerce.pedidos.modelo;

import com.ecommerce.pedidos.util.Utils;

/**
 * Endereço do cliente. É imutável: para "mudar" o endereço, o cliente
 * recebe um novo objeto {@code Endereco}.
 */
public class Endereco {
    static final String NAO_INFORMADO = "Não informado";

    private final String logradouro;
    private final String cidade;
    private final String cep;

    public Endereco(String logradouro, String cidade, String cep) {
        this.logradouro = Utils.textoObrigatorio(logradouro, "Logradouro");
        this.cidade = Utils.textoObrigatorio(cidade, "Cidade");
        this.cep = Utils.textoObrigatorio(cep, "CEP");
    }

    /**
     * Cria o endereço a partir do texto livre enviado pelo front-end.
     * Texto vazio significa "sem endereço" e devolve {@code null}.
     */
    public static Endereco aPartirDoTexto(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        return new Endereco(texto, NAO_INFORMADO, NAO_INFORMADO);
    }

    public String getLogradouro() {
        return logradouro;
    }

    public String getCidade() {
        return cidade;
    }

    public String getCep() {
        return cep;
    }

    @Override
    public String toString() {
        return String.format("%s, %s - CEP: %s", logradouro, cidade, cep);
    }
}
