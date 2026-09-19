package com.ecommerce.pedidos.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Categoria {
    private final String nome;
    private final List<Produto> produtos = new ArrayList<>();

    public Categoria(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome da categoria é obrigatório");
        }
        this.nome = nome.trim();
    }

    public String getNome() {
        return nome;
    }

    public void incluir(Produto produto) {
        if (produto == null) {
            throw new IllegalArgumentException("Produto é obrigatório");
        }
        if (!produtos.contains(produto)) {
            produtos.add(produto);
        }
    }

    public boolean remover(Produto produto) {
        return produtos.remove(produto);
    }

    public List<Produto> getProdutos() {
        return Collections.unmodifiableList(new ArrayList<>(produtos));
    }
}