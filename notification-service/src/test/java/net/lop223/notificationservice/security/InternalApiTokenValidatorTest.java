package net.lop223.notificationservice.security;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class InternalApiTokenValidatorTest {

    @Test
    void acceptsMatchingToken() {
        InternalApiTokenValidator validator =
                new InternalApiTokenValidator("test-token");

        assertDoesNotThrow(() -> validator.validate("test-token"));
    }

    @Test
    void rejectsWrongToken() {
        InternalApiTokenValidator validator =
                new InternalApiTokenValidator("test-token");

        assertThrows(
                ResponseStatusException.class,
                () -> validator.validate("wrong-token")
        );
    }

    @Test
    void rejectsMissingToken() {
        InternalApiTokenValidator validator =
                new InternalApiTokenValidator("test-token");

        assertThrows(
                ResponseStatusException.class,
                () -> validator.validate(null)
        );
    }
}