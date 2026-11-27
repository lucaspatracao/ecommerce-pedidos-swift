package com.ecommerce.pedidos.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ecommerce.pedidos.excecao.PedidoInvalidoException;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Regras de domínio que os testes originais não cobriam
 * (cenários adaptados da branch feature/crud-completo). Sem Spring.
 */
class DominioComplementarTest {

    private Cliente cliente;

    @BeforeEach
    void setUp() {
        cliente = new Cliente("Ana Souza", "89910440261", "ana@email.com", "11998887766", "Rua B");
    }

    @Nested
    @DisplayName("Pedido")
    class PedidoRegras {

        @Test
        @DisplayName("remover item devolve o estoque; índice inválido é recusado")
        void deveRemoverItem() {
            Produto produto = new Produto("P-100", "Caneca", new BigDecimal("30.00"), 5);
            Pedido pedido = new Pedido(cliente, "PED-2026-00001");
            pedido.adicionarItem(produto, 2);

            pedido.removerItem(0);

            assertEquals(5, produto.getQuantidadeEmEstoque());
            assertEquals(0, pedido.quantidadeDeItens());
            assertThrows(IndexOutOfBoundsException.class, () -> pedido.removerItem(0));
        }

        @Test
        @DisplayName("mesmo produto adicionado duas vezes soma a quantidade num item só")
        void deveAgruparMesmoProduto() {
            Produto produto = new Produto("P-101", "Lápis", new BigDecimal("2.00"), 10);
            Pedido pedido = new Pedido(cliente, "PED-2026-00001");
            pedido.adicionarItem(produto, 2);
            pedido.adicionarItem(produto, 3);

            assertEquals(1, pedido.quantidadeDeItens());
            assertEquals(5, pedido.getItens().get(0).getQuantidade());
            assertEquals(new BigDecimal("10.00"), pedido.calcularValorTotal());
        }

        @Test
        @DisplayName("itens ordenados por subtotal e texto do pedido")
        void deveOrdenarItens() {
            Pedido pedido = new Pedido(cliente, "PED-2026-00001");
            pedido.adicionarItem(new Produto("P-102", "Caro", new BigDecimal("50.00"), 5), 1);
            pedido.adicionarItem(new Produto("P-103", "Barato", new BigDecimal("5.00"), 5), 1);

            List<ItemPedido> ordenados = pedido.getItensOrdenadosPorSubtotal();

            assertEquals("Barato", ordenados.get(0).getProduto().getNome());
            assertTrue(pedido.toString().contains("Ana Souza"));
            assertTrue(ordenados.get(0).toString().contains("Barato"));
        }

        @Test
        @DisplayName("pagarCom exige valor igual ao total; pagamento nulo é recusado")
        void deveValidarPagamento() {
            Pedido pedido = new Pedido(cliente, "PED-2026-00001");
            pedido.adicionarItem(new Produto("P-104", "Livro", new BigDecimal("40.00"), 5), 1);

            assertThrows(IllegalStateException.class, () -> pedido.pagarCom(new Pix(new BigDecimal("39.99"), "c", "EMAIL")));
            assertThrows(IllegalArgumentException.class, () -> pedido.pagarCom(null));
            assertThrows(IllegalArgumentException.class, () -> pedido.pagar(null));
            assertTrue(pedido.isAberto());
        }

        @Test
        @DisplayName("cancelar pedido pago estorna o pagamento e devolve o estoque")
        void deveCancelarPedidoPago() {
            Produto produto = new Produto("P-105", "Mochila", new BigDecimal("120.00"), 4);
            Pedido pedido = new Pedido(cliente, "PED-2026-00001");
            pedido.adicionarItem(produto, 2);
            Pix pix = new Pix(new BigDecimal("240.00"), "c", "EMAIL");
            pedido.pagarCom(pix);

            pedido.cancelar();

            assertEquals(SituacaoPedido.CANCELADO, pedido.getSituacao());
            assertEquals(SituacaoPagamento.ESTORNADO, pix.getSituacao());
            assertEquals(4, produto.getQuantidadeEmEstoque());
            assertThrows(IllegalStateException.class, pedido::cancelar);
        }

