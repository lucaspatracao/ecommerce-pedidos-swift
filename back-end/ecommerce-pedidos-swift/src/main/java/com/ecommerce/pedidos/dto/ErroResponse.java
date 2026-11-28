package com.ecommerce.pedidos.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

/**
 * Corpo padronizado de TODA resposta de erro da API (Aula 15).
 *
 * <pre>
 * {
 *   "status": 409,
 *   "erro": "Conflito",
 *   "mensagem": "Estoque insuficiente de Camiseta: disponível 2, solicitado 5",
 *   "caminho": "/api/pedidos",
 *   "campos": [ { "campo": "email", "mensagem": "E-mail inválido." } ]   (só em erro de validação)
 * }
 * </pre>
 *
 * @param status   o mesmo código da linha de status HTTP
 * @param erro     rótulo do status em português ("Conflito", "Não encontrado"...)
 * @param mensagem texto de negócio, pronto para mostrar ao usuário
 * @param caminho  endereço da requisição que falhou
 * @param campos   campos inválidos; omitido do JSON quando não há erro de validação
 */
public record ErroResponse(
    int status,
    String erro,
    String mensagem,
    String caminho,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    List<CampoInvalido> campos
) {
    public ErroResponse(int status, String erro, String mensagem, String caminho) {
        this(status, erro, mensagem, caminho, null);
    }
}
