package com.ecommerce.pedidos.servico;

import com.ecommerce.pedidos.excecao.ConflitoException;
import com.ecommerce.pedidos.excecao.RecursoNaoEncontradoException;
import com.ecommerce.pedidos.modelo.Produto;
import com.ecommerce.pedidos.util.Utils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class ProdutoService {
    private final Map<String, Produto> produtos = new ConcurrentHashMap<>();

    public List<Produto> listarTodos() {
        return new ArrayList<>(produtos.values());
    }

    /** @throws RecursoNaoEncontradoException (404) se o código não existir */
    public Produto buscarPorCodigo(String codigo) {
        String chave = Utils.textoObrigatorio(codigo, "Código do produto");
        Produto produto = produtos.get(chave);
        if (produto == null) {
            throw new RecursoNaoEncontradoException("Produto", chave);
        }
        return produto;
    }

    /** @throws ConflitoException (409) se o código já estiver em uso */
    public Produto salvar(Produto produto) {
        Objects.requireNonNull(produto, "Produto não pode ser nulo.");
        if (produtos.putIfAbsent(produto.getCodigo(), produto) != null) {
            throw new ConflitoException("Já existe produto com o código " + produto.getCodigo());
        }
        return produto;
    }

    public Produto atualizar(String codigo, Produto dados) {
        Objects.requireNonNull(dados, "Produto atualizado não pode ser nulo.");
        Produto existente = buscarPorCodigo(codigo);
        String novoCodigo = dados.getCodigo();
        if (!existente.getCodigo().equals(novoCodigo)) {
            if (produtos.containsKey(novoCodigo)) {
                throw new ConflitoException("Já existe produto com o código " + novoCodigo);
            }
            produtos.remove(existente.getCodigo());
            existente.alterarCodigo(novoCodigo);
            produtos.put(novoCodigo, existente);
        }
        existente.atualizarDados(dados.getNome(), dados.getDescricao(), dados.getPreco());
        existente.definirEstoque(dados.getQuantidadeEmEstoque());
        existente.definirDisponibilidade(dados.isAtivo());
        return existente;
    }

    public void excluir(String codigo) {
        Produto existente = buscarPorCodigo(codigo);
        produtos.remove(existente.getCodigo());
    }

    public long contar() {
        return produtos.size();
    }
}
