package com.ecommerce.pedidos.controller;

import com.ecommerce.pedidos.modelo.Produto;
import com.ecommerce.pedidos.servico.ProdutoService;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/produtos")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
public class ProdutoController {
    private final ProdutoService produtoService;

    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    /**
     * Por padrão lista só produtos ativos. {@code situacao=inativos|todos}
     * permite consultar os inativos explicitamente; {@code nome} filtra por
     * trecho do nome; {@code pagina}/{@code tamanho} paginam.
     */
    @GetMapping
    public List<Produto> listar(
            @RequestParam(required = false) String situacao,
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) Integer pagina,
            @RequestParam(required = false) Integer tamanho) {
        if (nome != null && !nome.isBlank()) {
            return produtoService.buscarPorNome(nome);
        }
        return produtoService.listar(ParametrosConsulta.visibilidade(situacao),
            ParametrosConsulta.pagina(pagina, tamanho));
    }

    @GetMapping("/{codigo}")
    public Produto buscar(@PathVariable String codigo) {
        return produtoService.buscarPorCodigo(codigo);
    }

    @PostMapping
    public ResponseEntity<Produto> cadastrar(@RequestBody ProdutoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(produtoService.salvar(request.paraProduto()));
    }

    @PutMapping("/{codigo}")
    public Produto atualizar(@PathVariable String codigo, @RequestBody ProdutoRequest request) {
        return produtoService.atualizar(codigo, request.paraProduto(), request.versao());
    }

    /** Atualização parcial: altera somente o estoque. */
    @PatchMapping("/{codigo}/estoque")
    public Produto atualizarEstoque(@PathVariable String codigo, @RequestBody EstoqueRequest request) {
        if (request == null || request.quantidade() == null) {
            throw new IllegalArgumentException("Quantidade em estoque é obrigatória.");
        }
        return produtoService.atualizarEstoque(codigo, request.quantidade());
    }

    /**
     * Padrão: soft delete (inativa). Com {@code definitivo=true}, tenta a
     * exclusão física, permitida apenas para produto nunca vendido (409 caso
     * contrário).
     */
    @DeleteMapping("/{codigo}")
    public ResponseEntity<Void> excluir(@PathVariable String codigo,
            @RequestParam(defaultValue = "false") boolean definitivo) {
        if (definitivo) {
            produtoService.excluirDefinitivamente(codigo);
        } else {
            produtoService.excluir(codigo);
        }
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{codigo}/inativar")
    public ResponseEntity<Void> inativar(@PathVariable String codigo) {
        produtoService.inativar(codigo);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{codigo}/reativar")
    public Produto reativar(@PathVariable String codigo) {
        return produtoService.reativar(codigo);
    }

    public record ProdutoRequest(
        String codigo,
        String nome,
        String descricao,
        String preco,
        Integer estoque,
        Boolean disponivel,
        Integer versao
    ) {
        Produto paraProduto() {
            if (preco == null || preco.isBlank()) {
                throw new IllegalArgumentException("Preço do produto é obrigatório.");
            }
            Produto produto = new Produto(codigo, nome, new BigDecimal(preco), estoque == null ? 0 : estoque);
            produto.setDescricao(descricao);
            produto.setAtivo(disponivel == null || disponivel);
            return produto;
        }
    }

    public record EstoqueRequest(Integer quantidade) {
    }
}
