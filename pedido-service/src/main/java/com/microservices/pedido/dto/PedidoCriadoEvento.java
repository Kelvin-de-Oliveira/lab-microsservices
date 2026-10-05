package com.microservices.pedido.dto;

public record PedidoCriadoEvento(
        Long pedidoId,
        Long produtoId,
        Integer quantidade,
        String correlationId
) {
}