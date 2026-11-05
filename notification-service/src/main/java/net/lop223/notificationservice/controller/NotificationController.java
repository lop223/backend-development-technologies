package net.lop223.notificationservice.controller;

import jakarta.validation.Valid;
import net.lop223.notificationservice.api.TicketCreatedRequest;
import net.lop223.notificationservice.api.TicketOperationRequest;
import net.lop223.notificationservice.security.InternalApiTokenValidator;
import net.lop223.notificationservice.service.NotificationOperationsService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/notifications")
public class NotificationController {

    private static final String INTERNAL_TOKEN_HEADER = "X-Internal-Token";

    private final NotificationOperationsService notificationService;
    private final InternalApiTokenValidator tokenValidator;

    public NotificationController(
            NotificationOperationsService notificationService,
            InternalApiTokenValidator tokenValidator
    ) {
        this.notificationService = notificationService;
        this.tokenValidator = tokenValidator;
    }

    @PostMapping("/ticket-created")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void sendTicketCreatedEmail(
            @RequestHeader(value = INTERNAL_TOKEN_HEADER, required = false) String token,
            @Valid @RequestBody TicketCreatedRequest request
    ) {
        tokenValidator.validate(token);
        notificationService.sendTicketCreatedEmail(request);
    }

    @PostMapping("/ticket-pdf")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void generatePdfReceipt(
            @RequestHeader(value = INTERNAL_TOKEN_HEADER, required = false) String token,
            @Valid @RequestBody TicketOperationRequest request
    ) {
        tokenValidator.validate(token);
        notificationService.generatePdfReceipt(request);
    }

    @PostMapping("/agent-webhook")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void notifyAssignedAgent(
            @RequestHeader(value = INTERNAL_TOKEN_HEADER, required = false) String token,
            @Valid @RequestBody TicketOperationRequest request
    ) {
        tokenValidator.validate(token);
        notificationService.notifyAssignedAgent(request);
    }
}