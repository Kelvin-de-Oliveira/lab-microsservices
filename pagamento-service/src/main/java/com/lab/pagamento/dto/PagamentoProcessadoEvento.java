package com.lab.pagamento.dto;

public record PagamentoProcessadoEvento(Long pedidoId, String status, String correlationId) {
}