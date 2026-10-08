package net.lop223.helpdesksystem.utils.aspect;

import net.lop223.helpdesksystem.dto.response.TicketResponse;
import net.lop223.helpdesksystem.utils.annotation.MaskSensitiveData;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MaskSensitiveDataAspectTest {
    @InjectMocks
    private MaskSensitiveDataAspect aspect;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private MaskSensitiveData maskAnnotation;

    @Test
    @DisplayName("Unit-test: Аспект повинен викликати proceed() і маскувати конфіденційний опис.")
    void shouldCallProceedAndMaskData() throws Throwable {
        TicketResponse rawResponse = new TicketResponse();
        rawResponse.setId(1L);
        rawResponse.setDescription("User email is john.doe@example.com, please assist.");

        when(maskAnnotation.mask()).thenReturn("******");
        when(joinPoint.proceed()).thenReturn(rawResponse);

        Object result = aspect.maskSensitiveData(joinPoint, maskAnnotation);

        verify(joinPoint, times(1)).proceed();

        assertNotNull(result);
        assertInstanceOf(TicketResponse.class, result);
        TicketResponse maskedResponse = (TicketResponse) result;

        assertEquals("User email is ******, please assist.", maskedResponse.getDescription());
    }

    @Test
    @DisplayName("Unit-test: Аспект повинен викликати proceed() для опису без вразливого тексту не змінити текст опису і додати maskDescription.")
    void shouldCallProceedNotSensitiveDataAndAddMaskDescription() throws Throwable {
        TicketResponse rawResponse = new TicketResponse();
        rawResponse.setId(1L);
        rawResponse.setDescription("This description is not sensitive.");

        when(maskAnnotation.maskDescription()).thenReturn("[MASKED]");
        when(joinPoint.proceed()).thenReturn(rawResponse);

        Object result = aspect.maskSensitiveData(joinPoint, maskAnnotation);

        verify(joinPoint, times(1)).proceed();

        assertNotNull(result);
        assertInstanceOf(TicketResponse.class, result);
        TicketResponse maskedResponse = (TicketResponse) result;

        assertEquals("[MASKED] This description is not sensitive.", maskedResponse.getDescription());
    }

    @Test
    @DisplayName("Unit-test: Аспект повинен поширювати виняток, якщо метод proceed() його генерує.")
    void shouldPropagateExceptionWhenTargetMethodFails() throws Throwable {
        when(joinPoint.proceed()).thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> aspect.maskSensitiveData(joinPoint, maskAnnotation));
        verify(joinPoint, times(1)).proceed();
    }
}
