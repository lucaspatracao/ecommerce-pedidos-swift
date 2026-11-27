package com.ecommerce.pedidos.dto;

import com.ecommerce.pedidos.modelo.Cliente;

/**
 * JSON de cliente devolvido pela API.
 *
 * <p>Por que DTO e não a entidade direto? A entidade agora tem id, relações
 * preguiçosas (LAZY) e anotações JPA. Serializá-la exporia detalhes do banco
 * e poderia disparar consultas fora da transação. O record fixa o contrato:
 * estes são exatamente os campos que o front-end já recebia.</p>
 */
public record ClienteResponse(
    String nome,
    String documento,
    String email,
    String telefone,
    EnderecoResponse endereco,
    String cpf,
    String identificacao,
    String resumo
) {

    public static ClienteResponse de(Cliente cliente) {
        return new ClienteResponse(
            cliente.getNome(),
            cliente.getDocumento(),
            cliente.getEmail(),
            cliente.getTelefone(),
            EnderecoResponse.de(cliente.getEndereco()),
            cliente.getCpf(),
            cliente.getIdentificacao(),
            cliente.getResumo()
        );
    }
}
