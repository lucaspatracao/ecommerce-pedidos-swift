package com.ecommerce.pedidos.controller;

import com.ecommerce.pedidos.dto.HistoricoEventoResponse;
import com.ecommerce.pedidos.servico.PedidoService;
import java.util.List;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
public class HistoricoController {
    private static final int QUANTIDADE_EVENTOS = 10;

    private final PedidoService pedidoService;

    public HistoricoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @GetMapping("/historico")
    public List<HistoricoEventoResponse> listar() {
        return pedidoService.listarRecentes(QUANTIDADE_EVENTOS).stream()
            .map(HistoricoEventoResponse::de)
            .toList();
    }
}
