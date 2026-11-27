package com.ecommerce.pedidos.dto;

import com.ecommerce.pedidos.modelo.Cliente;

/**
 * JSON de cliente devolvido pela API.
 *
 * <p>Por que DTO e não a entidade direto? A entidade agora tem id, relações
 * preguiçosas (LAZY) e anotações JPA. Serializá-la exporia detalhes do banco
 * e poderia disparar consultas fora da transação. O record fixa o contrato
 * com o front-end.</p>
 *
 * <p>Sem campos duplicados: {@code documento} repetia o {@code cpf} e
 * {@code identificacao} repetia o {@code resumo}. Ficou um de cada par.
 * {@code ativo} indica o soft delete.</p>
 */
public record ClienteResponse(
    String nome,
    String email,
    String telefone,
    EnderecoResponse endereco,
    String cpf,
    String resumo,
    boolean ativo
) {

    public static ClienteResponse de(Cliente cliente) {
        return new ClienteResponse(
            cliente.getNome(),
            cliente.getEmail(),
            cliente.getTelefone(),
            EnderecoResponse.de(cliente.getEndereco()),
            cliente.getCpf(),
            cliente.getResumo(),
            cliente.isAtivo()
        );
    }
}
