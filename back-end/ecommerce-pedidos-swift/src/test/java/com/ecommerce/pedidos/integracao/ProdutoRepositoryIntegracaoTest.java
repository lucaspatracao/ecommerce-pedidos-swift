package com.ecommerce.pedidos.integracao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ecommerce.pedidos.modelo.Produto;
import com.ecommerce.pedidos.repositorio.Pagina;
import com.ecommerce.pedidos.repositorio.RepositorioException;
import com.ecommerce.pedidos.repositorio.RepositorioException.Tipo;
import com.ecommerce.pedidos.repositorio.Visibilidade;
import com.ecommerce.pedidos.servico.PedidoService.ItemPedidoRequest;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProdutoRepositoryIntegracaoTest extends IntegracaoBancoBase {

    @Test
    @DisplayName("atualização: altera, busca de novo e confere o valor gravado")
    void deveAtualizarEConferirValorGravado() {
        Produto produto = criarProduto("PRD-001", "10.00", 5);

        Produto alterado = new Produto("PRD-001", "Camiseta Azul", new BigDecimal("19.90"), 8);
        alterado.setId(produto.getId());
        alterado.setVersao(produto.getVersao());
        transacao.executar(conexao -> produtoRepository.atualizar(conexao, alterado));

        Produto relido = transacao.executar(conexao -> produtoRepository.buscarPorId(conexao, produto.getId())).orElseThrow();
        assertEquals("Camiseta Azul", relido.getNome());
        assertEquals(new BigDecimal("19.90"), relido.getPreco());
        assertEquals(8, relido.getQuantidadeEmEstoque());
        assertEquals(1, relido.getVersao());
    }

    @Test
    @DisplayName("atualização de id inexistente falha com erro de domínio (nunca silencia)")
    void atualizarIdInexistenteDeveFalhar() {
        Produto fantasma = new Produto("PRD-404", "Fantasma", new BigDecimal("1.00"), 1);
        fantasma.setId(9999L);

        RepositorioException erro = assertThrows(RepositorioException.class,
            () -> transacao.executar(conexao -> produtoRepository.atualizar(conexao, fantasma)));
        assertEquals(Tipo.NAO_ENCONTRADO, erro.getTipo());
    }

    @Test
    @DisplayName("produto sem id não pode ser atualizado")
    void produtoSemIdNaoPodeSerAtualizado() {
        Produto semId = new Produto("PRD-000", "Sem id", new BigDecimal("1.00"), 1);

        RepositorioException erro = assertThrows(RepositorioException.class,
            () -> transacao.executar(conexao -> produtoRepository.atualizar(conexao, semId)));
        assertEquals(Tipo.VALIDACAO, erro.getTipo());
    }

    @Test
    @DisplayName("soft delete: produto inativado não aparece nas listagens padrão")
    void produtoInativadoNaoApareceNasListagensPadrao() {
        Produto ativo = criarProduto("PRD-ATV", "10.00", 5);
        Produto inativo = criarProduto("PRD-INA", "10.00", 5);

        transacao.executar(conexao -> {
            produtoRepository.inativar(conexao, inativo.getId());
            return null;
        });

        List<Produto> padrao = transacao.executar(conexao -> produtoRepository.listarTodos(conexao, Pagina.todas()));
        assertEquals(List.of("PRD-ATV"), padrao.stream().map(Produto::getCodigo).toList());
        assertTrue(transacao.executar(conexao -> produtoRepository.buscarPorCodigo(conexao, "PRD-INA")).isEmpty());
        long ativos = transacao.executar(produtoRepository::contar);
        assertEquals(1, ativos);

        // Forma explícita de enxergar os inativos.
        List<Produto> inativos = transacao.executar(
            conexao -> produtoRepository.listar(conexao, Visibilidade.INATIVOS, Pagina.todas()));
        assertEquals(List.of("PRD-INA"), inativos.stream().map(Produto::getCodigo).toList());

        transacao.executar(conexao -> {
            produtoRepository.reativar(conexao, inativo.getId());
            return null;
        });
        assertEquals(2, transacao.executar(conexao -> produtoRepository.listarTodos(conexao, Pagina.todas())).size());
        assertTrue(ativo.isAtivo());
    }

    @Test
    @DisplayName("inativar produto inexistente falha com NAO_ENCONTRADO")
    void inativarInexistenteDeveFalhar() {
        RepositorioException erro = assertThrows(RepositorioException.class,
            () -> transacao.executar(conexao -> {
                produtoRepository.inativar(conexao, 4242L);
                return null;
            }));
        assertEquals(Tipo.NAO_ENCONTRADO, erro.getTipo());
    }

    @Test
    @DisplayName("produto nunca vendido pode ser excluído de fato")
    void produtoNuncaVendidoPodeSerExcluido() {
        Produto produto = criarProduto("PRD-DEL", "10.00", 5);

        transacao.executar(conexao -> {
            produtoRepository.remover(conexao, produto.getId());
            return null;
        });

        assertTrue(transacao.executar(
            conexao -> produtoRepository.buscarPorId(conexao, produto.getId(), Visibilidade.TODOS)).isEmpty());
    }

    @Test
    @DisplayName("produto já vendido não pode ser excluído (RESTRICT) e a mensagem é de negócio")
    void produtoVendidoNaoPodeSerExcluido() throws Exception {
        criarCliente("11122233344", "ana@email.com");
        Produto produto = criarProduto("PRD-VND", "10.00", 5);
        pedidoService().criarPedido("11122233344", List.of(new ItemPedidoRequest("PRD-VND", 1)));

        RepositorioException erro = assertThrows(RepositorioException.class,
            () -> transacao.executar(conexao -> {
                produtoRepository.remover(conexao, produto.getId());
                return null;
            }));
        assertEquals(Tipo.VIOLACAO_INTEGRIDADE, erro.getTipo());
        assertTrue(erro.getMessage().startsWith("Este produto já foi vendido"));
        assertFalse(erro.getMessage().contains("item_pedido"));
        assertFalse(erro.getMessage().contains("fk_"));
    }

    @Test
    @DisplayName("concorrência otimista: segunda gravação com versão antiga é recusada")
    void controleDeConcorrenciaOtimista() {
        Produto original = criarProduto("PRD-VER", "10.00", 5);

        // Duas "sessões" leem a mesma versão (0).
        Produto sessaoA = transacao.executar(conexao -> produtoRepository.buscarPorId(conexao, original.getId())).orElseThrow();
        Produto sessaoB = transacao.executar(conexao -> produtoRepository.buscarPorId(conexao, original.getId())).orElseThrow();

        sessaoA.setPreco(new BigDecimal("12.00"));
        transacao.executar(conexao -> produtoRepository.atualizar(conexao, sessaoA));

        sessaoB.setPreco(new BigDecimal("15.00"));
        RepositorioException erro = assertThrows(RepositorioException.class,
            () -> transacao.executar(conexao -> produtoRepository.atualizar(conexao, sessaoB)));
        assertEquals(Tipo.CONFLITO_CONCORRENCIA, erro.getTipo());

        Produto relido = transacao.executar(conexao -> produtoRepository.buscarPorId(conexao, original.getId())).orElseThrow();
        assertEquals(new BigDecimal("12.00"), relido.getPreco());
    }

    @Test
    @DisplayName("atualização parcial altera somente o estoque")
    void atualizacaoParcialDoEstoque() {
        Produto produto = criarProduto("PRD-EST", "10.00", 5);

        transacao.executar(conexao -> {
            produtoRepository.atualizarEstoque(conexao, produto.getId(), 42);
            return null;
        });

        Produto relido = transacao.executar(conexao -> produtoRepository.buscarPorId(conexao, produto.getId())).orElseThrow();
        assertEquals(42, relido.getQuantidadeEmEstoque());
        assertEquals("Produto PRD-EST", relido.getNome());
        assertEquals(new BigDecimal("10.00"), relido.getPreco());
    }

    @Test
    @DisplayName("baixa de estoque condicional não deixa o estoque negativo")
    void baixaCondicionalNaoDeixaEstoqueNegativo() throws SQLException {
        Produto produto = criarProduto("PRD-BXA", "10.00", 3);

        boolean baixouAlemDoDisponivel = transacao.executar(
            conexao -> produtoRepository.baixarEstoque(conexao, produto.getId(), 4));
        assertFalse(baixouAlemDoDisponivel);
        assertEquals(3, estoqueNoBanco(produto.getId()));
        boolean baixouTudo = transacao.executar(conexao -> produtoRepository.baixarEstoque(conexao, produto.getId(), 3));
        assertTrue(baixouTudo);
        assertEquals(0, estoqueNoBanco(produto.getId()));
    }

    @Test
    @DisplayName("paginação e busca por nome")
    void paginacaoEBuscaPorNome() {
        criarProduto("PRD-A", "10.00", 1);
        criarProduto("PRD-B", "10.00", 1);
        criarProduto("PRD-C", "10.00", 1);

        assertEquals(2, transacao.executar(conexao -> produtoRepository.listarTodos(conexao, Pagina.de(0, 2))).size());
        List<Produto> segunda = transacao.executar(conexao -> produtoRepository.listarTodos(conexao, Pagina.de(1, 2)));
        assertEquals(List.of("PRD-C"), segunda.stream().map(Produto::getCodigo).toList());

        assertEquals(1, transacao.executar(conexao -> produtoRepository.buscarPorNome(conexao, "PRD-B")).size());
        assertEquals(0, transacao.executar(conexao -> produtoRepository.buscarPorNome(conexao, "%")).size());
    }

    @Test
    @DisplayName("código duplicado vira erro DUPLICADO com mensagem amigável")
    void codigoDuplicado() {
        criarProduto("PRD-DUP", "10.00", 1);

        RepositorioException erro = assertThrows(RepositorioException.class, () -> criarProduto("PRD-DUP", "11.00", 1));
        assertEquals(Tipo.DUPLICADO, erro.getTipo());
        assertEquals("Já existe um produto com este código.", erro.getMessage());
    }
}
