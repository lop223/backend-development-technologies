package net.lop223.helpdesksystem.entity;

import jakarta.persistence.*;
import lombok.*;
import net.lop223.helpdesksystem.utils.enums.Role;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "app_user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, unique = true, name = "username", length = 100)
    private String username;

    @Column(nullable = false, unique = true, name = "email", length = 150)
    private String email;

    @Column(name = "full_name", length = 200)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createAt;

    @PrePersist
    protected void onCreate() {
        this.createAt = LocalDateTime.now();
    }
}
