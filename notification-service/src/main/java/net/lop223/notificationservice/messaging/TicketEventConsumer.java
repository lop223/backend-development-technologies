package net.lop223.notificationservice.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lop223.notificationservice.entity.TicketEventRecord;
import net.lop223.notificationservice.repository.TicketEventRecordRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class TicketEventConsumer {
    private final TicketEventRecordRepository repository;

    @Transactional
    @RabbitListener(queues = RabbitMQConfig.TICKET_QUEUE)
    public void handle(TicketEvent event) {
        if (repository.findByEventId(event.eventId()).isPresent()) {
            log.info("Ignored duplicate ticket event {}", event.eventId());
            return;
        }

        repository.save(new TicketEventRecord(
                event.eventId(),
                event.action(),
                event.ticketId(),
                event.details(),
                event.occurredAt()
        ));
        log.info("Stored ticket event {}: action={}, ticketId={}",
                event.eventId(), event.action(), event.ticketId());
    }
}
