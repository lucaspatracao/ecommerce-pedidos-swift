package com.ecommerce.pedidos.servico;

import com.ecommerce.pedidos.dto.ProdutoRequest;
import com.ecommerce.pedidos.dto.ProdutoResponse;
import java.util.List;
import org.springframework.data.domain.Pageable;

/**
 * Casos de uso de produto (Aula 14).
 *
 * <p>Soft delete: o DELETE padrão só inativa o produto. Ele sai do catálogo,
 * mas os pedidos antigos continuam apontando para ele.</p>
 */
public interface ProdutoService {

    /** Produtos ativos, todos de uma vez. */
    default List<ProdutoResponse> listarTodos() {
        return listar(Visibilidade.ATIVOS, null, Pageable.unpaged());
    }

    /** @param nome trecho do nome (sem diferenciar maiúsculas); {@code null} = sem filtro */
    List<ProdutoResponse> listar(Visibilidade visibilidade, String nome, Pageable pagina);

    /** Produto ATIVO pelo código. @throws com.ecommerce.pedidos.excecao.RecursoNaoEncontradoException (404) */
    ProdutoResponse buscarPorCodigo(String codigo);

    /** @throws com.ecommerce.pedidos.excecao.ConflitoException (409) se o código já existir */
    ProdutoResponse cadastrar(ProdutoRequest request);

    /**
     * Atualiza todos os dados, inclusive de produto inativo (o administrador
     * pode corrigi-lo e reativá-lo). Se {@code request.versao()} vier e estiver
     * desatualizada, responde 409 em vez de sobrescrever a alteração de outro.
     */
    ProdutoResponse atualizar(String codigo, ProdutoRequest request);

    /** Atualização parcial: só o estoque de um produto ativo. */
    ProdutoResponse atualizarEstoque(String codigo, int quantidade);

    /**
     * {@code definitivo = false}: soft delete (inativa).
     * {@code definitivo = true}: apaga de fato, só se o produto nunca foi vendido (409 se já foi).
     */
    void excluir(String codigo, boolean definitivo);

    void inativar(String codigo);

    /** 404 se não houver produto INATIVO com o código. */
    ProdutoResponse reativar(String codigo);

    /** {@code ativo = false} inativa; {@code true} reativa. Devolve o produto já atualizado. */
    ProdutoResponse alterarAtivo(String codigo, boolean ativo);

    /** Quantidade de produtos ativos. */
    long contar();

    /** Produtos ativos com estoque abaixo de {@code limite}, para o alerta do dashboard. */
    List<ProdutoResponse> listarEstoqueCritico(int limite);
}
