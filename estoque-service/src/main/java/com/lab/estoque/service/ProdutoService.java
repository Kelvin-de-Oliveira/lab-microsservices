package com.lab.estoque.service;

import com.lab.estoque.exception.EstoqueInsuficienteException;
import com.lab.estoque.exception.ProdutoNaoEncontradoException;
import com.lab.estoque.model.Produto;
import com.lab.estoque.repository.ProdutoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


@Service
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private static final Logger log = LoggerFactory.getLogger(ProdutoService.class);

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
    public void reservar(Long id, Integer quantidade, String correlationId) {
        Produto produto = buscarPorId(id);

        if (produto.getQuantidade() < quantidade) {
            throw new EstoqueInsuficienteException();
        }

        produto.setQuantidade(produto.getQuantidade() - quantidade);
        produtoRepository.save(produto);

        log.info("correlationId={} Produto reservado produtoId={} quantidade={}", correlationId, id, quantidade);
    }
}