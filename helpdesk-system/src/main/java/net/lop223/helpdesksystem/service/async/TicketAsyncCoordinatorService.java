package net.lop223.helpdesksystem.service.async;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lop223.helpdesksystem.dto.response.TicketResponse;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketAsyncCoordinatorService
{
    private static final long TIMEOUT_SECONDS = 3;

    private final TicketAsyncTasksService ticketAsyncTasksService;

    public CompletableFuture<TicketAsyncSummary> dispatchAfterCreation(TicketResponse ticketResponse) {
        try {
            ticketAsyncTasksService.sendCreatedEmail(ticketResponse.getId(),
                    ticketResponse.getCreatedByUsername());
        } catch (Exception ex) {
            log.warn("Email task rejected for ticket {}", ticketResponse.getId(), ex);
        }

        CompletableFuture<OperationResult> pdf =
                capture("PDF", () -> ticketAsyncTasksService.generatePdfReceipt(ticketResponse.getId()));

        CompletableFuture<OperationResult> webhook =
                capture("Webhook", () -> ticketAsyncTasksService.notifyAssignedAgent(ticketResponse.getId()));

        return pdf.thenCombine(webhook,
                (pdfResult, webhookResult) ->
                        new TicketAsyncSummary(ticketResponse.getId(), pdfResult, webhookResult));
    }

    private CompletableFuture<OperationResult> capture(
            String operation,
            Supplier<CompletableFuture<String>> submission) {
        try {
            return submission.get()
                    .orTimeout(5, TimeUnit.SECONDS)
                    .handle((value, exception) -> {
                        if (exception != null) {
                            log.warn("{} failed or timed out", operation, exception);
                            return new OperationResult(
                                    operation, false, "Failed or timed out");
                        }
                        return new OperationResult(operation, true, value);
                    });
        } catch (Exception ex) {
            log.warn("{} task rejected: executor is saturated", operation, ex);
            return CompletableFuture.completedFuture(
                    new OperationResult(operation, false, "Executor is busy; retry later"));
        }
    }

    public record OperationResult(
            String operation,
            boolean successful,
            String details) {
    }

    public record TicketAsyncSummary(
            Long ticketId,
            OperationResult pdf,
            OperationResult webhook) {
    }
}
