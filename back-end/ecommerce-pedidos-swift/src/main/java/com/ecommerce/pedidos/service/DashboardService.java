package com.ecommerce.pedidos.service;

import com.ecommerce.pedidos.model.Pedido;
import com.ecommerce.pedidos.model.Produto;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {
    private final ClienteService clienteService;
    private final ProdutoService produtoService;
    private final PedidoService pedidoService;

    public DashboardService(ClienteService clienteService, ProdutoService produtoService, PedidoService pedidoService) {
        this.clienteService = clienteService;
        this.produtoService = produtoService;
        this.pedidoService = pedidoService;
    }

    public Map<String, Object> gerarResumo() {
        List<Pedido> pedidos = pedidoService.listarTodos();
        List<Produto> produtos = produtoService.listarTodos();

        BigDecimal receitaTotal = pedidos.stream()
            .map(Pedido::calcularValorTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Produto> estoqueCritico = produtos.stream()
            .filter(produto -> produto.getQuantidadeEmEstoque() < 15)
            .toList();

        Map<String, Object> resposta = new HashMap<>();
        resposta.put("totalClientes", clienteService.contar());
        resposta.put("totalPedidos", pedidoService.contar());
        resposta.put("totalProdutos", produtoService.contar());
        resposta.put("receitaTotal", receitaTotal);
        resposta.put("estoqueCritico", estoqueCritico);
        resposta.put("pedidosRecentes", pedidos.stream().limit(5).toList());
        return resposta;
    }
}
