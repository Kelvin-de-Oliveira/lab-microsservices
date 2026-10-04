package com.microservices.pedido.service;

import com.microservices.pedido.dto.CriarPedidoRequest;
import com.microservices.pedido.model.Pedido;
import com.microservices.pedido.model.StatusPedido;
import com.microservices.pedido.repository.PedidoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class PedidoService {

    private final PedidoRepository repository;
    private final RestTemplate restTemplate;
    private final String estoqueUrl;

    public PedidoService(PedidoRepository repository,
                         RestTemplate restTemplate,
                         @Value("${estoque.url}") String estoqueUrl) {
        this.repository = repository;
        this.restTemplate = restTemplate;
        this.estoqueUrl = estoqueUrl;
    }

    /** Todo novo pedido nasce com status AGUARDANDO_PAGAMENTO. */
    public Pedido criar(CriarPedidoRequest request) {
        reservarEstoque(request.produtoId(), request.quantidade());

        Pedido pedido = new Pedido();
        pedido.setProdutoId(request.produtoId());
        pedido.setQuantidade(request.quantidade());
        pedido.setStatus(StatusPedido.AGUARDANDO_PAGAMENTO.name());
        return repository.save(pedido);
    }

    private void reservarEstoque(Long produtoId, Integer quantidade) {
        String url = estoqueUrl + "/produtos/" + produtoId + "/reservar";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Integer>> entity =
                new HttpEntity<>(Map.of("quantidade", quantidade), headers);

        try {
            restTemplate.exchange(url, HttpMethod.PUT, entity, Void.class);
        } catch (HttpClientErrorException e) {
            throw new ReservaRecusadaException(e.getStatusCode(), e.getResponseBodyAsString());
        } catch (ResourceAccessException | HttpServerErrorException e) {
            throw new EstoqueIndisponivelException();
        }
    }

    public List<Pedido> listar() {
        return repository.findAll();
    }

    public Optional<Pedido> buscarPorId(Long id) {
        return repository.findById(id);
    }
}