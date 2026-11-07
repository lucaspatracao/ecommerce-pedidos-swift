package com.ecommerce.pedidos.controller;

import com.ecommerce.pedidos.modelo.Cliente;
import com.ecommerce.pedidos.modelo.Pedido;
import com.ecommerce.pedidos.servico.ClienteService;
import com.ecommerce.pedidos.servico.PedidoService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/clientes")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
public class ClienteController {
    private final ClienteService clienteService;
    private final PedidoService pedidoService;

    public ClienteController(ClienteService clienteService, PedidoService pedidoService) {
        this.clienteService = clienteService;
        this.pedidoService = pedidoService;
    }

    /** Por padrão lista só clientes ativos; {@code situacao=inativos|todos} para os demais. */
    @GetMapping
    public List<Cliente> listar(
            @RequestParam(required = false) String situacao,
            @RequestParam(required = false) Integer pagina,
            @RequestParam(required = false) Integer tamanho) {
        return clienteService.listar(ParametrosConsulta.visibilidade(situacao),
            ParametrosConsulta.pagina(pagina, tamanho));
    }

    @GetMapping("/{cpf}")
    public Cliente buscar(@PathVariable String cpf) {
        return clienteService.buscarPorCpf(cpf);
    }

    /** Histórico de pedidos do cliente (também para cliente inativo). */
    @GetMapping("/{cpf}/pedidos")
    public List<Pedido> listarPedidos(@PathVariable String cpf,
            @RequestParam(required = false) Integer pagina,
            @RequestParam(required = false) Integer tamanho) {
        return pedidoService.listarPorCliente(cpf, ParametrosConsulta.pagina(pagina, tamanho));
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
            cpf,
            request.email(),
            request.telefone(),
            request.endereco()
        );
        return clienteService.atualizar(cpf, clienteAtualizado);
    }

    /** Exclusão definitiva: 409 se o cliente possuir pedidos (use a inativação). */
    @DeleteMapping("/{cpf}")
    public ResponseEntity<Void> excluir(@PathVariable String cpf) {
        clienteService.excluir(cpf);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{cpf}/inativar")
    public ResponseEntity<Void> inativar(@PathVariable String cpf) {
        clienteService.inativar(cpf);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{cpf}/reativar")
    public Cliente reativar(@PathVariable String cpf) {
        return clienteService.reativar(cpf);
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
