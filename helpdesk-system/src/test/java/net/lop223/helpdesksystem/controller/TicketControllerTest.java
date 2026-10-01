package net.lop223.helpdesksystem.controller;

import net.lop223.helpdesksystem.dto.request.TicketRequest;
import net.lop223.helpdesksystem.dto.request.TicketStatusUpdateRequest;
import net.lop223.helpdesksystem.dto.response.TicketResponse;
import net.lop223.helpdesksystem.service.TicketService;
import net.lop223.helpdesksystem.utils.enums.TicketStatus;
import net.lop223.helpdesksystem.utils.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketControllerTest {

    @Mock
    private TicketService ticketService;

    @InjectMocks
    private TicketController ticketController;

    private static TicketResponse response(long id) {
        TicketResponse response = new TicketResponse();
        response.setId(id);
        return response;
    }

    @Test
    @DisplayName("getAll: повертає 200 зі списком тікетів")
    void getAll_ticketsExist_returnsOk() {
        List<TicketResponse> tickets = List.of(response(1L), response(2L));
        when(ticketService.getAll()).thenReturn(tickets);

        ResponseEntity<List<TicketResponse>> result = ticketController.getAll();

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(tickets, result.getBody());
    }

    @Test
    @DisplayName("getById: повертає 200 з тікетом")
    void getById_existingId_returnsOk() {
        when(ticketService.getById(1L)).thenReturn(response(1L));

        ResponseEntity<TicketResponse> result = ticketController.getById(1L);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(1L, result.getBody().getId());
    }

    @Test
    @DisplayName("getById: пробрасує ResourceNotFoundException із сервісу")
    void getById_missingId_propagatesResourceNotFoundException() {
        when(ticketService.getById(9L)).thenThrow(new ResourceNotFoundException("Ticket not found with id: 9"));

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> ticketController.getById(9L)
        );

        assertEquals("Ticket not found with id: 9", ex.getMessage());
    }

    @Test
    @DisplayName("create: бере username із claim 'preferred_username' JWT і повертає 201")
    void create_jwtPrincipal_usesPreferredUsernameClaim() {
        TicketRequest request = new TicketRequest();
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("preferred_username", "jwt-user")
                .build();
        Authentication authentication = new TestingAuthenticationToken(jwt, null);
        when(ticketService.create(request, "jwt-user")).thenReturn(response(10L));

        ResponseEntity<TicketResponse> result = ticketController.create(request, authentication);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(10L, result.getBody().getId());
        verify(ticketService, times(1)).create(request, "jwt-user");
    }

    @Test
    @DisplayName("create: бере username з OidcUser.getPreferredUsername()")
    void create_oidcPrincipal_usesPreferredUsername() {
        TicketRequest request = new TicketRequest();
        OidcUser oidcUser = mock(OidcUser.class);
        when(oidcUser.getPreferredUsername()).thenReturn("oidc-user");
        Authentication authentication = new TestingAuthenticationToken(oidcUser, null);
        when(ticketService.create(request, "oidc-user")).thenReturn(response(11L));

        ResponseEntity<TicketResponse> result = ticketController.create(request, authentication);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(ticketService).create(any(TicketRequest.class), captor.capture());
        assertEquals("oidc-user", captor.getValue());
    }

    @Test
    @DisplayName("create: для іншого типу principal використовує authentication.getName()")
    void create_plainPrincipal_fallsBackToAuthenticationName() {
        TicketRequest request = new TicketRequest();
        Authentication authentication = new TestingAuthenticationToken("plain-user", null);
        when(ticketService.create(request, "plain-user")).thenReturn(response(12L));

        ResponseEntity<TicketResponse> result = ticketController.create(request, authentication);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        verify(ticketService).create(request, "plain-user");
    }

    @Test
    @DisplayName("create: JWT без 'preferred_username' передає у сервіс null")
    void create_jwtWithoutPreferredUsername_passesNullUsername() {
        TicketRequest request = new TicketRequest();
        Jwt jwt = Jwt.withTokenValue("token").header("alg", "none").claim("sub", "abc").build();
        Authentication authentication = new TestingAuthenticationToken(jwt, null);
        when(ticketService.create(request, null)).thenThrow(new ResourceNotFoundException("User not found: null"));

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> ticketController.create(request, authentication)
        );

        assertEquals("User not found: null", ex.getMessage());
    }

    @Test
    @DisplayName("create: викидає NullPointerException без автентифікації і не звертається до сервісу")
    void create_nullAuthentication_throwsNullPointerException() {
        TicketRequest request = new TicketRequest();

        assertThrows(NullPointerException.class, () -> ticketController.create(request, null));

        verifyNoInteractions(ticketService);
    }

    @Test
    @DisplayName("update: повертає 200 з оновленим тікетом")
    void update_validRequest_returnsOk() {
        TicketRequest request = new TicketRequest();
        when(ticketService.update(3L, request)).thenReturn(response(3L));

        ResponseEntity<TicketResponse> result = ticketController.update(3L, request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(3L, result.getBody().getId());
    }

    @Test
    @DisplayName("updateStatus: повертає 200 і передає запит у сервіс")
    void updateStatus_validRequest_returnsOk() {
        TicketStatusUpdateRequest request = new TicketStatusUpdateRequest();
        request.setStatus(TicketStatus.CLOSED);
        when(ticketService.updateStatus(4L, request)).thenReturn(response(4L));

        ResponseEntity<TicketResponse> result = ticketController.updateStatus(4L, request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        ArgumentCaptor<TicketStatusUpdateRequest> captor = ArgumentCaptor.forClass(TicketStatusUpdateRequest.class);
        verify(ticketService).updateStatus(eq(4L), captor.capture());
        assertEquals(TicketStatus.CLOSED, captor.getValue().getStatus());
    }

    @Test
    @DisplayName("delete: повертає 204 і викликає сервіс")
    void delete_existingId_returnsNoContent() {
        Long id = 5L;

        ResponseEntity<Void> result = ticketController.delete(id);

        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        assertNull(result.getBody());
        verify(ticketService, times(1)).delete(5L);
    }

    @Test
    @DisplayName("delete: пробрасує ResourceNotFoundException із сервісу")
    void delete_missingId_propagatesResourceNotFoundException() {
        doThrow(new ResourceNotFoundException("Ticket not found with id: 6")).when(ticketService).delete(6L);

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> ticketController.delete(6L)
        );

        assertEquals("Ticket not found with id: 6", ex.getMessage());
    }
}
