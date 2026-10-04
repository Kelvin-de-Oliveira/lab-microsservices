package com.lab.estoque.exception;

public class EstoqueInsuficienteException extends RuntimeException{
    public EstoqueInsuficienteException(){
        super("Estoque insuficiente!");
    }
}
