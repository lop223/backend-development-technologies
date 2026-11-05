package net.lop223.helpdesksystem.service.impl;

import net.lop223.helpdesksystem.repository.TicketRepository;
import net.lop223.helpdesksystem.utils.enums.TicketStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тести сервісу планових завдань тикетів (ScheduledTicketTasksServiceImpl)")
class ScheduledTicketTasksServiceImplTest {

    @Mock
    private TicketRepository ticketRepository;

    @Test
    @DisplayName("Повторний запуск денного звіту є ідемпотентним та не змінює дані тикетів")
    void runningDailySummaryTwiceDoesNotChangeTicketData() {
        when(ticketRepository.countByStatus(TicketStatus.NEW)).thenReturn(3L);
        when(ticketRepository.countByStatus(TicketStatus.IN_PROGRESS)).thenReturn(2L);
        when(ticketRepository.countByStatus(TicketStatus.RESOLVED)).thenReturn(4L);
        when(ticketRepository.countByStatus(TicketStatus.CLOSED)).thenReturn(5L);

        ScheduledTicketTasksServiceImpl service =
                new ScheduledTicketTasksServiceImpl(ticketRepository);

        service.logDailyTicketSummary();
        service.logDailyTicketSummary();

        verify(ticketRepository, times(2)).countByStatus(TicketStatus.NEW);
        verify(ticketRepository, times(2)).countByStatus(TicketStatus.IN_PROGRESS);
        verify(ticketRepository, times(2)).countByStatus(TicketStatus.RESOLVED);
        verify(ticketRepository, times(2)).countByStatus(TicketStatus.CLOSED);
    }

    @Test
    @DisplayName("Нагадування про непризначені тикети використовує лише зчитування та може безпечно повторюватися")
    void reminderUsesReadOnlyCountAndCanBeRepeatedForSameAge() {
        when(ticketRepository.countByAssignedToIsNullAndStatusAndCreatedAtBefore(
                eq(TicketStatus.NEW),
                any(LocalDateTime.class)
        )).thenReturn(2L);

        ScheduledTicketTasksServiceImpl service =
                new ScheduledTicketTasksServiceImpl(ticketRepository);

        service.logUnassignedTicketReminder(24);
        service.logUnassignedTicketReminder(24);

        verify(ticketRepository, times(2))
                .countByAssignedToIsNullAndStatusAndCreatedAtBefore(
                        eq(TicketStatus.NEW),
                        any(LocalDateTime.class)
                );
    }

    @Test
    @DisplayName("Нагадування викидає IllegalArgumentException, якщо передано непозитивне значення віку тикета")
    void reminderRejectsNonPositiveAge() {
        ScheduledTicketTasksServiceImpl service =
                new ScheduledTicketTasksServiceImpl(ticketRepository);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.logUnassignedTicketReminder(0)
        );
    }
}