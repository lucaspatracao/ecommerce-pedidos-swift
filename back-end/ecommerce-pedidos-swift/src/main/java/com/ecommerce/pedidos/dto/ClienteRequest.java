package com.ecommerce.pedidos.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Corpo de POST/PUT {@code /api/clientes}. O endereço chega como texto livre.
 *
 * <p>Bean Validation (Aula 14) barra o básico logo na entrada, antes de
 * chegar ao serviço: campo vazio, e-mail sem formato, telefone com letras.
 * A regra de negócio mais forte (dígitos verificadores do CPF) continua no
 * domínio, em {@code Utils.validarCpf}, porque vale para qualquer origem e
 * não só para a API.</p>
 */
public record ClienteRequest(
    @NotBlank(message = "Nome é obrigatório.")
    @Size(max = 120, message = "Nome deve ter no máximo 120 caracteres.")
    String nome,

    // Obrigatório só no cadastro (o domínio valida os dígitos); no PUT o CPF da rota identifica o cliente.
    String cpf,

    @NotBlank(message = "E-mail é obrigatório.")
    @Email(message = "E-mail inválido.")
    String email,

    @NotBlank(message = "Telefone é obrigatório.")
    @Pattern(regexp = "\\d{8,20}", message = "Telefone deve conter apenas dígitos (8 a 20).")
    String telefone,

    @Size(max = 200, message = "Endereço deve ter no máximo 200 caracteres.")
    String endereco
) {
}
