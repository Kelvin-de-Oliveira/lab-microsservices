package com.lab.estoque.service;

import com.lab.estoque.exception.EstoqueInsuficienteException;
import com.lab.estoque.exception.ProdutoNaoEncontradoException;
import com.lab.estoque.model.Produto;
import com.lab.estoque.repository.ProdutoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProdutoService {

    private final ProdutoRepository produtoRepository;

    public ProdutoService(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    public List<Produto> listarTodos() {
        return produtoRepository.findAll();
    }

    public Produto buscarPorId(Long id) {
        return produtoRepository.findById(id)
                .orElseThrow(ProdutoNaoEncontradoException::new);
    }

    @Transactional
    public void reservar(Long id, Integer quantidade) {
        Produto produto = buscarPorId(id);

        if (produto.getQuantidade() < quantidade) {
            throw new EstoqueInsuficienteException();
        }

        produto.setQuantidade(produto.getQuantidade() - quantidade);
        produtoRepository.save(produto);
    }
}