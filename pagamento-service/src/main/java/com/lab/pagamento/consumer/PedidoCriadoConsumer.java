package com.lab.pagamento.consumer;

import com.lab.pagamento.config.RabbitConfig;
import com.lab.pagamento.dto.PagamentoProcessadoEvento;
import com.lab.pagamento.dto.PedidoEvento;
import com.lab.pagamento.model.Pagamento;
import com.lab.pagamento.model.StatusPagamento;
import com.lab.pagamento.repository.PagamentoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@Component
public class PedidoCriadoConsumer {

    private static final Logger log = LoggerFactory.getLogger(PedidoCriadoConsumer.class);
    private static final double PROBABILIDADE_APROVACAO = 0.8;

    private final PagamentoRepository repository;
    private final RabbitTemplate rabbitTemplate;

    public PedidoCriadoConsumer(PagamentoRepository repository, RabbitTemplate rabbitTemplate) {
        this.repository = repository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = "pedido.criado")
    public void consumir(PedidoEvento evento) {
        boolean aprovado = ThreadLocalRandom.current().nextDouble() < PROBABILIDADE_APROVACAO;
        StatusPagamento status = aprovado ? StatusPagamento.APROVADO : StatusPagamento.REJEITADO;

        Pagamento pagamento = new Pagamento();
        pagamento.setPedidoId(evento.pedidoId());
        pagamento.setStatus(status.name());
        repository.save(pagamento);

        log.info("correlationId={} Pagamento {} {}",
                evento.correlationId(),
                aprovado ? "aprovado" : "rejeitado",
                evento.pedidoId());

        PagamentoProcessadoEvento resultado = new PagamentoProcessadoEvento(
                evento.pedidoId(), status.name(), evento.correlationId());

        rabbitTemplate.convertAndSend(
                RabbitConfig.EXCHANGE,
                RabbitConfig.PAGAMENTO_PROCESSADO_ROUTING_KEY,
                resultado);

        log.info("correlationId={} Evento pagamento.processado publicado pedidoId={} status={}",
                evento.correlationId(), evento.pedidoId(), status);
    }
}