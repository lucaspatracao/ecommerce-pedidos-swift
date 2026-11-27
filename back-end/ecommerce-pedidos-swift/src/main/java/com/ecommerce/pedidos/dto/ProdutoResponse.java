package com.ecommerce.pedidos.dto;

import com.ecommerce.pedidos.modelo.Produto;
import java.math.BigDecimal;

/** JSON de produto devolvido pela API (mesmos campos de antes da persistência). */
public record ProdutoResponse(
    String codigo,
    String nome,
    String descricao,
    BigDecimal preco,
    int quantidadeEmEstoque,
    boolean ativo
) {

    public static ProdutoResponse de(Produto produto) {
        return new ProdutoResponse(
            produto.getCodigo(),
            produto.getNome(),
            produto.getDescricao(),
            produto.getPreco(),
            produto.getQuantidadeEmEstoque(),
            produto.isAtivo()
        );
    }
}
