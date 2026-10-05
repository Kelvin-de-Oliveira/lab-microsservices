package com.lab.pagamento.consumer;

import com.lab.pagamento.dto.PedidoEvento;
import com.lab.pagamento.model.Pagamento;
import com.lab.pagamento.model.StatusPagamento;
import com.lab.pagamento.repository.PagamentoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@Component
public class PedidoCriadoConsumer {

    private static final Logger log = LoggerFactory.getLogger(PedidoCriadoConsumer.class);
        private static final double PROBABILIDADE_APROVACAO = 0.8;

    private final PagamentoRepository repository;

    public PedidoCriadoConsumer(PagamentoRepository repository) {
        this.repository = repository;
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
    }
}