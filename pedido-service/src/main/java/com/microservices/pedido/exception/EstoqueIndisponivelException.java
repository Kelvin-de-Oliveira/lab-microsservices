package com.microservices.pedido.exception;

public class EstoqueIndisponivelException extends RuntimeException {

    public EstoqueIndisponivelException() {
        super("Estoque indisponível");
    }
}