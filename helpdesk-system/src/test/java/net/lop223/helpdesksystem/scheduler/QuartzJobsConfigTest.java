package net.lop223.helpdesksystem.scheduler;

import net.lop223.helpdesksystem.scheduler.job.DailyTicketSummaryJob;
import net.lop223.helpdesksystem.scheduler.job.UnassignedTicketReminderJob;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Trigger;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Тести конфігурації Quartz завдань та тригерів (QuartzJobsConfig)")
class QuartzJobsConfigTest {

    private final QuartzJobsConfig config = new QuartzJobsConfig();

    @Test
    @DisplayName("Завдання та тригери мають явні ключі, правильні зв'язки та класи")
    void jobsAndTriggersHaveExplicitKeysAndCorrectAssociations() {
        JobDetail summaryJob = config.dailyTicketSummaryJobDetail();
        JobDetail reminderJob = config.unassignedTicketReminderJobDetail();

        CronTrigger summaryTrigger = config.dailyTicketSummaryTrigger(summaryJob);
        CronTrigger dailyReminderTrigger =
                config.dailyUnassignedTicketReminderTrigger(reminderJob);
        CronTrigger weeklyReminderTrigger =
                config.weeklyUnassignedTicketReminderTrigger(reminderJob);

        assertEquals(
                new JobKey("daily-ticket-summary", "helpdesk-jobs"),
                summaryJob.getKey()
        );
        assertEquals(
                new JobKey("unassigned-ticket-reminder", "helpdesk-jobs"),
                reminderJob.getKey()
        );

        List<Trigger> triggers = List.of(
                summaryTrigger,
                dailyReminderTrigger,
                weeklyReminderTrigger
        );

        assertEquals(3, triggers.size());
        assertTrue(triggers.stream().allMatch(trigger ->
                trigger.getKey().getName() != null
                        && !trigger.getKey().getName().isBlank()
                        && trigger.getKey().getGroup().equals("helpdesk-triggers")
        ));

        assertEquals(summaryJob.getKey(), summaryTrigger.getJobKey());
        assertEquals(reminderJob.getKey(), dailyReminderTrigger.getJobKey());
        assertEquals(reminderJob.getKey(), weeklyReminderTrigger.getJobKey());

        assertEquals(DailyTicketSummaryJob.class, summaryJob.getJobClass());
        assertEquals(UnassignedTicketReminderJob.class, reminderJob.getJobClass());
        assertNotNull(summaryJob.getDescription());
        assertNotNull(reminderJob.getDescription());
    }

    @Test
    @DisplayName("Кожен Cron-тригер має явно визначену misfire інструкцію та часовий пояс GMT")
    void eachCronTriggerHasExplicitMisfirePolicyAndTimeZone() {
        JobDetail summaryJob = config.dailyTicketSummaryJobDetail();
        JobDetail reminderJob = config.unassignedTicketReminderJobDetail();

        List<CronTrigger> triggers = List.of(
                config.dailyTicketSummaryTrigger(summaryJob),
                config.dailyUnassignedTicketReminderTrigger(reminderJob),
                config.weeklyUnassignedTicketReminderTrigger(reminderJob)
        );

        for (CronTrigger trigger : triggers) {
            assertEquals(
                    CronTrigger.MISFIRE_INSTRUCTION_DO_NOTHING,
                    trigger.getMisfireInstruction()
            );
            assertEquals("GMT", trigger.getTimeZone().getID());
        }
    }

    @Test
    @DisplayName("Невідомий часовий пояс повертає GMT за замовчуванням")
    void unknownTimeZoneFallsBackToGmt() {
        assertEquals(
                "GMT",
                QuartzJobsConfig.cronTimeZone("not-a-valid-time-zone").getID()
        );
    }

    @Test
    @DisplayName("Завдання нагадування має значення параметра за замовчуванням, а тижневий тригер перевизначає його")
    void reminderJobHasDefaultParameterAndWeeklyTriggerOverridesIt() {
        JobDetail reminderJob = config.unassignedTicketReminderJobDetail();
        CronTrigger dailyTrigger =
                config.dailyUnassignedTicketReminderTrigger(reminderJob);
        CronTrigger weeklyTrigger =
                config.weeklyUnassignedTicketReminderTrigger(reminderJob);

        assertEquals(
                24,
                reminderJob.getJobDataMap().getInt(
                        UnassignedTicketReminderJob.MINIMUM_AGE_HOURS_KEY
                )
        );
        assertEquals(
                24,
                dailyTrigger.getJobDataMap().getInt(
                        UnassignedTicketReminderJob.MINIMUM_AGE_HOURS_KEY
                )
        );
        assertEquals(
                72,
                weeklyTrigger.getJobDataMap().getInt(
                        UnassignedTicketReminderJob.MINIMUM_AGE_HOURS_KEY
                )
        );
    }
}