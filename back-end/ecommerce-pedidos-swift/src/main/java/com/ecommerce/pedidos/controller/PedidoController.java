package com.ecommerce.pedidos.controller;

import com.ecommerce.pedidos.excecao.EstoqueInsuficienteException;
import com.ecommerce.pedidos.modelo.Pedido;
import com.ecommerce.pedidos.modelo.SituacaoPedido;
import com.ecommerce.pedidos.servico.PedidoService;
import com.ecommerce.pedidos.servico.PedidoService.ItemPedidoRequest;
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
import org.springframework.web.bind.annotation.RequestParam;
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
    public List<Pedido> listar(
            @RequestParam(required = false) String situacao,
            @RequestParam(required = false) Integer pagina,
            @RequestParam(required = false) Integer tamanho) {
        var paginacao = ParametrosConsulta.pagina(pagina, tamanho);
        if (situacao != null && !situacao.isBlank()) {
            return pedidoService.listarPorSituacao(SituacaoPedido.valueOf(situacao.trim().toUpperCase()), paginacao);
        }
        return pedidoService.listar(paginacao);
    }

    @GetMapping("/pedidos/{numero}")
    public Pedido buscarPorNumero(@PathVariable String numero) {
        return pedidoService.buscarPorNumero(numero);
    }

    /** Finalização transacional: pedido + itens + baixa de estoque, tudo ou nada. */
    @PostMapping("/pedidos")
    public ResponseEntity<Pedido> criar(@RequestBody PedidoRequest request) throws EstoqueInsuficienteException {
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

    /** Cancela (pedido pago não é excluído; muda de situação e devolve o estoque). */
    @PostMapping("/pedidos/{numero}/cancelar")
    public Pedido cancelar(@PathVariable String numero) {
        return pedidoService.cancelar(numero);
    }

    /** Exclui pedido em aberto (itens saem em cascata). Pago/cancelado: 409. */
    @DeleteMapping("/pedidos/{numero}")
    public ResponseEntity<Void> excluir(@PathVariable String numero) {
        pedidoService.excluir(numero);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/pedidos/{numero}/itens/{itemId}")
    public Pedido removerItem(@PathVariable String numero, @PathVariable Long itemId) {
        return pedidoService.removerItem(numero, itemId);
    }

    public record PedidoRequest(String clienteCpf, List<ItemPedidoRequest> itens) {
    }

    public record AtualizarStatusRequest(String status) {
    }
}
