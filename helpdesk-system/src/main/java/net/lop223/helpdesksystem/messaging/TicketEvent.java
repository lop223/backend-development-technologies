package net.lop223.helpdesksystem.messaging;

import java.time.Instant;

public record TicketEvent(
        String eventId,
        String action,
        Long ticketId,
        String details,
        Instant occurredAt
) {
}
