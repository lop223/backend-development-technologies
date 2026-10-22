package net.lop223.helpdesksystem.service.async;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class TicketAsyncTasksService
{
    @Async("ticketExecutor")
    public void sendCreatedEmail(Long ticketId, String username) {
        simulateSlowExternalCall();
        log.info("Simulated ticket-created email: ticketId={}, username={}, thread={}",
                ticketId, username, Thread.currentThread().getName());
    }

    @Async("ticketExecutor")
    public CompletableFuture<String> generatePdfReceipt(Long ticketId) {
        simulateSlowExternalCall();
        String result = "Generated PDF receipt for ticketId: " + ticketId;
        log.info("{}; thread={}", result, Thread.currentThread().getName());
        return CompletableFuture.completedFuture(result);
    }

    @Async("ticketExecutor")
    public CompletableFuture<String> notifyAssignedAgent(Long ticketId) {
        simulateSlowExternalCall();
        String result = "Webhook sent for ticket " + ticketId;
        log.info("{}; thread={}", result, Thread.currentThread().getName());
        return CompletableFuture.completedFuture(result);
    }

    private void simulateSlowExternalCall() {
        try {
            Thread.sleep(10000);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Async ticket operation was interrupted", ex);
        }
    }
}
