package com.ecommerce.pedidos.controller;

import com.ecommerce.pedidos.dto.AtualizarSituacaoRequest;
import com.ecommerce.pedidos.dto.PedidoRequest;
import com.ecommerce.pedidos.dto.PedidoResponse;
import com.ecommerce.pedidos.servico.PedidoService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {
    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    /** {@code situacao=aberto|pago|cancelado} e {@code pagina}/{@code tamanho} opcionais. */
    @GetMapping
    public List<PedidoResponse> listar(@RequestParam(name = "situacao", required = false) String situacao,
                                       @RequestParam(name = "pagina", required = false) Integer pagina,
                                       @RequestParam(name = "tamanho", required = false) Integer tamanho) {
        return pedidoService.listar(situacao, ParametrosConsulta.pagina(pagina, tamanho));
    }

    @GetMapping("/{numero}")
    public PedidoResponse buscarPorNumero(@PathVariable("numero") String numero) {
        return pedidoService.buscarPorNumero(numero);
    }

    @PostMapping
    public ResponseEntity<PedidoResponse> criar(@Valid @RequestBody PedidoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pedidoService.criar(request));
    }

    /**
     * PATCH, e não PUT: muda só a situação, sem reenviar o pedido inteiro.
     * {@code { "situacao": "CANCELADO" }} cancela (devolve estoque, estorna pagamento).
     */
    @PatchMapping("/{numero}")
    public PedidoResponse atualizarSituacao(@PathVariable("numero") String numero,
                                            @Valid @RequestBody AtualizarSituacaoRequest request) {
        return pedidoService.atualizarSituacao(numero, request.situacao());
    }

    /** Exclui só pedido ABERTO (204). Pago ou cancelado → 409; inexistente → 404. */
    @DeleteMapping("/{numero}")
    public ResponseEntity<Void> excluir(@PathVariable("numero") String numero) {
        pedidoService.excluir(numero);
        return ResponseEntity.noContent().build();
    }

    /** Remove um item de pedido aberto e devolve o estoque. */
    @DeleteMapping("/{numero}/itens/{itemId}")
    public PedidoResponse removerItem(@PathVariable("numero") String numero, @PathVariable("itemId") Long itemId) {
        return pedidoService.removerItem(numero, itemId);
    }
}
