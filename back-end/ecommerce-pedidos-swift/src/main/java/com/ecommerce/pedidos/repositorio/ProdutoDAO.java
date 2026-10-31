package com.ecommerce.pedidos.repositorio;

import com.ecommerce.pedidos.modelo.Produto;
import com.ecommerce.pedidos.util.ConexaoFactory;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProdutoDAO {
    public void salvar(Produto produto) throws SQLException {
        String sql = "INSERT INTO produto (codigo, nome, descricao, preco, quantidade_em_estoque, ativo) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = ConexaoFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, produto.getCodigo());
            statement.setString(2, produto.getNome());
            statement.setString(3, produto.getDescricao());
            statement.setBigDecimal(4, produto.getPreco());
            statement.setInt(5, produto.getQuantidadeEmEstoque());
            statement.setBoolean(6, produto.isAtivo());
            statement.executeUpdate();
        }
    }

    public List<Produto> listarTodos() throws SQLException {
        String sql = "SELECT codigo, nome, descricao, preco, quantidade_em_estoque, ativo FROM produto";
        List<Produto> produtos = new ArrayList<>();

        try (Connection connection = ConexaoFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                Produto produto = new Produto(
                    resultSet.getString("codigo"),
                    resultSet.getString("nome"),
                    resultSet.getBigDecimal("preco"),
                    resultSet.getInt("quantidade_em_estoque")
                );
                produto.setDescricao(resultSet.getString("descricao"));
                produto.setAtivo(resultSet.getBoolean("ativo"));
                produtos.add(produto);
            }
        }

        return produtos;
    }
}
