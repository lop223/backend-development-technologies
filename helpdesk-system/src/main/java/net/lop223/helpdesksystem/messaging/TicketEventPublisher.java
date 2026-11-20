package net.lop223.helpdesksystem.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Component
@RequiredArgsConstructor
public class TicketEventPublisher {
    private final RabbitTemplate rabbitTemplate;

    public void publishAfterCommit(TicketEvent event) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            publish(event);
                        }
                    }
            );
        } else {
            publish(event);
        }
    }

    private void publish(TicketEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.TICKET_EXCHANGE,
                RabbitMQConfig.TICKET_ROUTING_KEY,
                event
        );
        log.info("Published ticket event {}: action={}, ticketId={}",
                event.eventId(), event.action(), event.ticketId());
    }
}
