package net.lop223.helpdesksystem.service.async.client;

public record TicketCreatedNotificationRequest(
        Long ticketId,
        String username
) {
}