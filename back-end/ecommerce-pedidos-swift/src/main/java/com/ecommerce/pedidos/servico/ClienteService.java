package com.ecommerce.pedidos.servico;

import com.ecommerce.pedidos.dto.ClienteRequest;
import com.ecommerce.pedidos.dto.ClienteResponse;
import java.util.List;

/**
 * Casos de uso de cliente (Aula 14).
 *
 * <p>O controller depende desta INTERFACE, não da implementação. Assim a
 * regra de negócio pode mudar de implementação (ou ser simulada num teste)
 * sem tocar na camada web.</p>
 */
public interface ClienteService {

    List<ClienteResponse> listarTodos();

    /** @throws com.ecommerce.pedidos.excecao.ClienteNaoEncontradoException (404) */
    ClienteResponse buscarPorCpf(String cpf);

    /** @throws com.ecommerce.pedidos.excecao.ConflitoException (409) se o CPF já existir */
    ClienteResponse cadastrar(ClienteRequest request);

    ClienteResponse atualizar(String cpf, ClienteRequest request);

    /** @throws com.ecommerce.pedidos.excecao.ConflitoException (409) se o cliente tiver pedidos */
    void excluir(String cpf);

    long contar();
}
