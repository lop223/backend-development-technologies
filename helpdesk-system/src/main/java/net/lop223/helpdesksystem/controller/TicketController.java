package net.lop223.helpdesksystem.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.lop223.helpdesksystem.dto.request.TicketRequest;
import net.lop223.helpdesksystem.dto.request.TicketStatusUpdateRequest;
import net.lop223.helpdesksystem.dto.response.TicketResponse;
import net.lop223.helpdesksystem.service.TicketService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @GetMapping
    public ResponseEntity<List<TicketResponse>> getAll() {
        return ResponseEntity.ok(ticketService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ticketService.getById(id));
    }

    // TODO (Крок 6): creatorUsername братимемо з Authentication, а не з query-параметра
    @PostMapping
    public ResponseEntity<TicketResponse> create(@Valid @RequestBody TicketRequest request,
                                                 Authentication authentication) {
        String creatorUsername = extractUsername(authentication);
        TicketResponse created = ticketService.create(request, creatorUsername);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TicketResponse> update(@PathVariable Long id,
                                                 @Valid @RequestBody TicketRequest request) {
        return ResponseEntity.ok(ticketService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TicketResponse> updateStatus(@PathVariable Long id,
                                                       @Valid @RequestBody TicketStatusUpdateRequest request) {
        return ResponseEntity.ok(ticketService.updateStatus(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        ticketService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private String extractUsername(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof Jwt jwt) {
            return jwt.getClaimAsString("preferred_username");
        }
        if (principal instanceof OidcUser oidcUser) {
            return oidcUser.getPreferredUsername();
        }
        return authentication.getName();
    }
}

// g.prokhorov@chnu.edu.ua