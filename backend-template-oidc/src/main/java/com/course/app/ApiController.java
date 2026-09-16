package com.course.app;

import com.course.app.security.CurrentUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiController {

    @GetMapping("/api/public/ping")
    public String publicPing() {
        return "pong (no auth required)";
    }

    @GetMapping("/api/secure/me")
    public CurrentUser me(@AuthenticationPrincipal CurrentUser user) {
        return user;
    }

    @GetMapping("/api/admin/reports")
    public String reports() {
        return "Sensitive report data - ADMIN only";
    }
}
