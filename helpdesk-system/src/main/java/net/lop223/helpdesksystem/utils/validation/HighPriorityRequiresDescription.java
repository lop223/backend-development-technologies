package net.lop223.helpdesksystem.utils.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = HighPriorityRequiresDescriptionValidator.class)
public @interface HighPriorityRequiresDescription {

    String message() default "Ticket with priority HIGH must have a description of at least {minLength} characters";

    int minLength() default 20;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
