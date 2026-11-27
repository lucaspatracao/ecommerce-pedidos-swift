package com.ecommerce.pedidos.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Testes unitários de cada {@link FormaPagamento} (Aulas 06, 08 e 10).
 * Puro Java: nenhum Spring, nenhum banco.
 */
class FormasPagamentoTest {

    private static final BigDecimal CEM = new BigDecimal("100.00");

    @Nested
    @DisplayName("CartaoCredito")
    class Cartao {

        @Test
        @DisplayName("aprova dentro do limite, consome o limite e permite parcelar")
        void deveAprovarDentroDoLimite() {
            CartaoCredito cartao = new CartaoCredito(CEM, "**** 1234", "Visa", 3);

            assertTrue(cartao.processar());
            assertEquals(SituacaoPagamento.APROVADO, cartao.getSituacao());
            assertEquals(0, new BigDecimal("4900.00").compareTo(cartao.getLimiteDisponivel()));
            assertTrue(cartao.permiteParcelamento());
            assertEquals("Visa •••• **** 1234", cartao.descreverMetodo());
            assertEquals(List.of("numeroMascarado", "bandeira", "quantidadeDeParcelas", "limiteDisponivel"),
                List.copyOf(cartao.dadosEspecificos().keySet()));
            assertTrue(cartao.getComprovante().startsWith("COMPROVANTE CARTAO"));
            assertTrue(cartao.getResumo().contains("parcelas 3"));
        }

        @Test
        @DisplayName("recusa acima do limite e não consome o limite")
        void deveRecusarAcimaDoLimite() {
            CartaoCredito cartao = new CartaoCredito(new BigDecimal("5000.01"), "**** 1", "Visa", 1);

            assertFalse(cartao.processar());
            assertEquals(SituacaoPagamento.RECUSADO, cartao.getSituacao());
            assertEquals(0, new BigDecimal("5000.00").compareTo(cartao.getLimiteDisponivel()));
        }

        @ParameterizedTest
        @ValueSource(ints = {0, 13, -1})
        @DisplayName("recusa parcelas fora de 1 a 12")
        void deveRecusarParcelasInvalidas(int parcelas) {
            assertThrows(IllegalArgumentException.class, () -> new CartaoCredito(CEM, "**** 1", "Visa", parcelas));
        }

        @Test
        @DisplayName("exige número e bandeira")
        void deveExigirNumeroEBandeira() {
            assertThrows(IllegalArgumentException.class, () -> new CartaoCredito(CEM, " ", "Visa", 1));
            assertThrows(IllegalArgumentException.class, () -> new CartaoCredito(CEM, "**** 1", null, 1));
        }
    }

    @Nested
    @DisplayName("Boleto")
    class BoletoTest {

        @Test
        @DisplayName("aprova e expõe código e vencimento")
        void deveAprovar() {
            LocalDate vencimento = LocalDate.now().plusDays(3);
            Boleto boleto = new Boleto(CEM, vencimento, "34191.79001");

            assertTrue(boleto.processar());
            assertEquals(SituacaoPagamento.APROVADO, boleto.getSituacao());
            assertEquals("Código: 34191.79001", boleto.descreverMetodo());
            assertEquals(vencimento, boleto.dadosEspecificos().get("dataDeVencimento"));
            assertFalse(boleto.permiteParcelamento());
            assertTrue(boleto.getComprovante().contains("vencimento=" + vencimento));
            assertTrue(boleto.getResumo().contains("Boleto"));
        }

        @Test
        @DisplayName("recusa vencimento no passado, vencimento nulo e código vazio")
        void deveValidarCampos() {
            assertThrows(IllegalArgumentException.class, () -> new Boleto(CEM, LocalDate.now().minusDays(1), "123"));
            assertThrows(IllegalArgumentException.class, () -> new Boleto(CEM, null, "123"));
            assertThrows(IllegalArgumentException.class, () -> new Boleto(CEM, LocalDate.now(), " "));
        }
    }

    @Nested
    @DisplayName("Pix")
    class PixTest {

