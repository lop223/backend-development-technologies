package net.lop223.helpdesksystem.utils.aspect;

import lombok.extern.slf4j.Slf4j;
import net.lop223.helpdesksystem.dto.response.TicketResponse;
import net.lop223.helpdesksystem.utils.annotation.MaskSensitiveData;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Slf4j
@Aspect
@Component
public class MaskSensitiveDataAspect
{
    private static final Pattern SENSITIVE_PATTERN = Pattern.compile("(\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+" +
            "\\.[A-Z|a-z]{2,}\\b)|(\\b\\d{4}[- ]?\\d{4}[- ]?\\d{4}[- ]?\\d{4}\\b)");

    @Around("@annotation(maskConfig)")
    public Object maskSensitiveData(ProceedingJoinPoint joinPoint, MaskSensitiveData maskConfig) throws Throwable
    {
        Object result = joinPoint.proceed();
        if (result instanceof TicketResponse response) {
            if (response.getDescription() != null) {
                String origDescription = response.getDescription();
                String maskedDescription = SENSITIVE_PATTERN.matcher(origDescription).replaceAll(maskConfig.mask());

                if (maskedDescription.equals(origDescription)) {
                    maskedDescription = maskConfig.maskDescription() + " " + origDescription;
                }

                response.setDescription(maskedDescription);
                log.info("[AOP] Data masked for Ticket ID: {}", response.getId());
            }
        }
        return result;
    }
}
