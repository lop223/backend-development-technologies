package net.lop223.helpdesksystem.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import net.lop223.helpdesksystem.utils.enums.Role;

@Getter
@Setter
public class AppUserRequest {

    @NotBlank(message = "Username must not be blank")
    @Size(max = 100)
    private String username;

    @NotBlank(message = "Email must not be blank")
    @Email(message = "Email must be valid")
    @Size(max = 150)
    private String email;

    @Size(max = 200)
    private String fullName;

    @NotNull(message = "Role must be specified")
    private Role role;
}