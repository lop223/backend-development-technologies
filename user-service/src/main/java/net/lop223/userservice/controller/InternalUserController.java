package net.lop223.userservice.controller;

import net.lop223.userservice.api.InternalUserResponse;
import net.lop223.userservice.security.InternalApiTokenValidator;
import net.lop223.userservice.service.AppUserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/internal/users")
public class InternalUserController {

    private static final String INTERNAL_TOKEN_HEADER = "X-Internal-Token";

    private final AppUserService userService;
    private final InternalApiTokenValidator tokenValidator;

    public InternalUserController(
            AppUserService userService,
            InternalApiTokenValidator tokenValidator
    ) {
        this.userService = userService;
        this.tokenValidator = tokenValidator;
    }

    @GetMapping
    public List<InternalUserResponse> getAll(
            @RequestHeader(INTERNAL_TOKEN_HEADER) String token
    ) {
        tokenValidator.validate(token);
        return userService.getAll().stream()
                .map(user -> new InternalUserResponse(user.id(), user.username()))
                .toList();
    }

    @GetMapping("/{id}")
    public InternalUserResponse getById(
            @RequestHeader(INTERNAL_TOKEN_HEADER) String token,
            @PathVariable Long id
    ) {
        tokenValidator.validate(token);
        var user = userService.getById(id);
        return new InternalUserResponse(user.id(), user.username());
    }

    @GetMapping("/by-username/{username}")
    public InternalUserResponse getByUsername(
            @RequestHeader(INTERNAL_TOKEN_HEADER) String token,
            @PathVariable String username
    ) {
        tokenValidator.validate(token);
        var user = userService.getByUsername(username);
        return new InternalUserResponse(user.id(), user.username());
    }
}