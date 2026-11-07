package com.ecommerce.pedidos.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ecommerce.pedidos.excecao.PagamentoRecusadoException;
import com.ecommerce.pedidos.excecao.PedidoInvalidoException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Cenários de domínio que ainda não tinham teste (completa a suíte da Aula 10). */
class DominioComplementarTest {
    private Cliente cliente;
    private Produto caderno;
    private Produto caneta;

    @BeforeEach
    void preparar() {
        cliente = new Cliente("Rita", "45645645645", "rita@email.com", "11944443333",
            new Endereco("Rua D, 4", "Campinas", "13000-000"));
        caderno = new Produto("PRD-CAD", "Caderno", new BigDecimal("20.00"), 10);
        caneta = new Produto("PRD-CAN", "Caneta", 3.50, 10);
    }

    @Nested
    @DisplayName("Pedido")
    class PedidoCenarios {

        @Test
        @DisplayName("remover item devolve o estoque; índice inválido é recusado")
        void removerItem() throws Exception {
            Pedido pedido = new Pedido(cliente);
            pedido.adicionarItem(caderno, 2);
            pedido.adicionarItem(caneta, 1);

            pedido.removerItem(0);

            assertEquals(1, pedido.quantidadeDeItens());
            assertEquals(10, caderno.getQuantidadeEmEstoque());
            assertThrows(IndexOutOfBoundsException.class, () -> pedido.removerItem(5));
        }

        @Test
        @DisplayName("itens ordenados por subtotal e texto do pedido")
        void ordenacaoEToString() throws Exception {
            Pedido pedido = new Pedido(cliente);
            pedido.adicionarItem(caderno, 1);
            pedido.adicionarItem(caneta, 2);

            List<ItemPedido> ordenados = pedido.getItensOrdenadosPorSubtotal();
            assertEquals("Caneta", ordenados.get(0).getProduto().getNome());
            assertTrue(pedido.toString().contains("Rita"));
            assertTrue(ordenados.get(0).toString().contains("Caneta"));
        }

        @Test
        @DisplayName("pagamento: sem processador, sem itens, recusado e sem forma definida")
        void falhasDePagamento() throws Exception {
            Pedido vazio = new Pedido(cliente);
            assertThrows(IllegalArgumentException.class, () -> vazio.pagar(null));
            assertThrows(PedidoInvalidoException.class,
                () -> vazio.pagar(new Pix(new BigDecimal("1.00"), "rita@email.com", "EMAIL")));
            assertThrows(IllegalStateException.class, vazio::pagar);
            assertThrows(IllegalArgumentException.class, () -> vazio.pagarCom(null));

            Pedido caro = new Pedido(cliente);
            caro.adicionarItem(new Produto("PRD-TV", "TV", new BigDecimal("6000.00"), 1), 1);
            CartaoCredito cartao = new CartaoCredito(new BigDecimal("6000.00"), "**** 1234", "VISA", 3);
            PagamentoRecusadoException recusa = assertThrows(PagamentoRecusadoException.class, () -> caro.pagarCom(cartao));
            assertEquals("CartaoCredito", recusa.getFormaPagamento());
            assertEquals(SituacaoPedido.ABERTO, caro.getSituacao());
        }

        @Test
        @DisplayName("cancelar pedido pago estorna o pagamento; pedido fechado não aceita itens nem remoção")
        void cancelarPedidoPago() throws Exception {
            Pedido pedido = new Pedido(cliente);
            pedido.adicionarItem(caderno, 1);
            Boleto boleto = new Boleto(new BigDecimal("20.00"), LocalDate.now().plusDays(3), "34191.79001");
            pedido.pagarCom(boleto);

            assertThrows(IllegalStateException.class, () -> pedido.adicionarItem(caneta, 1));
            assertThrows(IllegalStateException.class, () -> pedido.removerItem(0));
            assertThrows(PedidoInvalidoException.class, () -> pedido.pagar());

            pedido.cancelar();
            assertEquals(SituacaoPagamento.ESTORNADO, boleto.getSituacao());
            assertEquals(10, caderno.getQuantidadeEmEstoque());
            assertThrows(IllegalStateException.class, pedido::cancelar);
        }
    }

    @Nested
    @DisplayName("Formas de pagamento")
    class FormasDePagamento {

        @Test
        @DisplayName("vale-compras aprova, gera comprovante e não parcela")
        void valeCompras() {
            ValeCompras vale = new ValeCompras(new BigDecimal("50.00"), " VC-001 ", " Loja X ");

            assertTrue(vale.processar(new BigDecimal("40.00")));
            assertEquals(SituacaoPagamento.APROVADO, vale.getSituacao());
            assertEquals("VC-001", vale.getCodigo());
            assertEquals("Loja X", vale.getEmissor());
            assertEquals("ValeCompras", vale.getDescricao());
            assertFalse(vale.permiteParcelamento());
            assertTrue(vale.getComprovante().contains("codigo=VC-001"));
            assertThrows(IllegalArgumentException.class, () -> vale.setCodigo(" "));
            assertThrows(IllegalArgumentException.class, () -> vale.setEmissor(null));
        }

