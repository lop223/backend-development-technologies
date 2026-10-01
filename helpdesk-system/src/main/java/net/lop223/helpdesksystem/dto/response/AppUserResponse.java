package net.lop223.helpdesksystem.dto.response;

import lombok.Getter;
import lombok.Setter;
import net.lop223.helpdesksystem.utils.enums.Role;

import java.time.LocalDateTime;

@Getter
@Setter
public class AppUserResponse {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private Role role;
    private LocalDateTime createdAt;
}