package com.ecommerce.pedidos.controller;

import com.ecommerce.pedidos.model.Pedido;
import com.ecommerce.pedidos.model.SituacaoPedido;
import com.ecommerce.pedidos.service.PedidoService;
import com.ecommerce.pedidos.service.PedidoService.ItemPedidoRequest;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
public class PedidoController {
    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @GetMapping("/pedidos")
    public List<Pedido> listar() {
        return pedidoService.listarTodos();
    }

    @GetMapping("/pedidos/{numero}")
    public Pedido buscarPorNumero(@PathVariable String numero) {
        return pedidoService.buscarPorNumero(numero);
    }

    @PostMapping("/pedidos")
    public ResponseEntity<Pedido> criar(@RequestBody PedidoRequest request) {
        Pedido pedido = pedidoService.criarPedido(request.clienteCpf(), request.itens());
        return ResponseEntity.status(HttpStatus.CREATED).body(pedido);
    }

    @PutMapping("/pedidos/{numero}/status")
    public Pedido atualizarStatus(@PathVariable String numero, @RequestBody AtualizarStatusRequest request) {
        if (request == null || request.status() == null || request.status().isBlank()) {
            throw new IllegalArgumentException("Status do pedido é obrigatório.");
        }
        pedidoService.atualizarStatus(numero, SituacaoPedido.valueOf(request.status()));
        return pedidoService.buscarPorNumero(numero);
    }

    public record PedidoRequest(String clienteCpf, List<ItemPedidoRequest> itens) {
    }

    public record AtualizarStatusRequest(String status) {
    }
}
