package net.lop223.notificationservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "ticket_event_records")
@Getter
@NoArgsConstructor
public class TicketEventRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, unique = true)
    private String eventId;

    @Column(nullable = false)
    private String action;

    @Column(name = "ticket_id", nullable = false)
    private Long ticketId;

    @Column(nullable = false)
    private String details;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

    public TicketEventRecord(
            String eventId,
            String action,
            Long ticketId,
            String details,
            Instant occurredAt
    ) {
        this.eventId = eventId;
        this.action = action;
        this.ticketId = ticketId;
        this.details = details;
        this.occurredAt = occurredAt;
        this.receivedAt = Instant.now();
    }
}
