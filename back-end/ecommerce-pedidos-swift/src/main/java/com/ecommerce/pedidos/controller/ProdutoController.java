package com.ecommerce.pedidos.controller;

import com.ecommerce.pedidos.dto.EstoqueRequest;
import com.ecommerce.pedidos.dto.ProdutoRequest;
import com.ecommerce.pedidos.dto.ProdutoResponse;
import com.ecommerce.pedidos.servico.ProdutoService;
import com.ecommerce.pedidos.servico.Visibilidade;
import jakarta.validation.Valid;
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
     * {@code situacao=ativos|inativos|todos} (padrão: ativos), {@code nome} (trecho)
     * e {@code pagina}/{@code tamanho} opcionais.
     */
    @GetMapping
    public List<ProdutoResponse> listar(@RequestParam(name = "situacao", required = false) String situacao,
                                        @RequestParam(name = "nome", required = false) String nome,
                                        @RequestParam(name = "pagina", required = false) Integer pagina,
                                        @RequestParam(name = "tamanho", required = false) Integer tamanho) {
        return produtoService.listar(Visibilidade.deTexto(situacao), nome, ParametrosConsulta.pagina(pagina, tamanho));
    }

    @GetMapping("/{codigo}")
    public ProdutoResponse buscar(@PathVariable("codigo") String codigo) {
        return produtoService.buscarPorCodigo(codigo);
    }

    @PostMapping
    public ResponseEntity<ProdutoResponse> cadastrar(@Valid @RequestBody ProdutoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(produtoService.cadastrar(request));
    }

    /** Envie {@code versao} para o controle otimista: versão desatualizada → 409. */
    @PutMapping("/{codigo}")
    public ProdutoResponse atualizar(@PathVariable("codigo") String codigo, @Valid @RequestBody ProdutoRequest request) {
        return produtoService.atualizar(codigo, request);
    }

    /** Atualização parcial: só o estoque. */
    @PatchMapping("/{codigo}/estoque")
    public ProdutoResponse atualizarEstoque(@PathVariable("codigo") String codigo,
                                            @Valid @RequestBody EstoqueRequest request) {
        return produtoService.atualizarEstoque(codigo, request.quantidade());
    }

    /**
     * Padrão: soft delete (inativa). Com {@code definitivo=true} apaga de fato,
     * permitido só para produto nunca vendido (409 caso contrário).
     */
    @DeleteMapping("/{codigo}")
    public ResponseEntity<Void> excluir(@PathVariable("codigo") String codigo,
                                        @RequestParam(name = "definitivo", defaultValue = "false") boolean definitivo) {
        produtoService.excluir(codigo, definitivo);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{codigo}/inativar")
    public ResponseEntity<Void> inativar(@PathVariable("codigo") String codigo) {
        produtoService.inativar(codigo);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{codigo}/reativar")
    public ProdutoResponse reativar(@PathVariable("codigo") String codigo) {
        return produtoService.reativar(codigo);
    }
}
