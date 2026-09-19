package com.ecommerce.pedidos.controller;

import com.ecommerce.pedidos.model.CartaoCredito;
import com.ecommerce.pedidos.model.FormaPagamento;
import com.ecommerce.pedidos.model.Boleto;
import com.ecommerce.pedidos.model.Pix;
import com.ecommerce.pedidos.service.PagamentoService;
import java.math.BigDecimal;
import java.time.LocalDate;
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

    public PagamentoController() {
        this.service = new PagamentoService();
    }

    @GetMapping("/pagamentos")
    public List<Map<String, Object>> listarPagamentos() {
        List<Map<String, Object>> resposta = new ArrayList<>();

        for (FormaPagamento pagamento : service.listarPagamentos()) {
            Map<String, Object> item = new HashMap<>();
            item.put("tipo", pagamento.getClass().getSimpleName());
            item.put("valor", pagamento.getValor());
            item.put("status", pagamento.getSituacao().name());
            item.put("ultimaTransacao", pagamento.getDataDoPagamento().toString());

            if (pagamento instanceof CartaoCredito cartao) {
                item.put("metodo", cartao.getBandeira() + " •••• " + cartao.getNumeroMascarado());
            } else if (pagamento instanceof Boleto boleto) {
                item.put("metodo", "Código: " + boleto.getCodigoDeBarras());
            } else if (pagamento instanceof Pix pix) {
                item.put("metodo", "Chave: " + pix.getChave());
            } else {
                item.put("metodo", "Pagamento registrado");
            }

            resposta.add(item);
        }

        return resposta;
    }

    @PostMapping("/pagamentos")
    public ResponseEntity<Map<String, Object>> registrar(@RequestBody PagamentoRequest request) {
        FormaPagamento pagamento;

        switch (request.tipo()) {
            case "CARTAO" -> pagamento = new CartaoCredito(
                new BigDecimal(request.valor()),
                request.numero(),
                request.bandeira(),
                Integer.parseInt(request.parcelas())
            );
            case "BOLETO" -> pagamento = new Boleto(
                new BigDecimal(request.valor()),
                LocalDate.parse(request.vencimento()),
                request.codigo()
            );
            case "PIX" -> pagamento = new Pix(
                new BigDecimal(request.valor()),
                request.chave(),
                request.tipoChave()
            );
            default -> throw new IllegalArgumentException("Tipo de pagamento inválido.");
        }

        service.registrar(pagamento);
        Map<String, Object> resposta = new HashMap<>();
        resposta.put("mensagem", "Pagamento registrado com sucesso.");
        resposta.put("pagamento", pagamento);
        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }

    public record PagamentoRequest(
        String tipo,
        String valor,
        String numero,
        String bandeira,
        String parcelas,
        String vencimento,
        String codigo,
        String chave,
        String tipoChave
    ) {
    }
}
