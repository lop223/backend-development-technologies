package net.lop223.notificationservice.api;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record TicketOperationRequest(
        @NotNull @Positive Long ticketId
) {
}