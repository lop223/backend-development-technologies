package net.lop223.helpdesksystem.controller;

import net.lop223.helpdesksystem.dto.request.AppUserRequest;
import net.lop223.helpdesksystem.dto.response.AppUserResponse;
import net.lop223.helpdesksystem.service.AppUserService;
import net.lop223.helpdesksystem.utils.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppUserControllerTest {

    @Mock
    private AppUserService appUserService;

    @InjectMocks
    private AppUserController appUserController;

    private static AppUserResponse response(long id) {
        AppUserResponse response = new AppUserResponse();
        response.setId(id);
        return response;
    }

    @Test
    @DisplayName("getAll: повертає 200 зі списком користувачів")
    void getAll_usersExist_returnsOk() {
        List<AppUserResponse> users = List.of(response(1L));
        when(appUserService.getAll()).thenReturn(users);

        ResponseEntity<List<AppUserResponse>> result = appUserController.getAll();

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(users, result.getBody());
    }

    @Test
    @DisplayName("getById: повертає 200 з користувачем")
    void getById_existingId_returnsOk() {
        when(appUserService.getById(1L)).thenReturn(response(1L));

        ResponseEntity<AppUserResponse> result = appUserController.getById(1L);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1L, result.getBody().getId());
    }

    @Test
    @DisplayName("getById: пробрасує ResourceNotFoundException із сервісу")
    void getById_missingId_propagatesResourceNotFoundException() {
        when(appUserService.getById(2L)).thenThrow(new ResourceNotFoundException("AppUser not found with id: 2"));

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> appUserController.getById(2L)
        );

        assertEquals("AppUser not found with id: 2", ex.getMessage());
    }

    @Test
    @DisplayName("create: повертає 201 зі створеним користувачем")
    void create_validRequest_returnsCreated() {
        AppUserRequest request = new AppUserRequest();
        when(appUserService.create(request)).thenReturn(response(3L));

        ResponseEntity<AppUserResponse> result = appUserController.create(request);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(3L, result.getBody().getId());
        verify(appUserService, times(1)).create(request);
    }

    @Test
    @DisplayName("update: повертає 200 з оновленим користувачем")
    void update_validRequest_returnsOk() {
        AppUserRequest request = new AppUserRequest();
        when(appUserService.update(4L, request)).thenReturn(response(4L));

        ResponseEntity<AppUserResponse> result = appUserController.update(4L, request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(4L, result.getBody().getId());
    }

    @Test
    @DisplayName("update: пробрасує ResourceNotFoundException із сервісу")
    void update_missingId_propagatesResourceNotFoundException() {
        AppUserRequest request = new AppUserRequest();
        when(appUserService.update(5L, request)).thenThrow(new ResourceNotFoundException("AppUser not found with id: 5"));

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> appUserController.update(5L, request)
        );

        assertEquals("AppUser not found with id: 5", ex.getMessage());
    }

    @Test
    @DisplayName("delete: повертає 204 і викликає сервіс")
    void delete_existingId_returnsNoContent() {
        Long id = 6L;

        ResponseEntity<Void> result = appUserController.delete(id);

        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        verify(appUserService, times(1)).delete(6L);
    }

    @Test
    @DisplayName("delete: пробрасує ResourceNotFoundException із сервісу")
    void delete_missingId_propagatesResourceNotFoundException() {
        doThrow(new ResourceNotFoundException("AppUser not found with id: 7")).when(appUserService).delete(7L);

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> appUserController.delete(7L)
        );

        assertEquals("AppUser not found with id: 7", ex.getMessage());
    }
}
