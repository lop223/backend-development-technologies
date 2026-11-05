package net.lop223.notificationservice.api;

import net.lop223.notificationservice.controller.NotificationController;
import net.lop223.notificationservice.service.NotificationOperationsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
@DisplayName("Тести REST-контролера сповіщень (NotificationController)")
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationOperationsService notificationService;

    @Test
    @DisplayName("Приймає коректний запит на створення тикета та повертає статус 202 Accepted")
    void acceptsValidTicketCreatedRequest() throws Exception {
        mockMvc.perform(post("/internal/notifications/ticket-created")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ticketId": 15,
                                  "username": "user1"
                                }
                                """))
                .andExpect(status().isAccepted());

        verify(notificationService).sendTicketCreatedEmail(
                new TicketCreatedRequest(15L, "user1")
        );
    }

    @Test
    @DisplayName("Відхиляє запит на генерацію PDF з некоректним (непозитивним) ID тикета та повертає статус 400 Bad Request")
    void rejectsTicketRequestWithNonPositiveId() throws Exception {
        mockMvc.perform(post("/internal/notifications/ticket-pdf")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ticketId": 0
                                }
                                """))
                .andExpect(status().isBadRequest());
    }
}