package net.lop223.helpdesksystem.utils.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import net.lop223.helpdesksystem.dto.request.TicketRequest;
import net.lop223.helpdesksystem.utils.enums.TicketPriority;

public class HighPriorityRequiresDescriptionValidator
        implements ConstraintValidator<HighPriorityRequiresDescription, TicketRequest> {

    private int minLength;

    @Override
    public void initialize(HighPriorityRequiresDescription annotation) {
        this.minLength = annotation.minLength();
    }

    @Override
    public boolean isValid(TicketRequest request, ConstraintValidatorContext context) {
        if (request == null || request.getPriority() != TicketPriority.HIGH) {
            return true;
        }

        String description = request.getDescription();
        int actualLength = description == null ? 0 : description.trim().length();
        if (actualLength >= minLength) {
            return true;
        }

        String message = String.format(
                "Ticket with priority %s must have a description of at least %d characters (actual: %d)",
                TicketPriority.HIGH, minLength, actualLength);
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
                .addPropertyNode("description")
                .addConstraintViolation();
        return false;
    }
}
