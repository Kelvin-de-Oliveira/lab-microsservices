package com.lab.estoque.controller;

import com.lab.estoque.dto.ReservaRequest;
import com.lab.estoque.model.Produto;
import com.lab.estoque.service.ProdutoService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@RestController
@RequestMapping("/produtos")
public class ProdutoController {

    private final ProdutoService produtoService;

    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    @GetMapping
    public List<Produto> listarTodos() {
        return produtoService.listarTodos();
    }

    @GetMapping("/{id}")
    public Produto buscarPorId(@PathVariable Long id) {
        return produtoService.buscarPorId(id);
    }

    @PutMapping("/{id}/reservar")
    public void reservar(@PathVariable Long id,
                         @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
                         @Valid @RequestBody ReservaRequest request) {
        produtoService.reservar(id, request.quantidade(), correlationId);
    }
}