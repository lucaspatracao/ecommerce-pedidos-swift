package com.ecommerce.pedidos.controller;

import com.ecommerce.pedidos.model.Produto;
import com.ecommerce.pedidos.service.ProdutoService;
import java.math.BigDecimal;
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
    public List<Produto> listar() {
        return produtoService.listarTodos();
    }

    @PostMapping
    public ResponseEntity<Produto> cadastrar(@RequestBody ProdutoRequest request) {
        Produto produto = new Produto(
            request.codigo(),
            request.nome(),
            new BigDecimal(request.preco()),
            request.estoque()
        );
        produto.setDescricao(request.descricao());
        produto.setAtivo(request.disponivel());
        return ResponseEntity.status(HttpStatus.CREATED).body(produtoService.salvar(produto));
    }

    @PutMapping("/{codigo}")
    public Produto atualizar(@PathVariable String codigo, @RequestBody ProdutoRequest request) {
        Produto produtoAtualizado = new Produto(
            request.codigo(),
            request.nome(),
            new BigDecimal(request.preco()),
            request.estoque()
        );
        produtoAtualizado.setDescricao(request.descricao());
        produtoAtualizado.setAtivo(request.disponivel());
        return produtoService.atualizar(codigo, produtoAtualizado);
    }

    @DeleteMapping("/{codigo}")
    public ResponseEntity<Void> excluir(@PathVariable String codigo) {
        produtoService.excluir(codigo);
        return ResponseEntity.noContent().build();
    }

    public record ProdutoRequest(
        String codigo,
        String nome,
        String descricao,
        String preco,
        Integer estoque,
        Boolean disponivel
    ) {
    }
}
