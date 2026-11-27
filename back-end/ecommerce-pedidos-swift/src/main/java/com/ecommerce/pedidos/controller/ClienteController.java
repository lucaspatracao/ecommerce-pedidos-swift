package com.ecommerce.pedidos.controller;

import com.ecommerce.pedidos.dto.ClienteRequest;
import com.ecommerce.pedidos.dto.ClienteResponse;
import com.ecommerce.pedidos.servico.ClienteService;
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

/**
 * Camada web de clientes: só traduz HTTP ↔ serviço.
 * Regras ficam no domínio e no serviço; erros viram JSON no ApiExceptionHandler.
 */
@RestController
@RequestMapping("/api/clientes")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
public class ClienteController {
    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping
    public List<ClienteResponse> listar() {
        return clienteService.listarTodos();
    }

    @GetMapping("/{cpf}")
    public ClienteResponse buscar(@PathVariable("cpf") String cpf) {
        return clienteService.buscarPorCpf(cpf);
    }

    @PostMapping
    public ResponseEntity<ClienteResponse> criar(@Valid @RequestBody ClienteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteService.cadastrar(request));
    }

    @PutMapping("/{cpf}")
    public ClienteResponse atualizar(@PathVariable("cpf") String cpf, @Valid @RequestBody ClienteRequest request) {
        return clienteService.atualizar(cpf, request);
    }

    @DeleteMapping("/{cpf}")
    public ResponseEntity<Void> excluir(@PathVariable("cpf") String cpf) {
        clienteService.excluir(cpf);
        return ResponseEntity.noContent().build();
    }
}
