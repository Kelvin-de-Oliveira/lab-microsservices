package com.microservices.pedido.consumer;

import com.microservices.pedido.config.RabbitConfig;
import com.microservices.pedido.dto.PagamentoProcessadoEvento;
import com.microservices.pedido.model.StatusPedido;
import com.microservices.pedido.repository.PedidoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class PagamentoProcessadoConsumer {

    private static final Logger log = LoggerFactory.getLogger(PagamentoProcessadoConsumer.class);

    private final PedidoRepository repository;

    public PagamentoProcessadoConsumer(PedidoRepository repository) {
        this.repository = repository;
    }

    @RabbitListener(queues = RabbitConfig.PAGAMENTO_PROCESSADO_QUEUE)
    public void consumir(PagamentoProcessadoEvento evento) {
        repository.findById(evento.pedidoId()).ifPresentOrElse(pedido -> {
            StatusPedido novoStatus = "APROVADO".equals(evento.status())
                    ? StatusPedido.PAGO
                    : StatusPedido.REJEITADO;

            pedido.setStatus(novoStatus.name());
            repository.save(pedido);

            log.info("correlationId={} Pedido atualizado pedidoId={} status={}",
                    evento.correlationId(), pedido.getId(), novoStatus);
        }, () -> log.warn("correlationId={} Pedido nao encontrado pedidoId={}",
                evento.correlationId(), evento.pedidoId()));
    }
}