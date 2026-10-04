package com.lab.estoque.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Entity
public class Produto {

    @Id
    private Long id;

    @Column(length = 100)
    private String nome;

    private Integer quantidade;

    protected Produto() {
    }

    public Produto(Long id, String nome, Integer quantidade) {
        this.id = id;
        this.nome = nome;
        this.quantidade = quantidade;
    }


}