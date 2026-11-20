package net.lop223.notificationservice.messaging;

import net.lop223.notificationservice.entity.TicketEventRecord;
import net.lop223.notificationservice.repository.TicketEventRecordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketEventConsumerTest {
    @Mock
    private TicketEventRecordRepository repository;

    @Test
    void duplicateEventIsProcessedOnlyOnce() {
        TicketEvent event = new TicketEvent(
                "event-123",
                "TICKET_CREATED",
                42L,
                "Ticket created",
                Instant.parse("2026-11-19T20:00:00Z")
        );
        when(repository.findByEventId("event-123"))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(new TicketEventRecord(
                        event.eventId(),
                        event.action(),
                        event.ticketId(),
                        event.details(),
                        event.occurredAt()
                )));
        TicketEventConsumer consumer = new TicketEventConsumer(repository);

        consumer.handle(event);
        consumer.handle(event);

        ArgumentCaptor<TicketEventRecord> captor =
                ArgumentCaptor.forClass(TicketEventRecord.class);
        verify(repository, times(1)).save(captor.capture());
        assertEquals("event-123", captor.getValue().getEventId());
        verify(repository, times(2)).findByEventId("event-123");
    }
}
