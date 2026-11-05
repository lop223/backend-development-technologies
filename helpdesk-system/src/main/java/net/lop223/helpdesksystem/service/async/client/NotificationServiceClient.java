package net.lop223.helpdesksystem.service.async.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class NotificationServiceClient {

    private static final String INTERNAL_TOKEN_HEADER = "X-Internal-Token";

    private final RestClient restClient;
    private final String apiToken;

    public NotificationServiceClient(
            RestClient.Builder restClientBuilder,
            @Value("${services.notification.base-url}") String baseUrl,
            @Value("${services.notification.api-token}") String apiToken
    ) {
        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .build();
        this.apiToken = apiToken;
    }

    public void sendTicketCreatedEmail(Long ticketId, String username) {
        post(
                "/internal/notifications/ticket-created",
                new TicketCreatedNotificationRequest(ticketId, username)
        );
    }

    public void generatePdfReceipt(Long ticketId) {
        post(
                "/internal/notifications/ticket-pdf",
                new TicketOperationNotificationRequest(ticketId)
        );
    }

    public void notifyAssignedAgent(Long ticketId) {
        post(
                "/internal/notifications/agent-webhook",
                new TicketOperationNotificationRequest(ticketId)
        );
    }

    private void post(String path, Object request) {
        restClient.post()
                .uri(path)
                .contentType(MediaType.APPLICATION_JSON)
                .header(INTERNAL_TOKEN_HEADER, apiToken)
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }
}