        @Test
        @DisplayName("pedido fechado não aceita itens, remoção nem novo pagamento")
        void deveBloquearPedidoFechado() {
            Produto produto = new Produto("P-106", "Boné", new BigDecimal("25.00"), 4);
            Pedido pedido = new Pedido(cliente, "PED-2026-00001");
            pedido.adicionarItem(produto, 1);
            pedido.pagar(new Pix(new BigDecimal("25.00"), "c", "EMAIL"));

            assertThrows(IllegalStateException.class, () -> pedido.adicionarItem(produto, 1));
            assertThrows(IllegalStateException.class, () -> pedido.removerItem(0));
            assertThrows(PedidoInvalidoException.class, () -> pedido.pagar(new Pix(new BigDecimal("25.00"), "c", "EMAIL")));
        }

        @Test
        @DisplayName("validações de criação e de item")
        void deveValidarEntradas() {
            Pedido pedido = new Pedido(cliente, "PED-2026-00001");
            assertThrows(IllegalArgumentException.class, () -> new Pedido(null, "PED-2026-00001"));
            assertThrows(IllegalArgumentException.class, () -> new Pedido(cliente, " "));
            assertThrows(IllegalArgumentException.class, () -> pedido.adicionarItem(null, 1));
            assertThrows(IllegalArgumentException.class,
                () -> pedido.adicionarItem(new Produto("P-107", "X", BigDecimal.ONE, 1), 0));
        }

        @Test
        @DisplayName("o pedido guarda o número recebido (sem sortear) e conta quantos foram criados")
        void deveGuardarNumeroRecebido() {
            int antes = Pedido.getTotalDePedidosCriados();
            Pedido pedido = new Pedido(cliente, " PED-2026-00042 ");
            assertEquals("PED-2026-00042", pedido.getNumero());
            assertEquals(antes + 1, Pedido.getTotalDePedidosCriados());
        }
    }

    @Nested
    @DisplayName("Cadastros")
    class Cadastros {

        @Test
        @DisplayName("cliente aceita CPF com máscara e guarda só os dígitos")
        void deveNormalizarCpf() {
            Cliente comMascara = new Cliente("Bia", "899.104.402-61", "bia@email.com", "11900000000", (String) null);

            assertEquals("89910440261", comMascara.getCpf());
            assertNull(comMascara.getEndereco());
            assertTrue(comMascara.toString().contains("89910440261"));
        }

        @Test
        @DisplayName("cliente recusa CPF com dígito verificador errado e telefone com letras")
        void deveRecusarDadosInvalidos() {
            assertThrows(IllegalArgumentException.class,
                () -> new Cliente("Bia", "89910440262", "bia@email.com", "11900000000", "Rua"));
            assertThrows(IllegalArgumentException.class,
                () -> new Cliente("Bia", "89910440261", "bia@email.com", "11-9000", "Rua"));
        }

        @Test
        @DisplayName("atualizarContato troca nome e contato; com dado inválido nada muda")
        void deveAtualizarContato() {
            cliente.atualizarContato("Ana Lima", "ana.lima@email.com", "11911112222", Endereco.aPartirDoTexto("Rua Nova"));

            assertEquals("Ana Lima", cliente.getNome());
            assertEquals("Rua Nova", cliente.getEndereco().getLogradouro());

            assertThrows(IllegalArgumentException.class,
                () -> cliente.atualizarContato("Outro Nome", "email-invalido", "11911112222", null));
            assertEquals("Ana Lima", cliente.getNome());
        }

        @Test
        @DisplayName("soft delete: cliente nasce ativo, inativa e reativa; repetir a mesma operação é recusado")
        void deveInativarEReativar() {
            assertTrue(cliente.isAtivo());
            cliente.inativar();
            assertFalse(cliente.isAtivo());
            assertThrows(IllegalStateException.class, cliente::inativar);
            cliente.reativar();
            assertTrue(cliente.isAtivo());
            assertThrows(IllegalStateException.class, cliente::reativar);
        }

