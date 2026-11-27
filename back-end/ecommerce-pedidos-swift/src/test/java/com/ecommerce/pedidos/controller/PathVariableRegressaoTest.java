package com.ecommerce.pedidos.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Regressão do bug "Name for argument of type [java.lang.String] not specified".
 *
 * O erro só aparecia em PUT e DELETE porque são as rotas com {@code @PathVariable}.
 * Este teste sobe a aplicação inteira e chama essas rotas via HTTP simulado
 * (MockMvc): se o nome do parâmetro não for resolvido, a resposta vira 500.
 */
@Tag("integracao")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PathVariableRegressaoTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("PUT e DELETE de cliente resolvem o @PathVariable cpf")
    void deveEditarEExcluirClientePeloCpf() throws Exception {
        String cpf = "90709836481";
        String corpo = """
            {"nome":"Cliente Regressao","cpf":"%s","email":"regressao@teste.com","telefone":"11999990000","endereco":"Rua Teste"}
            """.formatted(cpf);

        mockMvc.perform(post("/api/clientes").contentType(MediaType.APPLICATION_JSON).content(corpo))
            .andExpect(status().isCreated());

        mockMvc.perform(put("/api/clientes/{cpf}", cpf).contentType(MediaType.APPLICATION_JSON)
                .content(corpo.replace("Cliente Regressao", "Cliente Editado")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nome").value("Cliente Editado"));

        mockMvc.perform(delete("/api/clientes/{cpf}", cpf))
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("PUT e DELETE de produto resolvem o @PathVariable codigo")
    void deveEditarEExcluirProdutoPeloCodigo() throws Exception {
        String corpo = """
            {"codigo":"REG-001","nome":"Produto Regressao","descricao":"","preco":"10.00","estoque":3,"disponivel":true}
            """;

        mockMvc.perform(post("/api/produtos").contentType(MediaType.APPLICATION_JSON).content(corpo))
            .andExpect(status().isCreated());

        mockMvc.perform(put("/api/produtos/{codigo}", "REG-001").contentType(MediaType.APPLICATION_JSON)
                .content(corpo.replace("Produto Regressao", "Produto Editado")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nome").value("Produto Editado"));

        mockMvc.perform(delete("/api/produtos/{codigo}", "REG-001"))
            .andExpect(status().isNoContent());
    }
}
