package com.microservices.pedido.service;

import com.microservices.pedido.dto.CriarPedidoRequest;
import com.microservices.pedido.model.Pedido;
import com.microservices.pedido.model.StatusPedido;
import com.microservices.pedido.repository.PedidoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PedidoService {

    private final PedidoRepository repository;

    public PedidoService(PedidoRepository repository) {
        this.repository = repository;
    }

    /** Todo novo pedido nasce com status AGUARDANDO_PAGAMENTO. */
    public Pedido criar(CriarPedidoRequest request) {
        Pedido pedido = new Pedido();
        pedido.setProdutoId(request.produtoId());
        pedido.setQuantidade(request.quantidade());
        pedido.setStatus(StatusPedido.AGUARDANDO_PAGAMENTO.name());
        return repository.save(pedido);
    }

    public List<Pedido> listar() {
        return repository.findAll();
    }

    public Optional<Pedido> buscarPorId(Long id) {
        return repository.findById(id);
    }
}
