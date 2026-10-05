package com.lab.pagamento.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.QueueBuilder;

@Configuration
public class RabbitConfig {

    public static final String QUEUE = "pedido.criado";
    public static final String EXCHANGE = "pedidos.exchange";
    public static final String PAGAMENTO_PROCESSADO_QUEUE = "pagamento.processado";
    public static final String PAGAMENTO_PROCESSADO_ROUTING_KEY = "pagamento.processado";

    @Bean
    public Queue pedidoCriadoQueue() {
        return new Queue(QUEUE, true);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public DirectExchange pedidosExchange() {
        return new DirectExchange(EXCHANGE);
    }

    @Bean
    public Queue pagamentoProcessadoQueue() {
        return QueueBuilder.durable(PAGAMENTO_PROCESSADO_QUEUE).build();
    }

    @Bean
    public Binding pagamentoProcessadoBinding(Queue pagamentoProcessadoQueue, DirectExchange pedidosExchange) {
        return BindingBuilder.bind(pagamentoProcessadoQueue)
                .to(pedidosExchange)
                .with(PAGAMENTO_PROCESSADO_ROUTING_KEY);
    }
}