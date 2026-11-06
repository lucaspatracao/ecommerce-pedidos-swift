package com.ecommerce.pedidos.repositorio.mysql;

import com.ecommerce.pedidos.modelo.Cliente;
import com.ecommerce.pedidos.modelo.Endereco;
import com.ecommerce.pedidos.modelo.Produto;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Conversão de linhas do banco em objetos de domínio. O prefixo permite
 * reaproveitar o mapeamento em consultas com JOIN (ex.: "produto_id").
 */
final class Mapeadores {
    static final String COLUNAS_PRODUTO =
        "%1$s.id AS %2$sid, %1$s.codigo AS %2$scodigo, %1$s.nome AS %2$snome, "
            + "%1$s.descricao AS %2$sdescricao, %1$s.preco AS %2$spreco, "
            + "%1$s.quantidade_em_estoque AS %2$squantidade_em_estoque, "
            + "%1$s.ativo AS %2$sativo, %1$s.versao AS %2$sversao";

    static final String COLUNAS_CLIENTE =
        "%1$s.id AS %2$sid, %1$s.nome AS %2$snome, %1$s.cpf AS %2$scpf, "
            + "%1$s.email AS %2$semail, %1$s.telefone AS %2$stelefone, "
            + "%1$s.logradouro AS %2$slogradouro, %1$s.cidade AS %2$scidade, "
            + "%1$s.cep AS %2$scep, %1$s.ativo AS %2$sativo";

    private Mapeadores() {
    }

    /** Lista de colunas de produto para o SELECT, com alias de tabela e prefixo. */
    static String colunasProduto(String alias, String prefixo) {
        return String.format(COLUNAS_PRODUTO, alias, prefixo);
    }

    static String colunasCliente(String alias, String prefixo) {
        return String.format(COLUNAS_CLIENTE, alias, prefixo);
    }

    static Produto produto(ResultSet resultado, String prefixo) throws SQLException {
        Produto produto = new Produto(
            resultado.getString(prefixo + "codigo"),
            resultado.getString(prefixo + "nome"),
            resultado.getBigDecimal(prefixo + "preco"),
            resultado.getInt(prefixo + "quantidade_em_estoque")
        );
        produto.setId(resultado.getLong(prefixo + "id"));
        produto.setDescricao(resultado.getString(prefixo + "descricao"));
        produto.setAtivo(resultado.getBoolean(prefixo + "ativo"));
        produto.setVersao(resultado.getInt(prefixo + "versao"));
        return produto;
    }

    static Cliente cliente(ResultSet resultado, String prefixo) throws SQLException {
        String logradouro = resultado.getString(prefixo + "logradouro");
        Endereco endereco = logradouro == null ? null : new Endereco(
            logradouro,
            resultado.getString(prefixo + "cidade"),
            resultado.getString(prefixo + "cep")
        );
        Cliente cliente = new Cliente(
            resultado.getString(prefixo + "nome"),
            resultado.getString(prefixo + "cpf"),
            resultado.getString(prefixo + "email"),
            resultado.getString(prefixo + "telefone"),
            endereco
        );
        cliente.setId(resultado.getLong(prefixo + "id"));
        cliente.setAtivo(resultado.getBoolean(prefixo + "ativo"));
        return cliente;
    }

    /** Escapa curingas do LIKE para que o texto digitado seja buscado literalmente. */
    static String contendo(String trecho) {
        String escapado = trecho.trim()
            .replace("\\", "\\\\")
            .replace("%", "\\%")
            .replace("_", "\\_");
        return "%" + escapado + "%";
    }
}
