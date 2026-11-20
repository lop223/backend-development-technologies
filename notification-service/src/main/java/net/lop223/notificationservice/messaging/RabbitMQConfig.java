package net.lop223.notificationservice.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String TICKET_EXCHANGE = "helpdesk.ticket.exchange";
    public static final String TICKET_QUEUE = "helpdesk.ticket.events";
    public static final String TICKET_ROUTING_KEY = "ticket.event";

    @Bean
    DirectExchange ticketExchange() {
        return new DirectExchange(TICKET_EXCHANGE, true, false);
    }

    @Bean
    Queue ticketQueue() {
        return new Queue(TICKET_QUEUE, true);
    }

    @Bean
    Binding ticketBinding(Queue ticketQueue, DirectExchange ticketExchange) {
        return BindingBuilder.bind(ticketQueue).to(ticketExchange).with(TICKET_ROUTING_KEY);
    }

    @Bean
    MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        return factory;
    }
}
