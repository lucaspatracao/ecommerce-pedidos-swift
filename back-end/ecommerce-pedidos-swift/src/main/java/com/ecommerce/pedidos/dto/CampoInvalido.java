package com.ecommerce.pedidos.dto;

/** Um campo recusado pelo Bean Validation: nome do campo no JSON e a mensagem a mostrar junto dele. */
public record CampoInvalido(String campo, String mensagem) {
}
