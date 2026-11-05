package net.lop223.helpdesksystem.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lop223.helpdesksystem.repository.TicketRepository;
import net.lop223.helpdesksystem.service.ScheduledTicketTasksService;
import net.lop223.helpdesksystem.utils.enums.TicketStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ScheduledTicketTasksServiceImpl implements ScheduledTicketTasksService {

    private final TicketRepository ticketRepository;

    @Override
    public void logDailyTicketSummary() {
        long newTickets = ticketRepository.countByStatus(TicketStatus.NEW);
        long inProgressTickets = ticketRepository.countByStatus(TicketStatus.IN_PROGRESS);
        long resolvedTickets = ticketRepository.countByStatus(TicketStatus.RESOLVED);
        long closedTickets = ticketRepository.countByStatus(TicketStatus.CLOSED);

        log.info(
                "Daily ticket summary: NEW={}, IN_PROGRESS={}, RESOLVED={}, CLOSED={}",
                newTickets,
                inProgressTickets,
                resolvedTickets,
                closedTickets
        );
    }

    @Override
    public void logUnassignedTicketReminder(int minimumAgeHours) {
        if (minimumAgeHours <= 0) {
            throw new IllegalArgumentException("minimumAgeHours must be greater than zero");
        }

        LocalDateTime cutoff = LocalDateTime.now().minusHours(minimumAgeHours);
        long count = ticketRepository.countByAssignedToIsNullAndStatusAndCreatedAtBefore(
                TicketStatus.NEW,
                cutoff
        );

        if (count > 0) {
            log.warn(
                    "There are {} unassigned NEW ticket(s) older than {} hour(s)",
                    count,
                    minimumAgeHours
            );
        } else {
            log.info(
                    "No unassigned NEW tickets older than {} hour(s)",
                    minimumAgeHours
            );
        }
    }
}
