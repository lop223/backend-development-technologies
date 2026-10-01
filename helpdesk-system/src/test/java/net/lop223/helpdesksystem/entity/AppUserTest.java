package net.lop223.helpdesksystem.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AppUserTest {

    private static final LocalDateTime PAST = LocalDateTime.of(2020, 1, 1, 0, 0);

    @Test
    @DisplayName("onCreate: встановлює дату створення для нового користувача")
    void onCreate_newUser_setsCreateAt() {
        AppUser user = new AppUser();

        user.onCreate();

        assertNotNull(user.getCreateAt());
        assertTrue(user.getCreateAt().isAfter(PAST));
    }

    @Test
    @DisplayName("onCreate: перезаписує попередньо задану дату створення")
    void onCreate_createAtPreset_overwritesWithCurrentTime() {
        AppUser user = AppUser.builder().createAt(PAST).build();

        user.onCreate();

        assertNotEquals(PAST, user.getCreateAt());
    }
}
