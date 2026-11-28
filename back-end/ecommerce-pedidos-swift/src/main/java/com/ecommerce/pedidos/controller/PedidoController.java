package com.ecommerce.pedidos.controller;

import com.ecommerce.pedidos.dto.AtualizarSituacaoRequest;
import com.ecommerce.pedidos.dto.PedidoRequest;
import com.ecommerce.pedidos.dto.PedidoResponse;
import com.ecommerce.pedidos.servico.PedidoService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
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
import org.springframework.web.util.UriComponentsBuilder;

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

    /** 201 + {@code Location: /api/pedidos/{numero}} + o pedido criado. */
    @PostMapping
    public ResponseEntity<PedidoResponse> criar(@Valid @RequestBody PedidoRequest request, UriComponentsBuilder uri) {
        PedidoResponse criado = pedidoService.criar(request);
        URI endereco = uri.path("/api/pedidos/{numero}").buildAndExpand(criado.numero()).toUri();
        return ResponseEntity.created(endereco).body(criado);
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

    /** Remove um item de pedido aberto e devolve o estoque (204; o pedido atualizado está em GET /{numero}). */
    @DeleteMapping("/{numero}/itens/{itemId}")
    public ResponseEntity<Void> removerItem(@PathVariable("numero") String numero, @PathVariable("itemId") Long itemId) {
        pedidoService.removerItem(numero, itemId);
        return ResponseEntity.noContent().build();
    }
}
