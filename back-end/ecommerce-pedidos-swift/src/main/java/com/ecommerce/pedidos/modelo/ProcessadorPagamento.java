package com.ecommerce.pedidos.modelo;

import java.math.BigDecimal;

public interface ProcessadorPagamento {
    boolean processar(BigDecimal valor);

    String getComprovante();

    String getDescricao();

    default boolean permiteParcelamento() {
        return false;
    }
}
