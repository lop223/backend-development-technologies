package net.lop223.notificationservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.lop223.notificationservice.api.TicketCreatedRequest;
import net.lop223.notificationservice.api.TicketOperationRequest;
import net.lop223.notificationservice.service.NotificationOperationsService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/notifications")
public class NotificationController
{
    private final NotificationOperationsService notificationService;

    @PostMapping("/ticket-created")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void sendTicketCreatedEmail(
            @Valid @RequestBody TicketCreatedRequest request
    ) {
        notificationService.sendTicketCreatedEmail(request);
    }

    @PostMapping("/ticket-pdf")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void generatePdfReceipt(
            @Valid @RequestBody TicketOperationRequest request
    ) {
        notificationService.generatePdfReceipt(request);
    }

    @PostMapping("/agent-webhook")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void notifyAssignedAgent(
            @Valid @RequestBody TicketOperationRequest request
    ) {
        notificationService.notifyAssignedAgent(request);
    }
}
