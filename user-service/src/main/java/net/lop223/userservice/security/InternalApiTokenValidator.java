package net.lop223.userservice.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
public class InternalApiTokenValidator {

    private final byte[] expectedToken;

    public InternalApiTokenValidator(@Value("${internal.api.token}") String expectedToken) {
        if (expectedToken.isBlank()) {
            throw new IllegalArgumentException("Internal API token must not be blank");
        }
        this.expectedToken = expectedToken.getBytes(StandardCharsets.UTF_8);
    }

    public void validate(String suppliedToken) {
        if (suppliedToken == null
                || !MessageDigest.isEqual(
                expectedToken,
                suppliedToken.getBytes(StandardCharsets.UTF_8)
        )) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid internal API token"
            );
        }
    }
}