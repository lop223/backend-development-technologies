package net.lop223.helpdesksystem.service;

public interface ScheduledTicketTasksService {
    void logDailyTicketSummary();
    void logUnassignedTicketReminder(int minimumAgeHours);
}