        @Test
        @DisplayName("endereço a partir de texto: vazio vira null, cidade e CEP 'Não informado'")
        void deveCriarEndereco() {
            assertNull(Endereco.aPartirDoTexto("  "));
            Endereco endereco = Endereco.aPartirDoTexto("Rua A");
            assertEquals("Não informado", endereco.getCidade());
            assertEquals("Não informado", endereco.getCep());
            assertTrue(endereco.toString().contains("Rua A"));
            assertThrows(IllegalArgumentException.class, () -> new Endereco("Rua", "", "000"));
        }

        @Test
        @DisplayName("funcionário é outra Pessoa, com identificação e resumo próprios")
        void deveCriarFuncionario() {
            Funcionario funcionario = new Funcionario("João", "11122233344", "M-101", "Vendedor");
            funcionario.promoverPara("Supervisor");

            assertEquals("Supervisor", funcionario.getCargo());
            assertEquals("M-101", funcionario.getMatricula());
            assertTrue(funcionario.getResumo().endsWith("Supervisor"));
            assertThrows(IllegalArgumentException.class, () -> new Funcionario("João", "11A", "M", "C"));
            assertThrows(IllegalArgumentException.class, () -> funcionario.promoverPara(" "));
        }

        @Test
        @DisplayName("categoria agrega produtos sem duplicar")
        void deveAgruparProdutos() {
            Categoria categoria = new Categoria("Papelaria");
            Produto caneta = new Produto("P-200", "Caneta", 2.5, 10);
            categoria.incluir(caneta);
            categoria.incluir(caneta);

            assertEquals(1, categoria.getProdutos().size());
            assertTrue(categoria.remover(caneta));
            assertEquals("Papelaria", categoria.getNome());
            assertThrows(IllegalArgumentException.class, () -> categoria.incluir(null));
            assertThrows(IllegalArgumentException.class, () -> new Categoria(" "));
        }

        @Test
        @DisplayName("produto: estoque, disponibilidade, código e dados cadastrais")
        void deveGerenciarProduto() {
            Produto produto = new Produto("P-201", "Agenda", "Capa dura", new BigDecimal("35.50"), 3);

            produto.desativar();
            assertFalse(produto.temEstoqueDisponivel(1));
            produto.ativar();
            assertTrue(produto.temEstoqueDisponivel(3));
            assertFalse(produto.temEstoqueDisponivel(0));

            produto.definirEstoque(0);
            assertThrows(IllegalArgumentException.class, () -> produto.definirEstoque(-1));
            assertThrows(IllegalArgumentException.class, () -> produto.adicionarEstoque(0));
            assertThrows(IllegalArgumentException.class, () -> produto.baixarEstoque(0));

            produto.alterarCodigo("P-202");
            assertEquals("P-202", produto.getCodigo());
            assertThrows(IllegalArgumentException.class, () -> produto.alterarCodigo("P"));

            produto.atualizarDados("Agenda 2027", null, new BigDecimal("40"));
            assertEquals("", produto.getDescricao());
            assertEquals(new BigDecimal("40.00"), produto.getPreco());
            assertThrows(IllegalArgumentException.class, () -> produto.atualizarDados("Agenda", "", null));
            assertTrue(produto.toString().contains("P-202"));
            assertTrue(Produto.getTotalDeProdutosCriados() > 0);
        }
    }

    @Test
    @DisplayName("SituacaoPedido.deTexto ignora maiúsculas e explica valores aceitos")
    void deveConverterSituacao() {
        assertEquals(SituacaoPedido.CANCELADO, SituacaoPedido.deTexto(" cancelado "));
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class, () -> SituacaoPedido.deTexto("ENVIADO"));
        assertTrue(erro.getMessage().contains("ABERTO"));
        assertThrows(IllegalArgumentException.class, () -> SituacaoPedido.deTexto(" "));
    }
}
