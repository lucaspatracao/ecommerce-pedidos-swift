package com.ecommerce.pedidos.service;

import com.ecommerce.pedidos.model.Produto;
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

    public Produto buscarPorCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("Código do produto não pode ser vazio.");
        }
        return produtos.get(codigo.trim());
    }

    public Produto salvar(Produto produto) {
        Objects.requireNonNull(produto, "Produto não pode ser nulo.");
        produtos.put(produto.getCodigo(), produto);
        return produto;
    }

    public Produto atualizar(String codigo, Produto produtoAtualizado) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("Código do produto não pode ser vazio.");
        }
        Objects.requireNonNull(produtoAtualizado, "Produto atualizado não pode ser nulo.");

        Produto existente = produtos.get(codigo.trim());
        if (existente == null) {
            throw new IllegalArgumentException("Produto não encontrado.");
        }

        String novoCodigo = produtoAtualizado.getCodigo();
        if (!codigo.equalsIgnoreCase(novoCodigo)) {
            produtos.remove(codigo.trim());
        }

        produtos.put(novoCodigo, produtoAtualizado);
        return produtoAtualizado;
    }

    public boolean excluir(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("Código do produto não pode ser vazio.");
        }
        return produtos.remove(codigo.trim()) != null;
    }

    public long contar() {
        return produtos.size();
    }
}
