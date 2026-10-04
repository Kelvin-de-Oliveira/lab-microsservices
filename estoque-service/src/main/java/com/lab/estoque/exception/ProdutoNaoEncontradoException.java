package com.lab.estoque.exception;

public class ProdutoNaoEncontradoException extends  RuntimeException{
    public ProdutoNaoEncontradoException(){
        super("Produto não encontrado!");
    }
}
