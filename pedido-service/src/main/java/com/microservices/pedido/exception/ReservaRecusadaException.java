package com.microservices.pedido.exception;

import org.springframework.http.HttpStatusCode;

public class ReservaRecusadaException extends RuntimeException {

    private final HttpStatusCode status;
    private final String corpo;

    public ReservaRecusadaException(HttpStatusCode status, String corpo) {
        super(corpo);
        this.status = status;
        this.corpo = corpo;
    }

    public HttpStatusCode getStatus() {
        return status;
    }

    public String getCorpo() {
        return corpo;
    }
}