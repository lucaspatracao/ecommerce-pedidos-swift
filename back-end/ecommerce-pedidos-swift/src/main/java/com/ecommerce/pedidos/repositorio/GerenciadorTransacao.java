package com.ecommerce.pedidos.repositorio;

/**
 * Abstração da unidade de trabalho. Os serviços dependem apenas desta
 * interface: abrem um bloco e repassam a conexão recebida para cada
 * repositório envolvido.
 */
public interface GerenciadorTransacao {

    /**
     * Executa o bloco em uma transação: commit se terminar normalmente,
     * rollback completo se qualquer exceção (verificada ou não) escapar.
     */
    <T, E extends Exception> T executarEmTransacao(OperacaoComConexao<T, E> operacao) throws E;

    /** Executa o bloco em modo autocommit (consultas simples). */
    <T, E extends Exception> T executar(OperacaoComConexao<T, E> operacao) throws E;
}
