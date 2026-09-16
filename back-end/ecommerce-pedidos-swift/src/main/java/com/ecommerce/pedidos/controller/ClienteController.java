package com.ecommerce.pedidos.controller;

import com.ecommerce.pedidos.model.Cliente;
import com.ecommerce.pedidos.service.ClienteService;
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
@RequestMapping("/api/clientes")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
public class ClienteController {
    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping
    public List<Cliente> listar() {
        return clienteService.listarTodos();
    }

    @PostMapping
    public ResponseEntity<Cliente> criar(@RequestBody ClienteRequest request) {
        Cliente cliente = new Cliente(
            request.nome(),
            request.cpf(),
            request.email(),
            request.telefone(),
            request.endereco()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteService.salvar(cliente));
    }

    @PutMapping("/{cpf}")
    public Cliente atualizar(@PathVariable String cpf, @RequestBody ClienteRequest request) {
        Cliente clienteAtualizado = new Cliente(
            request.nome(),
            request.cpf(),
            request.email(),
            request.telefone(),
            request.endereco()
        );
        return clienteService.atualizar(cpf, clienteAtualizado);
    }

    @DeleteMapping("/{cpf}")
    public ResponseEntity<Void> excluir(@PathVariable String cpf) {
        clienteService.excluir(cpf);
        return ResponseEntity.noContent().build();
    }

    public record ClienteRequest(
        String nome,
        String cpf,
        String email,
        String telefone,
        String endereco
    ) {
    }
}
