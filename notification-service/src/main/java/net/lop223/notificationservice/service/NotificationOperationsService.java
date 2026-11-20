package net.lop223.notificationservice.service;

import net.lop223.notificationservice.api.TicketCreatedRequest;
import net.lop223.notificationservice.api.TicketOperationRequest;

public interface NotificationOperationsService {
    void sendTicketCreatedEmail(TicketCreatedRequest request);
    void generatePdfReceipt(TicketOperationRequest request);
    void notifyAssignedAgent(TicketOperationRequest request);
}
