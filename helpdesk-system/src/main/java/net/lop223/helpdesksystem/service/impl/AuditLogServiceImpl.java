package net.lop223.helpdesksystem.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.lop223.helpdesksystem.service.AuditLogService;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuditLogServiceImpl implements AuditLogService {

    @Override
    public void logAction(String action, Long entityId, String details) {
        log.info("[AUDIT TRAIL] Action: '{}', Target User ID: {}, Method: '{}'", action, entityId, details);
    }
}
