package com.ecommerce.pedidos.servico;

import com.ecommerce.pedidos.dto.ProdutoRequest;
import com.ecommerce.pedidos.dto.ProdutoResponse;
import java.util.List;

/** Casos de uso de produto (Aula 14). */
public interface ProdutoService {

    List<ProdutoResponse> listarTodos();

    /** @throws com.ecommerce.pedidos.excecao.RecursoNaoEncontradoException (404) */
    ProdutoResponse buscarPorCodigo(String codigo);

    /** @throws com.ecommerce.pedidos.excecao.ConflitoException (409) se o código já existir */
    ProdutoResponse cadastrar(ProdutoRequest request);

    ProdutoResponse atualizar(String codigo, ProdutoRequest request);

    /** @throws com.ecommerce.pedidos.excecao.ConflitoException (409) se o produto já foi vendido */
    void excluir(String codigo);

    long contar();

    /** Produtos com estoque abaixo de {@code limite}, para o alerta do dashboard. */
    List<ProdutoResponse> listarEstoqueCritico(int limite);
}
