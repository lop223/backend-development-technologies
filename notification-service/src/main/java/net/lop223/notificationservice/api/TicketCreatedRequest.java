package net.lop223.notificationservice.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record TicketCreatedRequest(
        @NotNull @Positive Long ticketId,
        @NotBlank String username
) {
}