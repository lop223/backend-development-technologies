package net.lop223.helpdesksystem.service.async;

import net.lop223.helpdesksystem.dto.response.TicketResponse;
import net.lop223.helpdesksystem.service.async.client.NotificationServiceClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

@SpringJUnitConfig(classes = {
        TicketAsyncSaturationTest.SmallExecutorConfiguration.class,
        TicketAsyncTasksService.class,
        TicketAsyncCoordinatorService.class
})
class TicketAsyncSaturationTest {

    @Autowired
    private TicketAsyncCoordinatorService coordinator;

    @Autowired
    private NotificationServiceClient notificationServiceClient;

    @Test
    @DisplayName("Повертає контрольований результат, коли черга executor переповнена")
    void dispatchAfterCreation_reportsRejectionWhenExecutorIsFull() throws Exception {
        CountDownLatch emailStarted = new CountDownLatch(1);
        CountDownLatch releaseEmail = new CountDownLatch(1);
        doAnswer(invocation -> {
            emailStarted.countDown();
            assertTrue(releaseEmail.await(3, TimeUnit.SECONDS));
            return null;
        }).when(notificationServiceClient).sendTicketCreatedEmail(anyLong(), anyString());

        TicketResponse ticket = new TicketResponse();
        ticket.setId(99L);
        ticket.setCreatedByUsername("user1");

        try {
            var result = coordinator.dispatchAfterCreation(ticket);
            assertTrue(emailStarted.await(1, TimeUnit.SECONDS));
            releaseEmail.countDown();
            var summary = result.get(3, TimeUnit.SECONDS);

            assertEquals(99L, summary.ticketId());
            assertTrue(summary.email().successful());
            assertTrue(summary.pdf().successful());
            assertFalse(summary.webhook().successful());
            assertEquals("Executor is busy; retry later", summary.webhook().details());
        } finally {
            releaseEmail.countDown();
        }
    }

    @Configuration
    @EnableAsync
    static class SmallExecutorConfiguration {
        @Bean(name = "ticketExecutor")
        ThreadPoolTaskExecutor ticketExecutor() {
            ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
            executor.setCorePoolSize(1);
            executor.setMaxPoolSize(1);
            executor.setQueueCapacity(1);
            executor.setThreadNamePrefix("ticket-test-");
            return executor;
        }

        @Bean
        NotificationServiceClient notificationServiceClient() {
            return mock(NotificationServiceClient.class);
        }
    }
}