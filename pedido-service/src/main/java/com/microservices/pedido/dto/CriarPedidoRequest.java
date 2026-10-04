package com.microservices.pedido.dto;

public record CriarPedidoRequest(Long produtoId, Integer quantidade) {
}
