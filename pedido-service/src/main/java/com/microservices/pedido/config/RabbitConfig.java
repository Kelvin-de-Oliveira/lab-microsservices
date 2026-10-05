package com.microservices.pedido.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "pedidos.exchange";
    public static final String QUEUE = "pedido.criado";
    public static final String ROUTING_KEY = "pedido.criado";
    public static final String PAGAMENTO_PROCESSADO_QUEUE = "pagamento.processado";
    public static final String PAGAMENTO_PROCESSADO_ROUTING_KEY = "pagamento.processado";

    @Bean
    public DirectExchange pedidosExchange() {
        return new DirectExchange(EXCHANGE);
    }

    @Bean
    public Queue pedidoCriadoQueue() {
        return new Queue(QUEUE, true);
    }

    @Bean
    public Binding pedidoCriadoBinding(Queue pedidoCriadoQueue, DirectExchange pedidosExchange) {
        return BindingBuilder.bind(pedidoCriadoQueue).to(pedidosExchange).with(ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public Queue pagamentoProcessadoQueue() {
        return new Queue(PAGAMENTO_PROCESSADO_QUEUE, true);
    }

    @Bean
    public Binding pagamentoProcessadoBinding(Queue pagamentoProcessadoQueue, DirectExchange pedidosExchange) {
        return BindingBuilder.bind(pagamentoProcessadoQueue).to(pedidosExchange).with(PAGAMENTO_PROCESSADO_ROUTING_KEY);
    }
}