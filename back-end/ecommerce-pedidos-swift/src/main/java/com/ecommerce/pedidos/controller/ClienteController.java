package com.ecommerce.pedidos.controller;

import com.ecommerce.pedidos.dto.AtivoRequest;
import com.ecommerce.pedidos.dto.ClienteRequest;
import com.ecommerce.pedidos.dto.ClienteResponse;
import com.ecommerce.pedidos.dto.PedidoResponse;
import com.ecommerce.pedidos.servico.ClienteService;
import com.ecommerce.pedidos.servico.PedidoService;
import com.ecommerce.pedidos.servico.Visibilidade;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

/**
 * Camada web de clientes: só traduz HTTP ↔ serviço.
 * Regras ficam no domínio e no serviço; erros viram JSON no ApiExceptionHandler.
 */
@RestController
@RequestMapping("/api/clientes")
public class ClienteController {
    private final ClienteService clienteService;
    private final PedidoService pedidoService;

    public ClienteController(ClienteService clienteService, PedidoService pedidoService) {
        this.clienteService = clienteService;
        this.pedidoService = pedidoService;
    }

    /** {@code situacao=ativos|inativos|todos} (padrão: ativos); {@code pagina}/{@code tamanho} opcionais. */
    @GetMapping
    public List<ClienteResponse> listar(@RequestParam(name = "situacao", required = false) String situacao,
                                        @RequestParam(name = "pagina", required = false) Integer pagina,
                                        @RequestParam(name = "tamanho", required = false) Integer tamanho) {
        return clienteService.listar(Visibilidade.deTexto(situacao), ParametrosConsulta.pagina(pagina, tamanho));
    }

    @GetMapping("/{cpf}")
    public ClienteResponse buscar(@PathVariable("cpf") String cpf) {
        return clienteService.buscarPorCpf(cpf);
    }

    /** Pedidos do cliente, inclusive se ele estiver inativo (histórico). */
    @GetMapping("/{cpf}/pedidos")
    public List<PedidoResponse> listarPedidos(@PathVariable("cpf") String cpf,
                                              @RequestParam(name = "pagina", required = false) Integer pagina,
                                              @RequestParam(name = "tamanho", required = false) Integer tamanho) {
        return pedidoService.listarPorCliente(cpf, ParametrosConsulta.pagina(pagina, tamanho));
    }

    @PostMapping
    public ResponseEntity<ClienteResponse> criar(@Valid @RequestBody ClienteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteService.cadastrar(request));
    }

    @PutMapping("/{cpf}")
    public ClienteResponse atualizar(@PathVariable("cpf") String cpf, @Valid @RequestBody ClienteRequest request) {
        return clienteService.atualizar(cpf, request);
    }

    /** Exclusão definitiva; cliente com pedidos → 409 (use o inativar). */
    @DeleteMapping("/{cpf}")
    public ResponseEntity<Void> excluir(@PathVariable("cpf") String cpf) {
        clienteService.excluir(cpf);
        return ResponseEntity.noContent().build();
    }

    /** PATCH parcial: {@code { "ativo": false }} inativa (soft delete) e {@code true} reativa. */
    @PatchMapping("/{cpf}")
    public ClienteResponse alterarAtivo(@PathVariable("cpf") String cpf, @Valid @RequestBody AtivoRequest request) {
        return clienteService.alterarAtivo(cpf, request.ativo());
    }
}
