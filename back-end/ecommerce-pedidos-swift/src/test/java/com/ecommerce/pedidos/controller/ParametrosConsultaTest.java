package com.ecommerce.pedidos.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ecommerce.pedidos.servico.Visibilidade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

/** Parâmetros de listagem (portado do PersistenciaSemBancoTest da feature/crud-completo). */
class ParametrosConsultaTest {

    @Test
    @DisplayName("sem pagina nem tamanho devolve tudo; com um deles, página 0 de 20 por padrão")
    void paginacaoPadrao() {
        assertTrue(ParametrosConsulta.pagina(null, null).isUnpaged());

        Pageable soTamanho = ParametrosConsulta.pagina(null, 5);
        assertEquals(0, soTamanho.getPageNumber());
        assertEquals(5, soTamanho.getPageSize());

        Pageable soPagina = ParametrosConsulta.pagina(2, null);
        assertEquals(2, soPagina.getPageNumber());
        assertEquals(ParametrosConsulta.TAMANHO_PADRAO, soPagina.getPageSize());
        assertEquals(40, soPagina.getOffset());
    }

    @Test
    @DisplayName("página negativa, tamanho zero e tamanho acima de 500 são recusados")
    void limitesDaPaginacao() {
        assertThrows(IllegalArgumentException.class, () -> ParametrosConsulta.pagina(-1, 10));
        assertThrows(IllegalArgumentException.class, () -> ParametrosConsulta.pagina(0, 0));
        IllegalArgumentException grande = assertThrows(IllegalArgumentException.class,
            () -> ParametrosConsulta.pagina(0, ParametrosConsulta.TAMANHO_MAXIMO + 1));
        assertEquals("Tamanho da página deve ser no máximo 500.", grande.getMessage());
        assertEquals(500, ParametrosConsulta.pagina(0, 500).getPageSize());
    }

    @Test
    @DisplayName("situacao: vazio = ativos; ativos/inativos/todos sem diferenciar maiúsculas; outro valor é recusado")
    void visibilidade() {
        assertEquals(Visibilidade.ATIVOS, Visibilidade.deTexto(null));
        assertEquals(Visibilidade.ATIVOS, Visibilidade.deTexto(" "));
        assertEquals(Visibilidade.INATIVOS, Visibilidade.deTexto("Inativos"));
        assertEquals(Visibilidade.TODOS, Visibilidade.deTexto(" TODOS "));
        assertEquals(Boolean.TRUE, Visibilidade.ATIVOS.filtroAtivo());
        assertEquals(Boolean.FALSE, Visibilidade.INATIVOS.filtroAtivo());
        assertNull(Visibilidade.TODOS.filtroAtivo());
        assertThrows(IllegalArgumentException.class, () -> Visibilidade.deTexto("excluidos"));
    }
}
