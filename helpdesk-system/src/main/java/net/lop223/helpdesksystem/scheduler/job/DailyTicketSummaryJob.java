package net.lop223.helpdesksystem.scheduler.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DailyTicketSummaryJob implements Job {

    private final net.lop223.helpdesksystem.service.ScheduledTicketTasksService scheduledTicketTasksService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            scheduledTicketTasksService.logDailyTicketSummary();
        } catch (RuntimeException exception) {
            log.error("Daily ticket summary job failed", exception);
            throw new JobExecutionException(
                    "Daily ticket summary job failed; waiting for the next scheduled run",
                    exception,
                    false
            );
        }
    }

}
