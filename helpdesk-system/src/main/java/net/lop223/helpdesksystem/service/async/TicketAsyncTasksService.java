package net.lop223.helpdesksystem.service.async;

import lombok.RequiredArgsConstructor;
import net.lop223.helpdesksystem.service.async.client.NotificationServiceClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class TicketAsyncTasksService {

    private final NotificationServiceClient notificationServiceClient;

    @Async("ticketExecutor")
    public CompletableFuture<String> sendCreatedEmail(Long ticketId, String username) {
        notificationServiceClient.sendTicketCreatedEmail(ticketId, username);
        return CompletableFuture.completedFuture(
                "Notification service accepted ticket-created email for ticket " + ticketId
        );
    }

    @Async("ticketExecutor")
    public CompletableFuture<String> generatePdfReceipt(Long ticketId) {
        notificationServiceClient.generatePdfReceipt(ticketId);
        return CompletableFuture.completedFuture(
                "Notification service accepted PDF request for ticket " + ticketId
        );
    }

    @Async("ticketExecutor")
    public CompletableFuture<String> notifyAssignedAgent(Long ticketId) {
        notificationServiceClient.notifyAssignedAgent(ticketId);
        return CompletableFuture.completedFuture(
                "Notification service accepted agent webhook for ticket " + ticketId
        );
    }
}