        @Test
        @DisplayName("aprova e normaliza o tipo da chave para maiúsculas")
        void deveAprovar() {
            Pix pix = new Pix(CEM, "cliente@email.com", "email");

            assertTrue(pix.processar());
            assertEquals("EMAIL", pix.getTipoDaChave());
            assertEquals("Chave: cliente@email.com", pix.descreverMetodo());
            assertEquals("cliente@email.com", pix.dadosEspecificos().get("chave"));
            assertTrue(pix.getComprovante().startsWith("COMPROVANTE PIX"));
        }

        @Test
        @DisplayName("exige chave e tipo da chave")
        void deveExigirChave() {
            assertThrows(IllegalArgumentException.class, () -> new Pix(CEM, "", "EMAIL"));
            assertThrows(IllegalArgumentException.class, () -> new Pix(CEM, "chave", null));
        }
    }

    @Nested
    @DisplayName("ValeCompras")
    class Vale {

        @Test
        @DisplayName("aprova, gera comprovante e não parcela")
        void deveAprovar() {
            ValeCompras vale = new ValeCompras(CEM, "VALE-1", "Loja");

            assertTrue(vale.processar());
            assertFalse(vale.permiteParcelamento());
            assertEquals("Vale Loja (VALE-1)", vale.descreverMetodo());
            assertEquals("Loja", vale.dadosEspecificos().get("emissor"));
            assertTrue(vale.getComprovante().contains("emissor=Loja"));
            assertEquals("ValeCompras", vale.getDescricao());
        }

        @Test
        @DisplayName("exige código e emissor")
        void deveExigirCampos() {
            assertThrows(IllegalArgumentException.class, () -> new ValeCompras(CEM, " ", "Loja"));
            assertThrows(IllegalArgumentException.class, () -> new ValeCompras(CEM, "V", " "));
        }
    }

    @Nested
    @DisplayName("Base comum (FormaPagamento)")
    class Base {

        @Test
        @DisplayName("valor é obrigatório e positivo, com 2 casas decimais")
        void deveValidarValor() {
            assertThrows(IllegalArgumentException.class, () -> new Pix(null, "c", "EMAIL"));
            assertThrows(IllegalArgumentException.class, () -> new Pix(BigDecimal.ZERO, "c", "EMAIL"));
            assertEquals(new BigDecimal("10.13"), new Pix(new BigDecimal("10.125"), "c", "EMAIL").getValor());
        }

        @Test
        @DisplayName("nasce PENDENTE; processar(valor) cobra o valor informado")
        void deveProcessarValorInformado() {
            Pix pix = new Pix(CEM, "c", "EMAIL");
            assertEquals(SituacaoPagamento.PENDENTE, pix.getSituacao());

            assertTrue(pix.processar(new BigDecimal("42.00")));
            assertEquals(new BigDecimal("42.00"), pix.getValor());
        }

        @Test
        @DisplayName("só estorna pagamento aprovado")
        void deveEstornarSomenteAprovado() {
            Pix pendente = new Pix(CEM, "c", "EMAIL");
            assertThrows(IllegalStateException.class, pendente::estornar);

            Pix aprovado = new Pix(CEM, "c", "EMAIL");
            aprovado.processar();
            aprovado.estornar();
            assertEquals(SituacaoPagamento.ESTORNADO, aprovado.getSituacao());
        }

        @Test
        @DisplayName("polimorfismo: a mesma chamada processar() executa a regra de cada subclasse")
        void devePolimorfismo() {
            List<FormaPagamento> formas = List.of(
                new Pix(CEM, "c", "EMAIL"),
                new Boleto(CEM, LocalDate.now(), "1"),
                new CartaoCredito(CEM, "**** 1", "Visa", 1),
                new CartaoCredito(new BigDecimal("9999.00"), "**** 2", "Visa", 1));

            List<Boolean> resultados = formas.stream().map(FormaPagamento::processar).toList();

            assertEquals(List.of(true, true, true, false), resultados);
        }
    }
}
