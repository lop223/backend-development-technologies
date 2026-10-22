package net.lop223.helpdesksystem.service.async;

import net.lop223.helpdesksystem.config.AsyncConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringJUnitConfig(classes = {
        AsyncConfig.class,
        TicketAsyncTasksService.class,
        AsyncInfrastructureTest.ProbeConfiguration.class
})
class AsyncInfrastructureTest {

    @Autowired
    private AsyncConfig asyncConfig;

    @Autowired
    private ThreadPoolTaskExecutor ticketExecutor;

    @Autowired
    private TicketAsyncTasksService tasks;

    @Autowired
    private AsyncProbe asyncProbe;

    @Test
    @DisplayName("Налаштовує executor із заданими обмеженнями та префіксом потоків")
    void executor_hasExpectedLimitsAndThreadPrefix() {
        var pool = ticketExecutor.getThreadPoolExecutor();

        assertEquals(3, pool.getCorePoolSize());
        assertEquals(6, pool.getMaximumPoolSize());
        assertEquals(20,
                pool.getQueue().size() + pool.getQueue().remainingCapacity());
        assertTrue(ticketExecutor.getThreadNamePrefix().startsWith("ticket-async-"));
        assertSame(ticketExecutor, asyncConfig.getAsyncExecutor());
    }

    @Test
    @DisplayName("Позначає всі фонові методи анотацією Async із ticketExecutor")
    void asyncTaskMethods_useNamedExecutor() throws ReflectiveOperationException {
        assertAsyncExecutor("sendCreatedEmail", Long.class, String.class);
        assertAsyncExecutor("generatePdfReceipt", Long.class);
        assertAsyncExecutor("notifyAssignedAgent", Long.class);
    }

    @Test
    @DisplayName("Виконує дві незалежні задачі одночасно у налаштованому executor")
    void twoAsyncInvocations_runAtTheSameTimeOnConfiguredExecutor() throws Exception {
        CountDownLatch bothTasksStarted = new CountDownLatch(2);

        CompletableFuture<String> first = asyncProbe.runTogether(bothTasksStarted);
        CompletableFuture<String> second = asyncProbe.runTogether(bothTasksStarted);

        String firstThread = first.get(3, TimeUnit.SECONDS);
        String secondThread = second.get(3, TimeUnit.SECONDS);

        assertTrue(firstThread.startsWith("ticket-async-"), firstThread);
        assertTrue(secondThread.startsWith("ticket-async-"), secondThread);
        assertNotEquals(firstThread, secondThread);
    }

    @Test
    @DisplayName("Повертає результати PDF і webhook асинхронних задач")
    void actualTicketTasks_completeAndReturnResults() throws Exception {
        CompletableFuture<String> combined =
                tasks.generatePdfReceipt(42L)
                        .thenCombine(
                                tasks.notifyAssignedAgent(42L),
                                (pdf, webhook) -> pdf + " | " + webhook
                        );

        String result = combined.get(4, TimeUnit.SECONDS);

        assertTrue(result.contains("Generated PDF receipt for ticketId: 42"));
        assertTrue(result.contains("Webhook sent for ticket 42"));
    }

    @Test
    @DisplayName("Налаштовує обробник винятків для асинхронних void-методів")
    void asyncUncaughtExceptionHandler_isConfiguredAndCallable()
            throws NoSuchMethodException {
        Method method = AsyncProbe.class.getMethod("asyncVoidTarget");

        assertNotNull(asyncConfig.getAsyncUncaughtExceptionHandler());
        assertDoesNotThrow(() ->
                asyncConfig.getAsyncUncaughtExceptionHandler()
                        .handleUncaughtException(
                                new IllegalStateException("тестовий виняток"),
                                method,
                                new Object[0]
                        )
        );
    }

    private void assertAsyncExecutor(String methodName, Class<?>... parameterTypes)
            throws ReflectiveOperationException {
        Method method = TicketAsyncTasksService.class
                .getMethod(methodName, parameterTypes);
        Async async = method.getAnnotation(Async.class);

        assertNotNull(async, methodName + " має бути позначений @Async");
        assertEquals("ticketExecutor", async.value());
    }

    @TestConfiguration
    static class ProbeConfiguration {
        @Bean
        AsyncProbe asyncProbe() {
            return new AsyncProbe();
        }
    }

    static class AsyncProbe {
        @Async("ticketExecutor")
        public CompletableFuture<String> runTogether(CountDownLatch bothTasksStarted) {
            bothTasksStarted.countDown();

            try {
                if (!bothTasksStarted.await(2, TimeUnit.SECONDS)) {
                    throw new IllegalStateException(
                            "Задачі не виконувалися одночасно");
                }
                return CompletableFuture.completedFuture(
                        Thread.currentThread().getName());
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(exception);
            }
        }

        public void asyncVoidTarget() {
        }
    }
}