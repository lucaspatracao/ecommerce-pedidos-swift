package com.ecommerce.pedidos.controller;

import com.ecommerce.pedidos.dto.AtualizarStatusRequest;
import com.ecommerce.pedidos.dto.PedidoRequest;
import com.ecommerce.pedidos.dto.PedidoResponse;
import com.ecommerce.pedidos.servico.PedidoService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
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
    public List<PedidoResponse> listar() {
        return pedidoService.listarTodos();
    }

    @GetMapping("/pedidos/{numero}")
    public PedidoResponse buscarPorNumero(@PathVariable("numero") String numero) {
        return pedidoService.buscarPorNumero(numero);
    }

    @PostMapping("/pedidos")
    public ResponseEntity<PedidoResponse> criar(@Valid @RequestBody PedidoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pedidoService.criar(request));
    }

    @PutMapping("/pedidos/{numero}/status")
    public PedidoResponse atualizarStatus(@PathVariable("numero") String numero,
                                          @Valid @RequestBody AtualizarStatusRequest request) {
        return pedidoService.atualizarStatus(numero, request.status());
    }

    /** Novo: exclui pedido não pago (204). Pedido pago → 409; inexistente → 404. */
    @DeleteMapping("/pedidos/{numero}")
    public ResponseEntity<Void> excluir(@PathVariable("numero") String numero) {
        pedidoService.excluir(numero);
        return ResponseEntity.noContent().build();
    }
}
