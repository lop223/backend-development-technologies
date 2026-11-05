package net.lop223.helpdesksystem.scheduler.job;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lop223.helpdesksystem.service.ScheduledTicketTasksService;
import org.quartz.*;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
@DisallowConcurrentExecution
public class UnassignedTicketReminderJob implements Job {

    public static final String MINIMUM_AGE_HOURS_KEY = "minimumAgeHours";
    public static final int DEFAULT_MINIMUM_AGE_HOURS = 24;

    private final ScheduledTicketTasksService scheduledTicketTasksService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            int minimumAgeHours = getMinimumAgeHours(context.getMergedJobDataMap());
            scheduledTicketTasksService.logUnassignedTicketReminder(minimumAgeHours);
        } catch (RuntimeException exception) {
            log.error("Unassigned ticket reminder job failed", exception);
            throw new JobExecutionException(
                    "Unassigned ticket reminder job failed; waiting for the next scheduled run",
                    exception,
                    false
            );
        }
    }

    private int getMinimumAgeHours(JobDataMap jobDataMap) {
        Object value = jobDataMap.get(MINIMUM_AGE_HOURS_KEY);

        if (value == null) {
            return DEFAULT_MINIMUM_AGE_HOURS;
        }

        final int minimumAgeHours;
        try {
            minimumAgeHours = Integer.parseInt(value.toString());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "JobDataMap key '%s' must contain an integer".formatted(MINIMUM_AGE_HOURS_KEY),
                    exception
            );
        }

        if (minimumAgeHours <= 0) {
            throw new IllegalArgumentException(
                    "JobDataMap key '%s' must be greater than zero"
                            .formatted(MINIMUM_AGE_HOURS_KEY)
            );
        }

        return minimumAgeHours;
    }
}