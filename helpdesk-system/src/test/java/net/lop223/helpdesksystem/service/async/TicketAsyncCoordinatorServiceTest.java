package net.lop223.helpdesksystem.service.async;

import net.lop223.helpdesksystem.dto.response.TicketResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.task.TaskRejectedException;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketAsyncCoordinatorServiceTest {

    @Mock
    private TicketAsyncTasksService ticketAsyncTasksService;

    @InjectMocks
    private TicketAsyncCoordinatorService coordinator;

    private static TicketResponse ticket() {
        TicketResponse response = new TicketResponse();
        response.setId(42L);
        response.setCreatedByUsername("user1");
        return response;
    }

    @Test
    @DisplayName("Об'єднує результати двох асинхронних задач після завершення обох")
    void dispatchAfterCreation_startsBothTasksAndCombinesTheirResults() throws Exception {
        CompletableFuture<String> pdf = new CompletableFuture<>();
        CompletableFuture<String> webhook = new CompletableFuture<>();

        when(ticketAsyncTasksService.generatePdfReceipt(42L)).thenReturn(pdf);
        when(ticketAsyncTasksService.notifyAssignedAgent(42L)).thenReturn(webhook);

        CompletableFuture<TicketAsyncCoordinatorService.TicketAsyncSummary> result =
                coordinator.dispatchAfterCreation(ticket());

        verify(ticketAsyncTasksService).sendCreatedEmail(42L, "user1");
        verify(ticketAsyncTasksService).generatePdfReceipt(42L);
        verify(ticketAsyncTasksService).notifyAssignedAgent(42L);
        assertFalse(result.isDone());

        pdf.complete("PDF готовий");
        assertFalse(result.isDone());

        webhook.complete("Webhook надіслано");
        var summary = result.get(1, TimeUnit.SECONDS);

        assertEquals(42L, summary.ticketId());
        assertTrue(summary.pdf().successful());
        assertEquals("PDF готовий", summary.pdf().details());
        assertTrue(summary.webhook().successful());
        assertEquals("Webhook надіслано", summary.webhook().details());
    }

    @Test
    @DisplayName("Перетворює виняток PDF-задачі на невдалий результат")
    void dispatchAfterCreation_convertsFailedFutureToFailureResult() throws Exception {
        when(ticketAsyncTasksService.generatePdfReceipt(42L))
                .thenReturn(CompletableFuture.failedFuture(
                        new IllegalStateException("PDF service failed")));
        when(ticketAsyncTasksService.notifyAssignedAgent(42L))
                .thenReturn(CompletableFuture.completedFuture("Webhook надіслано"));

        var summary = coordinator.dispatchAfterCreation(ticket())
                .get(1, TimeUnit.SECONDS);

        assertFalse(summary.pdf().successful());
        assertEquals("Failed or timed out", summary.pdf().details());
        assertTrue(summary.webhook().successful());
    }

    @Test
    @DisplayName("Повертає зрозумілий результат, якщо executor відхилив PDF-задачу")
    void dispatchAfterCreation_handlesPdfTaskRejection() throws Exception {
        when(ticketAsyncTasksService.generatePdfReceipt(42L))
                .thenThrow(new TaskRejectedException("executor is full"));
        when(ticketAsyncTasksService.notifyAssignedAgent(42L))
                .thenReturn(CompletableFuture.completedFuture("Webhook надіслано"));

        var summary = coordinator.dispatchAfterCreation(ticket())
                .get(1, TimeUnit.SECONDS);

        assertFalse(summary.pdf().successful());
        assertEquals("Executor is busy; retry later", summary.pdf().details());
        assertTrue(summary.webhook().successful());
    }

    @Test
    @DisplayName("Повертає зрозумілий результат, якщо executor відхилив webhook-задачу")
    void dispatchAfterCreation_handlesWebhookTaskRejection() throws Exception {
        when(ticketAsyncTasksService.generatePdfReceipt(42L))
                .thenReturn(CompletableFuture.completedFuture("PDF готовий"));
        when(ticketAsyncTasksService.notifyAssignedAgent(42L))
                .thenThrow(new TaskRejectedException("executor is full"));

        var summary = coordinator.dispatchAfterCreation(ticket())
                .get(1, TimeUnit.SECONDS);

        assertTrue(summary.pdf().successful());
        assertFalse(summary.webhook().successful());
        assertEquals("Executor is busy; retry later", summary.webhook().details());
    }

    @Test
    @DisplayName("Продовжує обробку тікета, якщо executor відхилив email-задачу")
    void dispatchAfterCreation_continuesIfEmailTaskIsRejected() throws Exception {
        doThrow(new TaskRejectedException("executor is full"))
                .when(ticketAsyncTasksService).sendCreatedEmail(42L, "user1");
        when(ticketAsyncTasksService.generatePdfReceipt(42L))
                .thenReturn(CompletableFuture.completedFuture("PDF готовий"));
        when(ticketAsyncTasksService.notifyAssignedAgent(42L))
                .thenReturn(CompletableFuture.completedFuture("Webhook надіслано"));

        var summary = coordinator.dispatchAfterCreation(ticket())
                .get(1, TimeUnit.SECONDS);

        assertTrue(summary.pdf().successful());
        assertTrue(summary.webhook().successful());
    }

    @Test
    @DisplayName("Перетворює перевищення часу очікування на невдалий результат")
    void dispatchAfterCreation_convertsTimeoutToFailureResult() throws Exception {
        CompletableFuture<String> neverCompletes = new CompletableFuture<>();
        when(ticketAsyncTasksService.generatePdfReceipt(42L)).thenReturn(neverCompletes);
        when(ticketAsyncTasksService.notifyAssignedAgent(42L))
                .thenReturn(CompletableFuture.completedFuture("Webhook надіслано"));

        var summary = coordinator.dispatchAfterCreation(ticket())
                .get(7, TimeUnit.SECONDS);

        assertFalse(summary.pdf().successful());
        assertEquals("Failed or timed out", summary.pdf().details());
        assertTrue(summary.webhook().successful());
    }
}