        @Test
        @DisplayName("cartão: limite, parcelas, comprovante e resumo")
        void cartao() {
            CartaoCredito cartao = new CartaoCredito(new BigDecimal("100.00"), "**** 9999", "MASTER", 2);

            assertTrue(cartao.processar());
            assertEquals(new BigDecimal("4900.00"), cartao.getLimiteDisponivel());
            assertTrue(cartao.permiteParcelamento());
            assertTrue(cartao.getComprovante().contains("parcelas=2"));
            assertTrue(cartao.getResumo().contains("MASTER"));
            assertThrows(IllegalArgumentException.class, () -> cartao.setQuantidadeDeParcelas(13));
            assertThrows(IllegalArgumentException.class, () -> cartao.setLimiteDisponivel(new BigDecimal("-1")));
            assertThrows(IllegalArgumentException.class, () -> cartao.setBandeira(""));
            assertThrows(IllegalArgumentException.class, () -> cartao.setNumeroMascarado(null));
        }

        @Test
        @DisplayName("boleto e pix: validações, comprovante e resumo")
        void boletoEPix() {
            Boleto boleto = new Boleto(new BigDecimal("30.00"), LocalDate.now(), "0001");
            assertTrue(boleto.getComprovante().contains("codigo=0001"));
            assertTrue(boleto.getResumo().contains("vencimento"));
            assertThrows(IllegalArgumentException.class, () -> boleto.setDataDeVencimento(LocalDate.now().minusDays(1)));
            assertThrows(IllegalArgumentException.class, () -> boleto.setDataDeVencimento(null));
            assertThrows(IllegalArgumentException.class, () -> boleto.setCodigoDeBarras(" "));

            Pix pix = new Pix(new BigDecimal("30.00"), "chave", "cpf");
            assertEquals("CPF", pix.getTipoDaChave());
            assertTrue(pix.getComprovante().contains("tipo=CPF"));
            assertTrue(pix.getResumo().contains("chave chave"));
            assertThrows(IllegalArgumentException.class, () -> pix.setChave(null));
            assertThrows(IllegalArgumentException.class, () -> pix.setTipoDaChave(" "));
        }

        @Test
        @DisplayName("base comum: valor, data e situação obrigatórios")
        void formaPagamentoBase() {
            Pix pix = new Pix(new BigDecimal("10.00"), "chave", "EMAIL");
            assertEquals(SituacaoPagamento.PENDENTE, pix.getSituacao());
            assertThrows(IllegalArgumentException.class, () -> pix.setValor(null));
            assertThrows(IllegalArgumentException.class, () -> pix.setValor(BigDecimal.ZERO));
            assertThrows(IllegalArgumentException.class, () -> pix.setDataDoPagamento(null));
            assertThrows(IllegalArgumentException.class, () -> pix.setSituacao(null));
            assertTrue(pix.getResumo().startsWith("Pix no valor de R$ 10.00"));
        }
    }

    @Nested
    @DisplayName("Cadastros")
    class Cadastros {

        @Test
        @DisplayName("categoria agrega produtos sem duplicar")
        void categoria() {
            Categoria categoria = new Categoria(" Papelaria ");
            categoria.incluir(caderno);
            categoria.incluir(caderno);
            categoria.incluir(caneta);

            assertEquals("Papelaria", categoria.getNome());
            assertEquals(2, categoria.getProdutos().size());
            assertTrue(categoria.remover(caneta));
            assertThrows(IllegalArgumentException.class, () -> categoria.incluir(null));
            assertThrows(IllegalArgumentException.class, () -> new Categoria(" "));
            assertThrows(UnsupportedOperationException.class, () -> categoria.getProdutos().add(caneta));
        }

        @Test
        @DisplayName("endereço, funcionário e cliente")
        void pessoasEEndereco() {
            Endereco endereco = cliente.getEndereco();
            assertEquals("Campinas", endereco.getCidade());
            assertEquals("13000-000", endereco.getCep());
            assertTrue(endereco.toString().contains("Rua D, 4"));
            assertThrows(IllegalArgumentException.class, () -> new Endereco("Rua", " ", "1"));

            Funcionario funcionario = new Funcionario("Caio", "78978978978", " M-01 ", " Vendedor ");
            assertEquals("M-01", funcionario.getMatricula());
            assertEquals("Caio - matrícula M-01", funcionario.getIdentificacao());
            assertTrue(funcionario.getResumo().endsWith("Vendedor"));
            assertThrows(IllegalArgumentException.class, () -> funcionario.setCargo(null));
            assertThrows(IllegalArgumentException.class, () -> funcionario.setMatricula(" "));

            assertEquals("Rita (45645645645)", cliente.getIdentificacao());
            assertTrue(cliente.toString().contains("rita@email.com"));
            cliente.setCpf("11111111111");
            assertEquals("11111111111", cliente.getDocumento());
            assertThrows(IllegalArgumentException.class, () -> cliente.setDocumento("12a"));
        }

        @Test
        @DisplayName("produto: estoque, descrição e texto")
        void produto() {
            assertFalse(caderno.temEstoqueDisponivel(0));
            caderno.setAtivo(false);
            assertFalse(caderno.temEstoqueDisponivel(1));
            assertThrows(IllegalArgumentException.class, () -> caderno.adicionarEstoque(0));
            assertThrows(IllegalArgumentException.class, () -> caderno.baixarEstoque(0));
            assertThrows(IllegalArgumentException.class, () -> new Produto("AB", "Curto", 1.0, 1));
            assertTrue(caderno.toString().contains("[PRD-CAD] Caderno"));
            assertTrue(Produto.getTotalDeProdutosCriados() > 0);
        }
    }
}
