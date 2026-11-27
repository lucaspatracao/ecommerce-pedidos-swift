package com.ecommerce.pedidos.modelo;

import com.ecommerce.pedidos.util.Utils;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * Endereço do cliente. É imutável: para "mudar" o endereço, o cliente
 * recebe um novo objeto {@code Endereco}.
 *
 * <p>{@code @Embeddable}: não tem tabela nem id próprios. Suas colunas
 * ficam dentro da tabela {@code cliente}.</p>
 */
@Embeddable
public class Endereco {
    static final String NAO_INFORMADO = "Não informado";

    // Sem "final" só porque o JPA precisa preencher os campos ao ler do banco;
    // não há setters, então continuam imutáveis para o resto do código.
    @Column(length = 200)
    private String logradouro;

    @Column(length = 100)
    private String cidade;

    @Column(length = 20)
    private String cep;

    protected Endereco() {
        // exigido pelo JPA
    }

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
