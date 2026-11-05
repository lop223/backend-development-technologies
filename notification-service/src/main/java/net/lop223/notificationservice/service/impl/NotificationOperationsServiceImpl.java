package net.lop223.notificationservice.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.lop223.notificationservice.api.TicketCreatedRequest;
import net.lop223.notificationservice.api.TicketOperationRequest;
import net.lop223.notificationservice.service.NotificationOperationsService;
import org.springframework.stereotype.Service;

import java.beans.Transient;

@Slf4j
@Service
public class NotificationOperationsServiceImpl implements NotificationOperationsService {

    public void sendTicketCreatedEmail(TicketCreatedRequest request) {
        log.info(
                "Simulated ticket-created email: ticketId={}, username={}",
                request.ticketId(),
                request.username()
        );
    }

    public void generatePdfReceipt(TicketOperationRequest request) {
        log.info(
                "Simulated PDF receipt generation: ticketId={}",
                request.ticketId()
        );
    }

    public void notifyAssignedAgent(TicketOperationRequest request) {
        log.info(
                "Simulated agent webhook: ticketId={}",
                request.ticketId()
        );
    }
}
