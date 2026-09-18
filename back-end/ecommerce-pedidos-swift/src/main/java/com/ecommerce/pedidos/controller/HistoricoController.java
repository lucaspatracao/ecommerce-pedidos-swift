package com.ecommerce.pedidos.controller;

import com.ecommerce.pedidos.model.Pedido;
import com.ecommerce.pedidos.service.PedidoService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
public class HistoricoController {
    private final PedidoService pedidoService;

    public HistoricoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @GetMapping("/historico")
    public List<Map<String, String>> listar() {
        List<Map<String, String>> eventos = new ArrayList<>();

        for (Pedido pedido : pedidoService.listarRecentes(10)) {
            Map<String, String> evento = new HashMap<>();
            evento.put("titulo", "Pedido " + pedido.getNumero() + " criado");
            evento.put("descricao", "Cliente: " + pedido.getCliente().getNome() + " | Status: " + pedido.getSituacao());
            evento.put("data", pedido.getData().toString());
            eventos.add(evento);
        }

        return eventos;
    }
}
