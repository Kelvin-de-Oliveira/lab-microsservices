package com.lab.pagamento.dto;

public record PedidoEvento(
        Long pedidoId,
        Long produtoId,
        Integer quantidade,
        String correlationId
) {
}