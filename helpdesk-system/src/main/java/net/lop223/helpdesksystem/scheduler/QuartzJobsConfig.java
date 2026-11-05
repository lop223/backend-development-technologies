package net.lop223.helpdesksystem.scheduler;

import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.TimeZone;

@Configuration
public class QuartzJobsConfig {
    static final String JOB_GROUP = "helpdesk-jobs";
    static final String TRIGGER_GROUP = "helpdesk-triggers";
    static final String CRON_TIME_ZONE_ID = "GIM";

    @Bean
    public JobDetail dailyTicketSummaryJobDetail() {
        return JobBuilder.newJob(DailyTicketSummaryJob.class)
                .withIdentity("daily-ticket-summary", JOB_GROUP)
                .withDescription("Logs a summary of tickets by status")
                .storeDurably()
                .build();
    }

    @Bean
    public CronTrigger dailyTicketSummaryTrigger(JobDetail dailyTicketSummaryJobDetail) {
        return TriggerBuilder.newTrigger()
                .withIdentity("daily-ticket-summary-at-0200", TRIGGER_GROUP)
                .forJob(dailyTicketSummaryJobDetail)
                .withSchedule(CronScheduleBuilder
                        .cronSchedule("0 0 2 * * ?")
                        .inTimeZone(cronTimeZone(CRON_TIME_ZONE_ID))
                        .withMisfireHandlingInstructionDoNothing())
                .build();
    }

    @Bean
    public JobDetail unassignedTicketReminderJobDetail() {
        return JobBuilder.newJob(UnassignedTicketReminderJob.class)
                .withIdentity("unassigned-ticket-reminder", JOB_GROUP)
                .withDescription("Logs reminders about old unassigned tickets")
                .usingJobData(
                        UnassignedTicketReminderJob.MINIMUM_AGE_HOURS_KEY,
                        UnassignedTicketReminderJob.DEFAULT_MINIMUM_AGE_HOURS
                )
                .storeDurably()
                .build();
    }

    @Bean
    public CronTrigger dailyUnassignedTicketReminderTrigger(
            JobDetail unassignedTicketReminderJobDetail
    ) {
        return TriggerBuilder.newTrigger()
                .withIdentity("unassigned-ticket-reminder-daily", TRIGGER_GROUP)
                .forJob(unassignedTicketReminderJobDetail)
                .usingJobData(
                        UnassignedTicketReminderJob.MINIMUM_AGE_HOURS_KEY,
                        UnassignedTicketReminderJob.DEFAULT_MINIMUM_AGE_HOURS
                )
                .withSchedule(CronScheduleBuilder
                        .cronSchedule("0 0 9 * * ?")
                        .inTimeZone(cronTimeZone(CRON_TIME_ZONE_ID))
                        .withMisfireHandlingInstructionDoNothing())
                .build();
    }

    @Bean
    public CronTrigger weeklyUnassignedTicketReminderTrigger(
            JobDetail unassignedTicketReminderJobDetail
    ) {
        return TriggerBuilder.newTrigger()
                .withIdentity("unassigned-ticket-reminder-weekly", TRIGGER_GROUP)
                .forJob(unassignedTicketReminderJobDetail)
                .usingJobData(
                        UnassignedTicketReminderJob.MINIMUM_AGE_HOURS_KEY,
                        72
                )
                .withSchedule(CronScheduleBuilder
                        .cronSchedule("0 15 9 ? * MON")
                        .inTimeZone(cronTimeZone(CRON_TIME_ZONE_ID))
                        .withMisfireHandlingInstructionDoNothing())
                .build();
    }

    static TimeZone cronTimeZone(String zoneId) {
        return TimeZone.getTimeZone(zoneId);
    }
}
