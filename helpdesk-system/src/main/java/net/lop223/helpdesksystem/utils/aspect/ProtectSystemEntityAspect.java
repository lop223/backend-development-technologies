package net.lop223.helpdesksystem.utils.aspect;

import lombok.extern.slf4j.Slf4j;
import net.lop223.helpdesksystem.utils.annotation.ProtectSystemEntity;
import net.lop223.helpdesksystem.utils.exception.OperationNotAllowedException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Slf4j
@Aspect
@Component
public class ProtectSystemEntityAspect {

    @Before("@annotation(protectedConfig)")
    public void checkProtectedEntity(JoinPoint joinPoint, ProtectSystemEntity protectedConfig) {
        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            if (arg instanceof Long id) {
                boolean isReserved = Arrays.stream(protectedConfig.reservedIds()).anyMatch(id::equals);
                if (isReserved) {
                    log.warn("[AOP Security Guard] Blocked attempt to modify system entity with ID: {}", id);
                    throw new OperationNotAllowedException(protectedConfig.message() + " (ID: " + id + ")");
                }
                break;
            }
        }
    }
}
