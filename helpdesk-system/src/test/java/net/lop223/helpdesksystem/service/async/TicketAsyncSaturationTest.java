package net.lop223.helpdesksystem.service.async;

import net.lop223.helpdesksystem.dto.response.TicketResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringJUnitConfig(classes = {
        TicketAsyncSaturationTest.SmallExecutorConfiguration.class,
        TicketAsyncTasksService.class,
        TicketAsyncCoordinatorService.class
})
class TicketAsyncSaturationTest {

    @org.springframework.beans.factory.annotation.Autowired
    private TicketAsyncCoordinatorService coordinator;

    @Test
    @DisplayName("Повертає контрольований результат, коли черга executor переповнена")
    void dispatchAfterCreation_reportsRejectionWhenExecutorIsFull() throws Exception {
        TicketResponse ticket = new TicketResponse();
        ticket.setId(99L);
        ticket.setCreatedByUsername("user1");

        var summary = coordinator.dispatchAfterCreation(ticket)
                .get(8, TimeUnit.SECONDS);

        assertEquals(99L, summary.ticketId());
        assertTrue(summary.pdf().successful());
        assertFalse(summary.webhook().successful());
        assertEquals("Executor is busy; retry later", summary.webhook().details());
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
    }
}