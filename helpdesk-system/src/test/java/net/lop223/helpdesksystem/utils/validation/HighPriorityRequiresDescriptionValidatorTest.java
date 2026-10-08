package net.lop223.helpdesksystem.utils.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import net.lop223.helpdesksystem.dto.request.TicketRequest;
import net.lop223.helpdesksystem.utils.enums.TicketPriority;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class HighPriorityRequiresDescriptionValidatorTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    private TicketRequest createBaseTicket(TicketPriority priority, String description) {
        TicketRequest request = new TicketRequest();
        request.setTitle("Server is down");
        request.setCategoryId(1L);
        request.setPriority(priority);
        request.setDescription(description);
        return request;
    }

    @Test
    @DisplayName("GIVEN priority HIGH AND description >= 20 chars WHEN validate THEN no violations")
    void shouldPassValidation_WhenPriorityIsHigh_AndDescriptionIsLongEnough() {
        TicketRequest request = createBaseTicket(
                TicketPriority.HIGH,
                "Database connection pool is completely exhausted"
        );

        Set<ConstraintViolation<TicketRequest>> violations = validator.validate(request);


        assertThat(violations)
                .filteredOn(v -> v.getConstraintDescriptor().getAnnotation() instanceof HighPriorityRequiresDescription)
                .isEmpty();
    }

    @Test
    @DisplayName("GIVEN priority HIGH AND description exactly 20 chars (with whitespace) WHEN validate THEN pass")
    void shouldPassValidation_WhenDescriptionExactly20CharsAfterTrim() {

        TicketRequest request = createBaseTicket(
                TicketPriority.HIGH,
                "   12345678901234567890   "
        );

        Set<ConstraintViolation<TicketRequest>> violations = validator.validate(request);

        assertThat(violations)
                .filteredOn(v -> v.getConstraintDescriptor().getAnnotation() instanceof HighPriorityRequiresDescription)
                .isEmpty();
    }

    @Test
    @DisplayName("GIVEN priority HIGH AND description is too short WHEN validate THEN returns violation with detailed message")
    void shouldFailValidation_WhenPriorityIsHigh_AndDescriptionIsTooShort() {
        TicketRequest request = createBaseTicket(
                TicketPriority.HIGH,
                "Broken ASAP"
        );

        Set<ConstraintViolation<TicketRequest>> violations = validator.validate(request);

        Set<ConstraintViolation<TicketRequest>> customViolations = violations.stream()
                .filter(v -> v.getConstraintDescriptor().getAnnotation() instanceof HighPriorityRequiresDescription)
                .collect(java.util.stream.Collectors.toSet());

        assertThat(customViolations).hasSize(1);

        ConstraintViolation<TicketRequest> violation = customViolations.iterator().next();

        assertThat(violation.getPropertyPath().toString()).isEqualTo("description");

        assertThat(violation.getMessage())
                .isEqualTo("Ticket with priority HIGH must have a description of at least 20 characters (actual: 11)");
    }

    @Test
    @DisplayName("GIVEN priority HIGH AND description is null WHEN validate THEN returns violation")
    void shouldFailValidation_WhenPriorityIsHigh_AndDescriptionIsNull() {
        TicketRequest request = createBaseTicket(TicketPriority.HIGH, null);

        Set<ConstraintViolation<TicketRequest>> violations = validator.validate(request);

        Set<ConstraintViolation<TicketRequest>> customViolations = violations.stream()
                .filter(v -> v.getConstraintDescriptor().getAnnotation() instanceof HighPriorityRequiresDescription)
                .collect(java.util.stream.Collectors.toSet());

        assertThat(customViolations).hasSize(1);
        ConstraintViolation<TicketRequest> violation = customViolations.iterator().next();

        assertThat(violation.getPropertyPath().toString()).isEqualTo("description");
        assertThat(violation.getMessage())
                .isEqualTo("Ticket with priority HIGH must have a description of at least 20 characters (actual: 0)");
    }

    @Test
    @DisplayName("GIVEN priority HIGH AND description consists only of spaces WHEN validate THEN returns violation")
    void shouldFailValidation_WhenPriorityIsHigh_AndDescriptionIsOnlyWhitespace() {
        TicketRequest request = createBaseTicket(TicketPriority.HIGH, "         ");

        Set<ConstraintViolation<TicketRequest>> violations = validator.validate(request);

        Set<ConstraintViolation<TicketRequest>> customViolations = violations.stream()
                .filter(v -> v.getConstraintDescriptor().getAnnotation() instanceof HighPriorityRequiresDescription)
                .collect(java.util.stream.Collectors.toSet());

        assertThat(customViolations).hasSize(1);
        ConstraintViolation<TicketRequest> violation = customViolations.iterator().next();
        assertThat(violation.getMessage()).contains("(actual: 0)");
    }

    @ParameterizedTest
    @EnumSource(value = TicketPriority.class, names = {"LOW", "MEDIUM"})
    @DisplayName("GIVEN priority NOT HIGH (LOW, MEDIUM) AND short/null description WHEN validate THEN passes")
    void shouldPassValidation_WhenPriorityIsNotHigh_RegardlessOfDescription(TicketPriority priority) {
        // Для пріоритетів LOW / MEDIUM короткий опис дозволений
        TicketRequest request = createBaseTicket(priority, "Short");

        Set<ConstraintViolation<TicketRequest>> violations = validator.validate(request);

        assertThat(violations)
                .filteredOn(v -> v.getConstraintDescriptor().getAnnotation() instanceof HighPriorityRequiresDescription)
                .isEmpty();
    }
}