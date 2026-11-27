package com.ecommerce.pedidos.servico;

import com.ecommerce.pedidos.dto.ClienteRequest;
import com.ecommerce.pedidos.dto.ClienteResponse;
import java.util.List;
import org.springframework.data.domain.Pageable;

/**
 * Casos de uso de cliente (Aula 14).
 *
 * <p>O controller depende desta INTERFACE, não da implementação. Assim a
 * regra de negócio pode mudar de implementação (ou ser simulada num teste)
 * sem tocar na camada web.</p>
 *
 * <p>Soft delete: cliente inativo some das listagens padrão e das buscas,
 * mas continua no banco para preservar o histórico de pedidos.</p>
 */
public interface ClienteService {

    /** Clientes ativos, todos de uma vez (uso do front). */
    default List<ClienteResponse> listarTodos() {
        return listar(Visibilidade.ATIVOS, Pageable.unpaged());
    }

    List<ClienteResponse> listar(Visibilidade visibilidade, Pageable pagina);

    /** Cliente ATIVO pelo CPF. @throws com.ecommerce.pedidos.excecao.ClienteNaoEncontradoException (404) */
    ClienteResponse buscarPorCpf(String cpf);

    /** @throws com.ecommerce.pedidos.excecao.ConflitoException (409) se o CPF ou o e-mail já existirem */
    ClienteResponse cadastrar(ClienteRequest request);

    /** Atualiza nome e contato de um cliente ativo; o CPF não muda. */
    ClienteResponse atualizar(String cpf, ClienteRequest request);

    /**
     * Exclusão definitiva (ativo ou inativo).
     * @throws com.ecommerce.pedidos.excecao.ConflitoException (409) se o cliente tiver pedidos: inative-o
     */
    void excluir(String cpf);

    /** Soft delete. */
    void inativar(String cpf);

    /** Desfaz o soft delete. 404 se não houver cliente INATIVO com o CPF. */
    ClienteResponse reativar(String cpf);

    /** Quantidade de clientes ativos. */
    long contar();
}
