package com.ecommerce.pedidos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * Corpo de POST/PUT {@code /api/produtos}.
 *
 * <p>O front envia o preço como texto ({@code "49.90"}); o Jackson converte
 * texto numérico para {@code BigDecimal} sozinho, então o contrato não muda.</p>
 */
public record ProdutoRequest(
    @NotBlank(message = "Código do produto é obrigatório.")
    @Size(min = 3, max = 40, message = "Código do produto deve ter entre 3 e 40 caracteres.")
    String codigo,

    @NotBlank(message = "Nome do produto é obrigatório.")
    @Size(max = 120, message = "Nome do produto deve ter no máximo 120 caracteres.")
    String nome,

    @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres.")
    String descricao,

    @NotNull(message = "Preço do produto é obrigatório.")
    @Positive(message = "Preço do produto deve ser maior que zero.")
    BigDecimal preco,

    // Obrigatório no cadastro (o serviço confere); no PUT, ausente = mantém o estoque atual.
    @PositiveOrZero(message = "Estoque do produto não pode ser negativo.")
    Integer estoque,

    Boolean disponivel,

    // Versão lida pelo cliente (controle otimista). Ausente = sem checagem.
    Long versao
) {

    /** Sem controle de versão (cadastro e chamadas internas). */
    public ProdutoRequest(String codigo, String nome, String descricao, BigDecimal preco,
                          Integer estoque, Boolean disponivel) {
        this(codigo, nome, descricao, preco, estoque, disponivel, null);
    }
}
