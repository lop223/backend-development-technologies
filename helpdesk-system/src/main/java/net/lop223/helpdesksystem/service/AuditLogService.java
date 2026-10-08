package net.lop223.helpdesksystem.service;

public interface AuditLogService {
    void logAction(String action, Long entityId, String details);
}
