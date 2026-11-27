package com.ecommerce.pedidos.controller;

import com.ecommerce.pedidos.dto.PagamentoRegistradoResponse;
import com.ecommerce.pedidos.dto.PagamentoRequest;
import com.ecommerce.pedidos.dto.PagamentoResumoResponse;
import com.ecommerce.pedidos.servico.PagamentoService;
import java.util.List;
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
    private final PagamentoService pagamentoService;

    public PagamentoController(PagamentoService pagamentoService) {
        this.pagamentoService = pagamentoService;
    }

    @GetMapping("/pagamentos")
    public List<PagamentoResumoResponse> listarPagamentos() {
        return pagamentoService.listar();
    }

    @PostMapping("/pagamentos")
    public ResponseEntity<PagamentoRegistradoResponse> registrar(@RequestBody PagamentoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pagamentoService.registrar(request));
    }
}
