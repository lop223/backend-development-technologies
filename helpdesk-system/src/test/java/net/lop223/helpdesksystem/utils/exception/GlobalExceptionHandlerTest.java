package net.lop223.helpdesksystem.utils.exception;

import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @SuppressWarnings("unchecked")
    private static Map<String, Object> body(ResponseEntity<Object> response) {
        assertInstanceOf(Map.class, response.getBody());
        return (Map<String, Object>) response.getBody();
    }

    private static MethodArgumentNotValidException validationException(FieldError... errors) throws NoSuchMethodException {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        for (FieldError error : errors) {
            bindingResult.addError(error);
        }
        MethodParameter parameter = new MethodParameter(Object.class.getMethod("toString"), -1);
        return new MethodArgumentNotValidException(parameter, bindingResult);
    }

    @Test
    @DisplayName("handleNotFound: повертає 404 з повідомленням винятку")
    void handleNotFound_resourceMissing_returnsNotFoundBody() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Ticket not found with id: 1");

        ResponseEntity<Object> response = handler.handleNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Map<String, Object> body = body(response);
        assertEquals(404, body.get("status"));
        assertEquals("Not Found", body.get("error"));
        assertEquals("Ticket not found with id: 1", body.get("message"));
        assertInstanceOf(LocalDateTime.class, body.get("timestamp"));
    }

    @Test
    @DisplayName("handleNotFound: null повідомлення передається у тіло як null")
    void handleNotFound_nullMessage_returnsNullMessageInBody() {
        ResourceNotFoundException ex = new ResourceNotFoundException(null);

        ResponseEntity<Object> response = handler.handleNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Map<String, Object> body = body(response);
        assertTrue(body.containsKey("message"));
        assertNull(body.get("message"));
    }

    @Test
    @DisplayName("handleValidation: об'єднує помилки полів через '; ' і повертає 400")
    void handleValidation_multipleFieldErrors_joinsMessages() throws Exception {
        MethodArgumentNotValidException ex = validationException(
                new FieldError("request", "title", "Title must not be blank"),
                new FieldError("request", "priority", "Priority must be specified")
        );

        ResponseEntity<Object> response = handler.handleValidation(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = body(response);
        assertEquals(400, body.get("status"));
        assertEquals("Bad Request", body.get("error"));
        assertEquals("title: Title must not be blank; priority: Priority must be specified", body.get("message"));
    }

    @Test
    @DisplayName("handleValidation: без помилок полів повертає 'Validation error'")
    void handleValidation_noFieldErrors_returnsDefaultMessage() throws Exception {
        MethodArgumentNotValidException ex = validationException();

        ResponseEntity<Object> response = handler.handleValidation(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Validation error", body(response).get("message"));
    }

    @Test
    @DisplayName("handleConstraintViolation: повертає 400 з повідомленням винятку")
    void handleConstraintViolation_violation_returnsBadRequest() {
        ConstraintViolationException ex = new ConstraintViolationException("id: must be positive", Set.of());

        ResponseEntity<Object> response = handler.handleConstraintViolation(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("id: must be positive", body(response).get("message"));
    }

    @Test
    @DisplayName("handleAccessDenied: повертає 403 з фіксованим повідомленням, приховуючи деталі")
    void handleAccessDenied_anyException_returnsForbiddenWithGenericMessage() {
        AccessDeniedException ex = new AccessDeniedException("internal details");

        ResponseEntity<Object> response = handler.handleAccessDenied(ex);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        Map<String, Object> body = body(response);
        assertEquals(403, body.get("status"));
        assertEquals("Forbidden", body.get("error"));
        assertEquals("Access denied: insufficient permissions", body.get("message"));
    }
}
