package com.ecommerce.pedidos.util;

import java.util.regex.Pattern;

/**
 * Validações reutilizáveis do domínio (Aula 03).
 *
 * <p>É {@code final} (ninguém herda) e tem construtor privado (ninguém instancia):
 * só faz sentido chamar os métodos estáticos, como em {@code Math}.
 * As entidades usam estes métodos em vez de repetir a mesma validação em
 * cada classe.</p>
 */
public final class Utils {

    /**
     * Formato simples de e-mail: algo@dominio.extensao, sem espaços.
     * Não tenta cobrir toda a RFC 5322; o objetivo é barrar erros de digitação.
     */
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private static final int TAMANHO_CPF = 11;

    private Utils() {
        throw new UnsupportedOperationException("Classe utilitária: use os métodos estáticos.");
    }

    /**
     * Garante que o texto foi informado e devolve-o sem espaços nas pontas.
     *
     * @throws IllegalArgumentException se o valor for nulo ou só espaços
     */
    public static String textoObrigatorio(String valor, String nomeCampo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(nomeCampo + " é obrigatório.");
        }
        return valor.trim();
    }

    /** Remove tudo que não for dígito (pontos, traços, espaços). */
    public static String apenasDigitos(String valor) {
        return valor == null ? "" : valor.replaceAll("\\D", "");
    }

    /**
     * Indica se o CPF é válido pelos dígitos verificadores.
     * Aceita o CPF com ou sem máscara ("529.982.247-25" ou "52998224725").
     */
    public static boolean isCpfValido(String cpf) {
        String digitos = apenasDigitos(cpf);
        if (digitos.length() != TAMANHO_CPF) {
            return false;
        }
        // "11111111111" passa na conta dos dígitos, mas não é um CPF válido.
        if (digitos.chars().distinct().count() == 1) {
            return false;
        }
        return calcularDigito(digitos, 9) == digitos.charAt(9) - '0'
            && calcularDigito(digitos, 10) == digitos.charAt(10) - '0';
    }

    /**
     * Valida o CPF e o devolve normalizado (só os 11 dígitos), que é a forma
     * guardada no banco e usada nas rotas da API.
     *
     * @throws IllegalArgumentException se o CPF for vazio ou inválido
     */
    public static String validarCpf(String cpf) {
        String informado = textoObrigatorio(cpf, "CPF");
        if (!isCpfValido(informado)) {
            throw new IllegalArgumentException("CPF inválido: " + informado);
        }
        return apenasDigitos(informado);
    }

    /** Indica se o e-mail tem o formato algo@dominio.extensao. */
    public static boolean isEmailValido(String email) {
        return email != null && EMAIL.matcher(email.trim()).matches();
    }

    /**
     * Valida o e-mail e o devolve sem espaços nas pontas.
     *
     * @throws IllegalArgumentException se o e-mail for vazio ou mal formado
     */
    public static String validarEmail(String email) {
        String informado = textoObrigatorio(email, "E-mail");
        if (!isEmailValido(informado)) {
            throw new IllegalArgumentException("E-mail inválido: " + informado);
        }
        return informado;
    }

    /**
     * Cálculo do dígito verificador do CPF (módulo 11).
     *
     * <p>Multiplica os {@code quantidade} primeiros dígitos por pesos
     * decrescentes, começando em {@code quantidade + 1}. O dígito é
     * {@code (soma * 10) % 11}, e o resultado 10 vira 0.</p>
     */
    private static int calcularDigito(String digitos, int quantidade) {
        int soma = 0;
        for (int i = 0; i < quantidade; i++) {
            soma += (digitos.charAt(i) - '0') * (quantidade + 1 - i);
        }
        int resto = (soma * 10) % 11;
        return resto == 10 ? 0 : resto;
    }
}
