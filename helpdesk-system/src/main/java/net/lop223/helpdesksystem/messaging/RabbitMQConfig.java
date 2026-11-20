package net.lop223.helpdesksystem.messaging;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String TICKET_EXCHANGE = "helpdesk.ticket.exchange";
    public static final String TICKET_ROUTING_KEY = "ticket.event";

    @Bean
    DirectExchange ticketExchange() {
        return new DirectExchange(TICKET_EXCHANGE, true, false);
    }

    @Bean
    MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        template.setExchange(TICKET_EXCHANGE);
        template.setRoutingKey(TICKET_ROUTING_KEY);
        return template;
    }
}
