package com.ecommerce.pedidos.service;

import com.ecommerce.pedidos.model.Cliente;
import com.ecommerce.pedidos.model.Pedido;
import com.ecommerce.pedidos.model.Produto;
import com.ecommerce.pedidos.model.SituacaoPedido;
import com.ecommerce.pedidos.swift.util.PedidoUtils;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class PedidoService {
    private final Map<String, Pedido> pedidos = new ConcurrentHashMap<>();
    private final ClienteService clienteService;
    private final ProdutoService produtoService;

    public PedidoService(ClienteService clienteService, ProdutoService produtoService) {
        this.clienteService = clienteService;
        this.produtoService = produtoService;
    }

    public List<Pedido> listarTodos() {
        return pedidos.values().stream()
            .sorted(Comparator.comparing(Pedido::getData).reversed())
            .toList();
    }

    public Pedido buscarPorNumero(String numero) {
        if (numero == null || numero.isBlank()) {
            throw new IllegalArgumentException("Número do pedido não pode ser vazio.");
        }
        return pedidos.get(numero.trim());
    }

    public Pedido criarPedido(String clienteCpf, List<ItemPedidoRequest> itens) {
        Objects.requireNonNull(clienteCpf, "CPF do cliente não pode ser nulo.");
        Cliente cliente = clienteService.buscarPorCpf(clienteCpf);
        if (cliente == null) {
            throw new IllegalArgumentException("Cliente não encontrado para o CPF informado.");
        }

        Pedido pedido = new Pedido(cliente);
        if (itens == null || itens.isEmpty()) {
            throw new IllegalArgumentException("Pedido deve conter pelo menos um item.");
        }

        for (ItemPedidoRequest itemRequest : itens) {
            Produto produto = produtoService.buscarPorCodigo(itemRequest.codigo());
            if (produto == null) {
                throw new IllegalArgumentException("Produto não encontrado: " + itemRequest.codigo());
            }
            if (itemRequest.quantidade() <= 0) {
                throw new IllegalArgumentException("Quantidade do item deve ser maior que zero.");
            }
            if (!produto.temEstoqueDisponivel(itemRequest.quantidade())) {
                throw new IllegalArgumentException("Estoque insuficiente para o produto " + produto.getNome());
            }

            pedido.adicionarItem(produto, itemRequest.quantidade());
        }

        pedidos.put(pedido.getNumero(), pedido);
        return pedido;
    }

    public void atualizarStatus(String numero, SituacaoPedido situacao) {
        Pedido pedido = buscarPorNumero(numero);
        if (pedido == null) {
            throw new IllegalArgumentException("Pedido não encontrado.");
        }
        if (situacao == null) {
            throw new IllegalArgumentException("Situação do pedido é obrigatória.");
        }
        if (situacao == SituacaoPedido.CANCELADO) {
            pedido.cancelar();
            return;
        }
        if (situacao == SituacaoPedido.PAGO) {
            throw new IllegalStateException("Pagamento deve ser processado com uma forma de pagamento");
        }
        if (situacao != SituacaoPedido.ABERTO || pedido.getSituacao() != SituacaoPedido.ABERTO) {
            throw new IllegalStateException("Transição de situação inválida");
        }
    }

    public BigDecimal calcularReceitaTotal() {
        return listarTodos().stream()
            .map(Pedido::calcularValorTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public long contar() {
        return pedidos.size();
    }

    public List<Pedido> listarRecentes(int limite) {
        return listarTodos().stream().limit(limite).toList();
    }

    public record ItemPedidoRequest(String codigo, int quantidade) {
    }
}
