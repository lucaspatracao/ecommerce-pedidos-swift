package com.ecommerce.pedidos.repositorio;

/**
 * Erro de persistência próprio e tipado. Toda falha vinda do JDBC/MySQL é
 * traduzida para esta exceção dentro da camada de repositório, para que
 * serviços e controllers nunca recebam um {@link java.sql.SQLException} cru
 * (com nome de tabela, constraint ou detalhes do driver).
 *
 * O {@link Tipo} faz o papel dos "cases" de um enum de erro: quem trata a
 * exceção decide a reação (ex.: HTTP 404 ou 409) olhando apenas o tipo.
 */
public class RepositorioException extends RuntimeException {

    public enum Tipo {
        /** O registro procurado/alterado não existe (zero linhas afetadas). */
        NAO_ENCONTRADO,
        /** Dados inválidos para a operação (ex.: atualizar objeto sem id). */
        VALIDACAO,
        /** Violação de chave estrangeira (ex.: excluir cliente com pedidos). */
        VIOLACAO_INTEGRIDADE,
        /** Violação de chave única (ex.: CPF ou código já cadastrado). */
        DUPLICADO,
        /** Outra operação alterou o registro antes (controle otimista). */
        CONFLITO_CONCORRENCIA,
        /** Falha técnica inesperada do banco (conexão, SQL inválido etc.). */
        FALHA_TECNICA
    }

    private final Tipo tipo;

    public RepositorioException(Tipo tipo, String mensagem, Throwable causa) {
        super(mensagem, causa);
        this.tipo = tipo;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public static RepositorioException naoEncontrado(String entidade, Object identificador) {
        return new RepositorioException(Tipo.NAO_ENCONTRADO,
            entidade + " não encontrado(a): " + identificador, null);
    }

    public static RepositorioException validacao(String mensagem) {
        return new RepositorioException(Tipo.VALIDACAO, mensagem, null);
    }

    public static RepositorioException violacaoIntegridade(String mensagemDeNegocio, Throwable causa) {
        return new RepositorioException(Tipo.VIOLACAO_INTEGRIDADE, mensagemDeNegocio, causa);
    }

    public static RepositorioException duplicado(String mensagemDeNegocio, Throwable causa) {
        return new RepositorioException(Tipo.DUPLICADO, mensagemDeNegocio, causa);
    }

    public static RepositorioException conflitoConcorrencia(String entidade, Object identificador) {
        return new RepositorioException(Tipo.CONFLITO_CONCORRENCIA,
            entidade + " " + identificador + " foi alterado(a) por outra operação. "
                + "Recarregue os dados e tente novamente.", null);
    }

    public static RepositorioException falhaTecnica(String mensagem, Throwable causa) {
        return new RepositorioException(Tipo.FALHA_TECNICA, mensagem, causa);
    }
}
