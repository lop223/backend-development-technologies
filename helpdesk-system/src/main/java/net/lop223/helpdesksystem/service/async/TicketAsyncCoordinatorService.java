package net.lop223.helpdesksystem.service.async;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lop223.helpdesksystem.dto.response.TicketResponse;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketAsyncCoordinatorService {

    private static final long TIMEOUT_SECONDS = 5;

    private final TicketAsyncTasksService ticketAsyncTasksService;

    public CompletableFuture<TicketAsyncSummary> dispatchAfterCreation(
            TicketResponse ticketResponse
    ) {
        Long ticketId = ticketResponse.getId();

        CompletableFuture<OperationResult> email = capture(
                "Email",
                () -> ticketAsyncTasksService.sendCreatedEmail(
                        ticketId,
                        ticketResponse.getCreatedByUsername()
                )
        );

        CompletableFuture<OperationResult> pdf = capture(
                "PDF",
                () -> ticketAsyncTasksService.generatePdfReceipt(ticketId)
        );

        CompletableFuture<OperationResult> webhook = capture(
                "Webhook",
                () -> ticketAsyncTasksService.notifyAssignedAgent(ticketId)
        );

        return CompletableFuture.allOf(email, pdf, webhook)
                .thenApply(ignored -> new TicketAsyncSummary(
                        ticketId,
                        email.join(),
                        pdf.join(),
                        webhook.join()
                ));
    }

    private CompletableFuture<OperationResult> capture(
            String operation,
            Supplier<CompletableFuture<String>> submission
    ) {
        try {
            return submission.get()
                    .orTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .handle((value, exception) -> {
                        if (exception != null) {
                            log.warn("{} notification failed or timed out", operation, exception);
                            return new OperationResult(
                                    operation,
                                    false,
                                    "Failed or timed out"
                            );
                        }
                        return new OperationResult(operation, true, value);
                    });
        } catch (Exception exception) {
            log.warn("{} notification task could not be submitted", operation, exception);
            String details = exception instanceof TaskRejectedException
                    ? "Executor is busy; retry later"
                    : "Task could not be submitted";
            return CompletableFuture.completedFuture(
                    new OperationResult(operation, false, details)
            );
        }
    }

    public record OperationResult(
            String operation,
            boolean successful,
            String details
    ) {
    }

    public record TicketAsyncSummary(
            Long ticketId,
            OperationResult email,
            OperationResult pdf,
            OperationResult webhook
    ) {
    }
}