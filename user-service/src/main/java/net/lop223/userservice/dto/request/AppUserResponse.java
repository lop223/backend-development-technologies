package net.lop223.userservice.dto.request;

import net.lop223.userservice.entity.AppUser;
import net.lop223.userservice.utils.enums.Role;

import java.time.LocalDateTime;

public record AppUserResponse(
        Long id,
        String username,
        String email,
        String fullName,
        Role role,
        LocalDateTime createdAt
) {
    public static AppUserResponse from(AppUser user) {
        return new AppUserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}