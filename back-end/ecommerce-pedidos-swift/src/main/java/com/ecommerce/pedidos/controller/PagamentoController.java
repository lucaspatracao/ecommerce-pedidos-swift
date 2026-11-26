package com.ecommerce.pedidos.controller;

import com.ecommerce.pedidos.dto.PagamentoRequest;
import com.ecommerce.pedidos.modelo.FormaPagamento;
import com.ecommerce.pedidos.servico.FabricaFormaPagamento;
import com.ecommerce.pedidos.servico.PagamentoService;
import com.ecommerce.pedidos.servico.PedidoService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
public class PagamentoController {
    private final PagamentoService service;
    private final PedidoService pedidoService;
    private final FabricaFormaPagamento fabrica;

    public PagamentoController(PagamentoService service, PedidoService pedidoService, FabricaFormaPagamento fabrica) {
        this.service = service;
        this.pedidoService = pedidoService;
        this.fabrica = fabrica;
    }

    @GetMapping("/pagamentos")
    public List<Map<String, Object>> listarPagamentos() {
        List<Map<String, Object>> resposta = new ArrayList<>();

        for (FormaPagamento pagamento : service.listarPagamentos()) {
            Map<String, Object> item = new HashMap<>();
            item.put("tipo", pagamento.getDescricao());
            item.put("valor", pagamento.getValor());
            item.put("status", pagamento.getSituacao().name());
            item.put("ultimaTransacao", pagamento.getDataDoPagamento().toString());
            // Polimorfismo: cada subclasse sabe se descrever, sem instanceof.
            item.put("metodo", pagamento.descreverMetodo());
            resposta.add(item);
        }

        return resposta;
    }

    @PostMapping("/pagamentos")
    public ResponseEntity<Map<String, Object>> registrar(@RequestBody PagamentoRequest request) {
        if (request == null || request.pedidoNumero() == null || request.pedidoNumero().isBlank()) {
            throw new IllegalArgumentException("Número do pedido é obrigatório para registrar o pagamento.");
        }

        FormaPagamento pagamento = fabrica.criar(request);

        var pedido = pedidoService.buscarPorNumero(request.pedidoNumero());
        if (pedido == null) {
            throw new IllegalArgumentException("Pedido não encontrado.");
        }
        try {
            pedido.pagarCom(pagamento);
        } catch (Exception ex) {
            if (ex instanceof RuntimeException runtime) {
                throw runtime;
            }
            throw new IllegalStateException(ex.getMessage(), ex);
        }
        service.registrar(pagamento);
        Map<String, Object> resposta = new HashMap<>();
        resposta.put("mensagem", "Pagamento registrado com sucesso.");
        resposta.put("pagamento", pagamento);
        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }
}
