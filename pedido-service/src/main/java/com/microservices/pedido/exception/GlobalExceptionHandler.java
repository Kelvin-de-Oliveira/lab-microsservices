package com.microservices.pedido.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ReservaRecusadaException.class)
    public ResponseEntity<String> tratarReservaRecusada(ReservaRecusadaException ex) {
        return ResponseEntity.status(ex.getStatus())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ex.getCorpo());
    }

    @ExceptionHandler(EstoqueIndisponivelException.class)
    public ResponseEntity<String> tratarEstoqueIndisponivel(EstoqueIndisponivelException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"mensagem\":\"" + ex.getMessage() + "\"}");
    }
}