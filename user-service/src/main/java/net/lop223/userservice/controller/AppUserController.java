package net.lop223.userservice.controller;

import jakarta.validation.Valid;
import net.lop223.userservice.dto.request.AppUserRequest;
import net.lop223.userservice.dto.request.AppUserResponse;
import net.lop223.userservice.service.AppUserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class AppUserController {

    private final AppUserService userService;

    public AppUserController(AppUserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<AppUserResponse> getAll() {
        return userService.getAll();
    }

    @GetMapping("/{id}")
    public AppUserResponse getById(@PathVariable Long id) {
        return userService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AppUserResponse create(@Valid @RequestBody AppUserRequest request) {
        return userService.create(request);
    }

    @PutMapping("/{id}")
    public AppUserResponse update(
            @PathVariable Long id,
            @Valid @RequestBody AppUserRequest request
    ) {
        return userService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        userService.delete(id);
    }
}