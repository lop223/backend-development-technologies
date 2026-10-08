package net.lop223.helpdesksystem.utils.aspect;

import net.lop223.helpdesksystem.utils.annotation.AuditAction;
import net.lop223.helpdesksystem.dto.response.AppUserResponse;
import net.lop223.helpdesksystem.service.AuditLogService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditActionAspectTest {

    @InjectMocks
    private AuditActionAspect aspect;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private JoinPoint joinPoint;

    @Mock
    private Signature signature;

    @Mock
    private AuditAction auditAction;

    @ParameterizedTest(name = "[{index}] {0} -> method ''{1}'' with expected userId={3}")
    @MethodSource("provideAuditScenarios")
    @DisplayName("Unit-test: Aspect should record audit log for create, update and delete")
    void shouldLogAuditActionOnSuccess(
            String action,
            String methodName,
            Object result,
            Long expectedUserId
    ) {
        when(auditAction.action()).thenReturn(action);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn(methodName);

        if (result == null && expectedUserId != null) {
            when(joinPoint.getArgs()).thenReturn(new Object[]{expectedUserId});
        }

        aspect.auditSuccess(joinPoint, auditAction, result);

        verify(auditLogService, times(1)).logAction(action, expectedUserId, methodName);
    }

    private static Stream<Arguments> provideAuditScenarios() {
        AppUserResponse userResponse = new AppUserResponse();
        userResponse.setId(42L);

        return Stream.of(
                Arguments.of("USER_CREATED", "create", userResponse, 42L),
                Arguments.of("USER_UPDATED", "update", userResponse, 42L),
                Arguments.of("USER_DELETED", "delete", null, 42L)
        );
    }
}