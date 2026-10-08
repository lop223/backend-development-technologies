package net.lop223.helpdesksystem.utils.aspect;

import net.lop223.helpdesksystem.utils.annotation.ProtectSystemEntity;
import net.lop223.helpdesksystem.utils.exception.OperationNotAllowedException;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProtectSystemEntityAspectTest {

    @InjectMocks
    private ProtectSystemEntityAspect aspect;

    @Mock
    private JoinPoint joinPoint;

    @Mock
    private ProtectSystemEntity protectAnnotation;

    @Test
    @DisplayName("Unit-test: Аспект повинен генерувати виняток, якщо цільовий ідентифікатор зарезервовано.")
    void shouldThrowExceptionWhenIdIsReserved() {
        when(joinPoint.getArgs()).thenReturn(new Object[]{1L});
        when(protectAnnotation.reservedIds()).thenReturn(new long[]{1L, 3L});
        when(protectAnnotation.message()).thenReturn("Cannot delete system entity");

        assertThrows(OperationNotAllowedException.class,
                () -> aspect.checkProtectedEntity(joinPoint, protectAnnotation));
    }

    @Test
    @DisplayName("Unit-test: Аспект повинен проходити перевірку (PASS) без винятків, якщо цільовий ідентифікатор не зарезервовано.")
    void shouldPassWhenIdIsNotReserved() {
        when(joinPoint.getArgs()).thenReturn(new Object[]{5L});
        when(protectAnnotation.reservedIds()).thenReturn(new long[]{1L, 2L});

        assertDoesNotThrow(() -> aspect.checkProtectedEntity(joinPoint, protectAnnotation));
    }
}