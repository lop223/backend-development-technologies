package net.lop223.helpdesksystem.scheduler.job;

import net.lop223.helpdesksystem.service.ScheduledTicketTasksService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.OngoingStubbing;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionException;
import org.quartz.JobBuilder;
import org.quartz.TriggerBuilder;
import org.quartz.impl.JobExecutionContextImpl;
import org.quartz.spi.OperableTrigger;
import org.quartz.spi.TriggerFiredBundle;

import java.util.Date;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тести завдання Quartz для нагадування про непризначені тикети (UnassignedTicketReminderJob)")
class UnassignedTicketReminderJobTest {

    @Mock
    private ScheduledTicketTasksService scheduledTicketTasksService;

    @Test
    @DisplayName("Значення з JobDataMap тригера перевизначає дефолтні значення Job, зберігаючи інші ключі")
    void triggerValueOverridesJobDefaultWhenQuartzMergesJobDataMaps() throws Exception {
        JobDetail jobDetail = JobBuilder.newJob(UnassignedTicketReminderJob.class)
                .withIdentity("reminder", "test-jobs")
                .usingJobData(
                        UnassignedTicketReminderJob.MINIMUM_AGE_HOURS_KEY,
                        24
                )
                .usingJobData("jobOnlyKey", "preserved")
                .build();

        CronTrigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("reminder-trigger", "test-triggers")
                .forJob(jobDetail)
                .usingJobData(
                        UnassignedTicketReminderJob.MINIMUM_AGE_HOURS_KEY,
                        72
                )
                .withSchedule(CronScheduleBuilder
                        .cronSchedule("0 0 9 * * ?")
                        .inTimeZone(TimeZone.getTimeZone("GMT")))
                .build();

        JobExecutionContextImpl context = executionContext(jobDetail, trigger);
        JobDataMap mergedData = context.getMergedJobDataMap();

        assertEquals(
                72,
                mergedData.getInt(UnassignedTicketReminderJob.MINIMUM_AGE_HOURS_KEY)
        );
        assertEquals("preserved", mergedData.getString("jobOnlyKey"));

        new UnassignedTicketReminderJob(scheduledTicketTasksService).execute(context);

        verify(scheduledTicketTasksService).logUnassignedTicketReminder(72);
    }

    @Test
    @DisplayName("Відсутність параметра використовує задокументоване значення за замовчуванням (24)")
    void missingParameterUsesDocumentedDefault() throws Exception {
        JobDetail jobDetail = JobBuilder.newJob(UnassignedTicketReminderJob.class)
                .withIdentity("reminder", "test-jobs")
                .build();

        CronTrigger trigger = triggerFor(jobDetail, new JobDataMap());
        JobExecutionContextImpl context = executionContext(jobDetail, trigger);

        new UnassignedTicketReminderJob(scheduledTicketTasksService).execute(context);

        verify(scheduledTicketTasksService).logUnassignedTicketReminder(24);
    }

    @Test
    @DisplayName("Некоректний тип параметра завершується помилкою JobExecutionException без негайного повторного запуску")
    void invalidParameterFailsClearlyAndDoesNotRefireImmediately() throws Exception {
        JobDetail jobDetail = JobBuilder.newJob(UnassignedTicketReminderJob.class)
                .withIdentity("reminder", "test-jobs")
                .build();

        JobDataMap triggerData = new JobDataMap();
        triggerData.put(
                UnassignedTicketReminderJob.MINIMUM_AGE_HOURS_KEY,
                "not-an-integer"
        );

        CronTrigger trigger = triggerFor(jobDetail, triggerData);
        JobExecutionContextImpl context = executionContext(jobDetail, trigger);

        JobExecutionException exception = assertThrows(
                JobExecutionException.class,
                () -> new UnassignedTicketReminderJob(scheduledTicketTasksService)
                        .execute(context)
        );

        assertFalse(exception.refireImmediately());
    }

    @Test
    @DisplayName("Збій у сервісі обробляється Job ExecutionException та не викликає негайного повторного запуску")
    void serviceFailureIsLoggedByJobAndDoesNotRefireImmediately() throws Exception {
        JobDetail jobDetail = JobBuilder.newJob(UnassignedTicketReminderJob.class)
                .withIdentity("reminder", "test-jobs")
                .build();

        CronTrigger trigger = triggerFor(jobDetail, new JobDataMap());
        JobExecutionContextImpl context = executionContext(jobDetail, trigger);

        org.mockito.Mockito.doThrow(new IllegalStateException("database unavailable"))
                .when(scheduledTicketTasksService)
                .logUnassignedTicketReminder(24);

        JobExecutionException exception = assertThrows(
                JobExecutionException.class,
                () -> new UnassignedTicketReminderJob(scheduledTicketTasksService)
                        .execute(context)
        );

        assertFalse(exception.refireImmediately());
    }

    @Test
    @DisplayName("Перевірка наявності анотації @DisallowConcurrentExecution для заборони паралельного виконання")
    void reminderJobDisallowsConcurrentExecution() {
        assertEquals(
                true,
                UnassignedTicketReminderJob.class.isAnnotationPresent(
                        org.quartz.DisallowConcurrentExecution.class
                )
        );
    }

    private CronTrigger triggerFor(JobDetail jobDetail, JobDataMap triggerData) {
        return TriggerBuilder.newTrigger()
                .withIdentity("reminder-trigger", "test-triggers")
                .forJob(jobDetail)
                .usingJobData(triggerData)
                .withSchedule(CronScheduleBuilder
                        .cronSchedule("0 0 9 * * ?")
                        .inTimeZone(TimeZone.getTimeZone("GMT")))
                .build();
    }

    private JobExecutionContextImpl executionContext(
            JobDetail jobDetail,
            CronTrigger trigger
    ) {
        TriggerFiredBundle bundle = new TriggerFiredBundle(
                jobDetail,
                (OperableTrigger) trigger,
                null,
                false,
                new Date(),
                new Date(),
                null,
                null
        );

        return new JobExecutionContextImpl(null, bundle, null);
    }
}