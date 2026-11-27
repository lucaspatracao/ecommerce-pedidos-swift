package com.ecommerce.pedidos.servico;

import com.ecommerce.pedidos.modelo.Produto;
import com.ecommerce.pedidos.repositorio.GerenciadorTransacao;
import com.ecommerce.pedidos.repositorio.Pagina;
import com.ecommerce.pedidos.repositorio.ProdutoRepository;
import com.ecommerce.pedidos.repositorio.RepositorioException;
import com.ecommerce.pedidos.repositorio.Visibilidade;
import java.sql.Connection;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;

/**
 * Regras de catálogo. Depende apenas de interfaces (repositório e
 * transação), nunca da implementação MySQL nem de SQL.
 */
@Service
public class ProdutoService {
    private final ProdutoRepository produtoRepository;
    private final GerenciadorTransacao transacao;

    public ProdutoService(ProdutoRepository produtoRepository, GerenciadorTransacao transacao) {
        this.produtoRepository = produtoRepository;
        this.transacao = transacao;
    }

    /** Somente produtos ativos (consulta padrão). */
    public List<Produto> listarTodos() {
        return listar(Visibilidade.ATIVOS, Pagina.todas());
    }

    public List<Produto> listar(Visibilidade visibilidade, Pagina pagina) {
        return transacao.executar(conexao -> produtoRepository.listar(conexao, visibilidade, pagina));
    }

    public List<Produto> buscarPorNome(String nome) {
        return transacao.executar(conexao -> produtoRepository.buscarPorNome(conexao, nome));
    }

    /** Busca um produto ativo pelo código; lança NAO_ENCONTRADO se não existir. */
    public Produto buscarPorCodigo(String codigo) {
        validarCodigo(codigo);
        return transacao.executar(conexao -> produtoRepository.buscarPorCodigo(conexao, codigo)
            .orElseThrow(() -> RepositorioException.naoEncontrado("Produto", codigo)));
    }

    public Produto salvar(Produto produto) {
        Objects.requireNonNull(produto, "Produto não pode ser nulo.");
        if (produto.getId() != null) {
            throw new IllegalArgumentException("Novo produto não deve informar id.");
        }
        return transacao.executarEmTransacao(conexao -> produtoRepository.salvar(conexao, produto));
    }

    /**
     * Atualiza todos os dados do produto. A busca usa {@link Visibilidade#TODOS}
     * de propósito: o administrador pode editar (e reativar) um produto inativo.
     *
     * @param versaoInformada versão lida pelo cliente da API; se informada, o
     *                        UPDATE falha com conflito caso outra operação tenha
     *                        alterado o produto nesse meio-tempo
     */
    public Produto atualizar(String codigo, Produto produtoAtualizado, Integer versaoInformada) {
        validarCodigo(codigo);
        Objects.requireNonNull(produtoAtualizado, "Produto atualizado não pode ser nulo.");
        return transacao.executarEmTransacao(conexao -> {
            Produto existente = buscarExistente(conexao, codigo, Visibilidade.TODOS);
            produtoAtualizado.setId(existente.getId());
            produtoAtualizado.setVersao(versaoInformada != null ? versaoInformada : existente.getVersao());
            return produtoRepository.atualizar(conexao, produtoAtualizado);
        });
    }

    public Produto atualizar(String codigo, Produto produtoAtualizado) {
        return atualizar(codigo, produtoAtualizado, null);
    }

    /** Atualização parcial: altera apenas o estoque. */
    public Produto atualizarEstoque(String codigo, int novaQuantidade) {
        validarCodigo(codigo);
        if (novaQuantidade < 0) {
            throw new IllegalArgumentException("Estoque não pode ser negativo.");
        }
        return transacao.executarEmTransacao(conexao -> {
            Produto existente = buscarExistente(conexao, codigo, Visibilidade.ATIVOS);
            produtoRepository.atualizarEstoque(conexao, existente.getId(), novaQuantidade);
            return buscarExistente(conexao, codigo, Visibilidade.ATIVOS);
        });
    }

    /** Soft delete: o produto sai do catálogo, mas os pedidos antigos continuam íntegros. */
    public void inativar(String codigo) {
        validarCodigo(codigo);
        transacao.executarEmTransacao(conexao -> {
            produtoRepository.inativar(conexao, buscarExistente(conexao, codigo, Visibilidade.ATIVOS).getId());
            return null;
        });
    }

    public Produto reativar(String codigo) {
        validarCodigo(codigo);
        return transacao.executarEmTransacao(conexao -> {
            Produto inativo = buscarExistente(conexao, codigo, Visibilidade.INATIVOS);
            produtoRepository.reativar(conexao, inativo.getId());
            return buscarExistente(conexao, codigo, Visibilidade.ATIVOS);
        });
    }

    /** DELETE padrão da API: produto nunca é apagado de fato, apenas inativado. */
    public void excluir(String codigo) {
        inativar(codigo);
    }

    /**
     * Exclusão definitiva, permitida apenas para produto nunca vendido. Se
     * houver pedidos com o produto, o banco recusa (RESTRICT) e o repositório
     * devolve VIOLACAO_INTEGRIDADE com mensagem de negócio.
     */
    public void excluirDefinitivamente(String codigo) {
        validarCodigo(codigo);
        transacao.executarEmTransacao(conexao -> {
            produtoRepository.remover(conexao, buscarExistente(conexao, codigo, Visibilidade.TODOS).getId());
            return null;
        });
    }

    public long contar() {
        return transacao.executar(produtoRepository::contar);
    }

    private Produto buscarExistente(Connection conexao, String codigo, Visibilidade visibilidade) {
        return produtoRepository.buscarPorCodigo(conexao, codigo, visibilidade)
            .orElseThrow(() -> RepositorioException.naoEncontrado("Produto", codigo));
    }

    private void validarCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("Código do produto não pode ser vazio.");
        }
    }
}
