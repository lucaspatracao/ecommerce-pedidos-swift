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

    /** {@code situacao=aberto|pago|cancelado} e {@code pagina}/{@code tamanho} opcionais. */
    @GetMapping("/pedidos")
    public List<PedidoResponse> listar(@RequestParam(name = "situacao", required = false) String situacao,
                                       @RequestParam(name = "pagina", required = false) Integer pagina,
                                       @RequestParam(name = "tamanho", required = false) Integer tamanho) {
        return pedidoService.listar(situacao, ParametrosConsulta.pagina(pagina, tamanho));
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

    /** Cancela: devolve o estoque e estorna o pagamento. Já cancelado → 409. */
    @PostMapping("/pedidos/{numero}/cancelar")
    public PedidoResponse cancelar(@PathVariable("numero") String numero) {
        return pedidoService.cancelar(numero);
    }

    /** Exclui só pedido ABERTO (204). Pago ou cancelado → 409; inexistente → 404. */
    @DeleteMapping("/pedidos/{numero}")
    public ResponseEntity<Void> excluir(@PathVariable("numero") String numero) {
        pedidoService.excluir(numero);
        return ResponseEntity.noContent().build();
    }

    /** Remove um item de pedido aberto e devolve o estoque; responde o pedido atualizado. */
    @DeleteMapping("/pedidos/{numero}/itens/{itemId}")
    public PedidoResponse removerItem(@PathVariable("numero") String numero, @PathVariable("itemId") Long itemId) {
        return pedidoService.removerItem(numero, itemId);
    }
}
