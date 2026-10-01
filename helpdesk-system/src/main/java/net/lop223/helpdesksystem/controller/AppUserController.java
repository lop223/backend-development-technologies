package net.lop223.helpdesksystem.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.lop223.helpdesksystem.dto.request.AppUserRequest;
import net.lop223.helpdesksystem.dto.response.AppUserResponse;
import net.lop223.helpdesksystem.service.AppUserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class AppUserController {

    private final AppUserService appUserService;

    @GetMapping
    public ResponseEntity<List<AppUserResponse>> getAll() {
        return ResponseEntity.ok(appUserService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppUserResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(appUserService.getById(id));
    }

    @PostMapping
    public ResponseEntity<AppUserResponse> create(@Valid @RequestBody AppUserRequest request) {
        AppUserResponse created = appUserService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AppUserResponse> update(@PathVariable Long id,
                                                  @Valid @RequestBody AppUserRequest request) {
        return ResponseEntity.ok(appUserService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        appUserService.delete(id);
        return ResponseEntity.noContent().build();
    }
}