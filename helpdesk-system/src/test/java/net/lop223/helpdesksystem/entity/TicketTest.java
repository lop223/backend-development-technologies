package net.lop223.helpdesksystem.entity;

import net.lop223.helpdesksystem.utils.enums.TicketStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TicketTest {

    private static final LocalDateTime PAST = LocalDateTime.of(2020, 1, 1, 0, 0);

    @Test
    @DisplayName("onCreate: встановлює createdAt/updatedAt і статус NEW за замовчуванням")
    void onCreate_statusNotSet_setsTimestampsAndDefaultStatusNew() {
        Ticket ticket = new Ticket();

        ticket.onCreate();

        assertNotNull(ticket.getCreatedAt());
        assertNotNull(ticket.getUpdatedAt());
        assertTrue(ticket.getCreatedAt().isAfter(PAST));
        assertEquals(TicketStatus.NEW, ticket.getStatus());
    }

    @Test
    @DisplayName("onCreate: не перезаписує явно заданий статус")
    void onCreate_statusAlreadySet_keepsExistingStatus() {
        Ticket ticket = Ticket.builder().status(TicketStatus.IN_PROGRESS).build();

        ticket.onCreate();

        assertEquals(TicketStatus.IN_PROGRESS, ticket.getStatus());
        assertNotNull(ticket.getCreatedAt());
    }

    @Test
    @DisplayName("onUpdate: оновлює лише updatedAt, не змінюючи createdAt і статус")
    void onUpdate_existingTicket_refreshesUpdatedAtOnly() {
        Ticket ticket = Ticket.builder()
                .status(TicketStatus.RESOLVED)
                .createdAt(PAST)
                .updatedAt(PAST)
                .build();

        ticket.onUpdate();

        assertEquals(PAST, ticket.getCreatedAt());
        assertTrue(ticket.getUpdatedAt().isAfter(PAST));
        assertEquals(TicketStatus.RESOLVED, ticket.getStatus());
    }
}
