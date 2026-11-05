package net.lop223.userservice.repository;

import net.lop223.userservice.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
}
