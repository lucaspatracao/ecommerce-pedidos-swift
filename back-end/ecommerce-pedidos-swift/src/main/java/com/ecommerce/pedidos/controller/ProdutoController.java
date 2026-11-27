package com.ecommerce.pedidos.controller;

import com.ecommerce.pedidos.dto.ProdutoRequest;
import com.ecommerce.pedidos.dto.ProdutoResponse;
import com.ecommerce.pedidos.servico.ProdutoService;
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
@RequestMapping("/api/produtos")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
public class ProdutoController {
    private final ProdutoService produtoService;

    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    @GetMapping
    public List<ProdutoResponse> listar() {
        return produtoService.listarTodos();
    }

    @GetMapping("/{codigo}")
    public ProdutoResponse buscar(@PathVariable("codigo") String codigo) {
        return produtoService.buscarPorCodigo(codigo);
    }

    @PostMapping
    public ResponseEntity<ProdutoResponse> cadastrar(@Valid @RequestBody ProdutoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(produtoService.cadastrar(request));
    }

    @PutMapping("/{codigo}")
    public ProdutoResponse atualizar(@PathVariable("codigo") String codigo, @Valid @RequestBody ProdutoRequest request) {
        return produtoService.atualizar(codigo, request);
    }

    @DeleteMapping("/{codigo}")
    public ResponseEntity<Void> excluir(@PathVariable("codigo") String codigo) {
        produtoService.excluir(codigo);
        return ResponseEntity.noContent().build();
    }
}
