package com.ecommerce.pedidos.servico;

import com.ecommerce.pedidos.dto.DashboardResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    /** Abaixo deste saldo o produto aparece no alerta de estoque crítico. */
    static final int LIMITE_ESTOQUE_CRITICO = 15;
    static final int QUANTIDADE_PEDIDOS_RECENTES = 5;

    private final ClienteService clienteService;
    private final ProdutoService produtoService;
    private final PedidoService pedidoService;

    public DashboardServiceImpl(ClienteService clienteService, ProdutoService produtoService,
                                PedidoService pedidoService) {
        this.clienteService = clienteService;
        this.produtoService = produtoService;
        this.pedidoService = pedidoService;
    }

    @Override
    public DashboardResponse gerarResumo() {
        return new DashboardResponse(
            clienteService.contar(),
            pedidoService.contar(),
            produtoService.contar(),
            pedidoService.calcularReceitaTotal(),
            produtoService.listarEstoqueCritico(LIMITE_ESTOQUE_CRITICO),
            pedidoService.listarRecentes(QUANTIDADE_PEDIDOS_RECENTES)
        );
    }
}
