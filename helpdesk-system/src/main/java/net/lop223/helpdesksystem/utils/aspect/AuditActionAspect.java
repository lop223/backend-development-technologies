package net.lop223.helpdesksystem.utils.aspect;

import lombok.RequiredArgsConstructor;
import net.lop223.helpdesksystem.dto.response.AppUserResponse;
import net.lop223.helpdesksystem.service.AuditLogService;
import net.lop223.helpdesksystem.utils.annotation.AuditAction;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class AuditActionAspect {

    private final AuditLogService auditLogService;

    @AfterReturning(pointcut = "@annotation(auditAction)", returning = "result")
    public void auditSuccess(JoinPoint joinPoint, AuditAction auditAction, Object result) {
        Long targetUserId = null;

        if (result instanceof AppUserResponse userResponse) {
            targetUserId = userResponse.getId();
        }
        else if (joinPoint.getArgs() != null) {
            for (Object arg : joinPoint.getArgs()) {
                if (arg instanceof Long id) {
                    targetUserId = id;
                    break;
                }
            }
        }

        String methodName = joinPoint.getSignature().getName();
        auditLogService.logAction(auditAction.action(), targetUserId, methodName);
    }
}
