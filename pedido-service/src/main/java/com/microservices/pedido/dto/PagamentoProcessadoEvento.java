package com.microservices.pedido.dto;

public record PagamentoProcessadoEvento(Long pedidoId, String status, String correlationId) {
}