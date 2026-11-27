package com.ecommerce.pedidos.integracao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ecommerce.pedidos.modelo.Cliente;
import com.ecommerce.pedidos.modelo.Pedido;
import com.ecommerce.pedidos.repositorio.Pagina;
import com.ecommerce.pedidos.repositorio.RepositorioException;
import com.ecommerce.pedidos.repositorio.RepositorioException.Tipo;
import com.ecommerce.pedidos.repositorio.Visibilidade;
import com.ecommerce.pedidos.servico.ClienteService;
import com.ecommerce.pedidos.servico.PedidoService.ItemPedidoRequest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ClienteRepositoryIntegracaoTest extends IntegracaoBancoBase {

    @Test
    @DisplayName("atualização: altera, busca de novo e confere o valor gravado")
    void deveAtualizarEConferir() {
        Cliente cliente = criarCliente("12345678901", "bruno@email.com");

        Cliente alterado = new Cliente("Bruno Souza", "12345678901", "bruno.souza@email.com", "11888887777",
            "Av. Paulista, 1000");
        alterado.setId(cliente.getId());
        transacao.executar(conexao -> clienteRepository.atualizar(conexao, alterado));

        Cliente relido = transacao.executar(conexao -> clienteRepository.buscarPorId(conexao, cliente.getId())).orElseThrow();
        assertEquals("Bruno Souza", relido.getNome());
        assertEquals("bruno.souza@email.com", relido.getEmail());
        assertEquals("Av. Paulista, 1000", relido.getEndereco().getLogradouro());
    }

    @Test
    @DisplayName("atualização de id inexistente falha com NAO_ENCONTRADO")
    void atualizarInexistente() {
        Cliente fantasma = new Cliente("Fantasma", "99999999999", "fantasma@email.com", "11000000000", "Rua X");
        fantasma.setId(777L);

        RepositorioException erro = assertThrows(RepositorioException.class,
            () -> transacao.executar(conexao -> clienteRepository.atualizar(conexao, fantasma)));
        assertEquals(Tipo.NAO_ENCONTRADO, erro.getTipo());
    }

    @Test
    @DisplayName("exclusão: exclui, busca de novo e confirma resultado vazio")
    void deveExcluirEConfirmarVazio() {
        Cliente cliente = criarCliente("22233344455", "carla@email.com");

        transacao.executar(conexao -> {
            clienteRepository.remover(conexao, cliente.getId());
            return null;
        });

        assertTrue(transacao.executar(
            conexao -> clienteRepository.buscarPorId(conexao, cliente.getId(), Visibilidade.TODOS)).isEmpty());
    }

    @Test
    @DisplayName("excluir id inexistente falha com NAO_ENCONTRADO")
    void excluirInexistente() {
        RepositorioException erro = assertThrows(RepositorioException.class,
            () -> transacao.executar(conexao -> {
                clienteRepository.remover(conexao, 31337L);
                return null;
            }));
        assertEquals(Tipo.NAO_ENCONTRADO, erro.getTipo());
    }

    @Test
    @DisplayName("restrição: excluir cliente com pedidos falha com a mensagem de negócio")
    void excluirClienteComPedidosFalha() throws Exception {
        Cliente cliente = criarCliente("33344455566", "davi@email.com");
        criarProduto("PRD-CLI", "25.00", 10);
        pedidoService().criarPedido(cliente.getCpf(), List.of(new ItemPedidoRequest("PRD-CLI", 1)));

        ClienteService clienteService = new ClienteService(clienteRepository, transacao);
        RepositorioException erro = assertThrows(RepositorioException.class,
            () -> clienteService.excluir(cliente.getCpf()));

        assertEquals(Tipo.VIOLACAO_INTEGRIDADE, erro.getTipo());
        assertTrue(erro.getMessage().startsWith("Este cliente possui pedidos e não pode ser excluído."));
        assertFalse(erro.getMessage().contains("pedido_cliente"));
        assertTrue(transacao.executar(conexao -> clienteRepository.buscarPorId(conexao, cliente.getId())).isPresent());
    }

    @Test
    @DisplayName("soft delete: cliente inativo some da listagem, mas continua nos pedidos antigos")
    void clienteInativoContinuaNosPedidos() throws Exception {
        Cliente cliente = criarCliente("44455566677", "eva@email.com");
        criarProduto("PRD-EVA", "25.00", 10);
        Pedido pedido = pedidoService().criarPedido(cliente.getCpf(), List.of(new ItemPedidoRequest("PRD-EVA", 1)));

        new ClienteService(clienteRepository, transacao).inativar(cliente.getCpf());

        assertTrue(transacao.executar(conexao -> clienteRepository.listarTodos(conexao, Pagina.todas())).isEmpty());
        long clientesAtivos = transacao.executar(clienteRepository::contar);
        assertEquals(0, clientesAtivos);
        Pedido relido = pedidoService().buscarPorNumero(pedido.getNumero());
        assertEquals("44455566677", relido.getCliente().getCpf());
        assertFalse(relido.getCliente().isAtivo());
    }

    @Test
    @DisplayName("busca por e-mail e CPF duplicado")
    void buscaPorEmailECpfDuplicado() {
        criarCliente("55566677788", "fabio@email.com");

        assertTrue(transacao.executar(conexao -> clienteRepository.buscarPorEmail(conexao, "fabio@email.com")).isPresent());
        RepositorioException erro = assertThrows(RepositorioException.class,
            () -> criarCliente("55566677788", "outro@email.com"));
        assertEquals(Tipo.DUPLICADO, erro.getTipo());
    }
}
