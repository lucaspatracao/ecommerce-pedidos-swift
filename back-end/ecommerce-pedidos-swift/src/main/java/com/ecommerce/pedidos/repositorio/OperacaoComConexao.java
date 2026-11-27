package com.ecommerce.pedidos.repositorio;

import java.sql.Connection;

/**
 * Bloco de código que recebe a conexão compartilhada. Todos os repositórios
 * chamados dentro do bloco devem usar ESSA conexão, para participarem da
 * mesma transação.
 *
 * @param <T> tipo do resultado
 * @param <E> exceção verificada que o bloco pode lançar (ex.: estoque insuficiente)
 */
@FunctionalInterface
public interface OperacaoComConexao<T, E extends Exception> {
    T executar(Connection conexao) throws E;
}
