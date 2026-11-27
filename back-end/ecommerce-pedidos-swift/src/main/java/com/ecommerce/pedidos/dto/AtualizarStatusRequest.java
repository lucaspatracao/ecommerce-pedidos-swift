package com.ecommerce.pedidos.dto;

/** Corpo de PUT {@code /api/pedidos/{numero}/status}. */
public record AtualizarStatusRequest(String status) {
}
