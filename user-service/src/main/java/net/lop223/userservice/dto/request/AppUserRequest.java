package net.lop223.userservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import net.lop223.userservice.utils.enums.Role;

public record AppUserRequest(
        @NotBlank
        @Size(max = 100)
        String username,

        @NotBlank
        @Email
        @Size(max = 150)
        String email,

        @Size(max = 200)
        String fullName,

        @NotNull
        Role role
) {
}