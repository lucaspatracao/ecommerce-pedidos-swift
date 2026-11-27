package com.ecommerce.pedidos.controller;

import com.ecommerce.pedidos.dto.PagamentoRequest;
import com.ecommerce.pedidos.dto.PagamentoResumoResponse;
import com.ecommerce.pedidos.servico.PagamentoService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Pagamento é sub-recurso do pedido (Aula 15): cada pedido tem no máximo um,
 * então o endereço é {@code /api/pedidos/{numero}/pagamento}, no singular.
 * A lista geral {@code /api/pagamentos} continua para a tela de pagamentos.
 */
@RestController
@RequestMapping("/api")
public class PagamentoController {
    private final PagamentoService pagamentoService;

    public PagamentoController(PagamentoService pagamentoService) {
        this.pagamentoService = pagamentoService;
    }

    @GetMapping("/pagamentos")
    public List<PagamentoResumoResponse> listarPagamentos() {
        return pagamentoService.listar();
    }

    /**
     * Paga o pedido: 201 + {@code Location: /api/pedidos/{numero}/pagamento}.
     * Já pago ou recusado → 409; pedido inexistente → 404.
     */
    @PostMapping("/pedidos/{numero}/pagamento")
    public ResponseEntity<Map<String, Object>> pagar(@PathVariable("numero") String numero,
                                                     @Valid @RequestBody PagamentoRequest request,
                                                     UriComponentsBuilder uri) {
        Map<String, Object> criado = pagamentoService.registrar(numero, request);
        URI endereco = uri.path("/api/pedidos/{numero}/pagamento").buildAndExpand(numero).toUri();
        return ResponseEntity.created(endereco).body(criado);
    }

    /** Pagamento do pedido; 404 se o pedido não existir ou ainda não tiver pagamento. */
    @GetMapping("/pedidos/{numero}/pagamento")
    public Map<String, Object> buscar(@PathVariable("numero") String numero) {
        return pagamentoService.buscarPorPedido(numero);
    }
}
