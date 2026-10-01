package net.lop223.helpdesksystem.controller.web;

import net.lop223.helpdesksystem.dto.request.AppUserRequest;
import net.lop223.helpdesksystem.dto.response.AppUserResponse;
import net.lop223.helpdesksystem.service.AppUserService;
import net.lop223.helpdesksystem.utils.enums.Role;
import net.lop223.helpdesksystem.utils.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppUserWebControllerTest {

    @Mock
    private AppUserService appUserService;

    @InjectMocks
    private AppUserWebController appUserWebController;

    @Test
    @DisplayName("list: кладе користувачів у модель і повертає users/list")
    void list_usersExist_populatesModel() {
        Model model = new ExtendedModelMap();
        List<AppUserResponse> users = List.of(new AppUserResponse());
        when(appUserService.getAll()).thenReturn(users);

        String view = appUserWebController.list(model);

        assertEquals("users/list", view);
        assertEquals(users, model.getAttribute("users"));
    }

    @Test
    @DisplayName("newForm: кладе порожній запит і isEdit=false")
    void newForm_always_populatesEmptyRequest() {
        Model model = new ExtendedModelMap();

        String view = appUserWebController.newForm(model);

        assertEquals("users/form", view);
        assertInstanceOf(AppUserRequest.class, model.getAttribute("appUserRequest"));
        assertEquals(false, model.getAttribute("isEdit"));
        verifyNoInteractions(appUserService);
    }

    @Test
    @DisplayName("editForm: заповнює запит даними користувача і ставить isEdit=true")
    void editForm_existingUser_prefillsRequest() {
        Model model = new ExtendedModelMap();
        AppUserResponse user = new AppUserResponse();
        user.setUsername("alice");
        user.setEmail("alice@mail.com");
        user.setFullName("Alice Smith");
        user.setRole(Role.AGENT);
        when(appUserService.getById(1L)).thenReturn(user);

        String view = appUserWebController.editForm(1L, model);

        assertEquals("users/form", view);
        AppUserRequest request = (AppUserRequest) model.getAttribute("appUserRequest");
        assertNotNull(request);
        assertEquals("alice", request.getUsername());
        assertEquals("alice@mail.com", request.getEmail());
        assertEquals("Alice Smith", request.getFullName());
        assertEquals(Role.AGENT, request.getRole());
        assertEquals(1L, model.getAttribute("userId"));
        assertEquals(true, model.getAttribute("isEdit"));
    }

    @Test
    @DisplayName("editForm: пробрасує ResourceNotFoundException і не заповнює модель")
    void editForm_missingUser_propagatesResourceNotFoundException() {
        Model model = new ExtendedModelMap();
        when(appUserService.getById(2L)).thenThrow(new ResourceNotFoundException("AppUser not found with id: 2"));

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> appUserWebController.editForm(2L, model)
        );

        assertEquals("AppUser not found with id: 2", ex.getMessage());
        assertTrue(model.asMap().isEmpty());
    }

    @Test
    @DisplayName("create: створює користувача і робить redirect")
    void create_validRequest_createsAndRedirects() {
        AppUserRequest request = new AppUserRequest();

        String view = appUserWebController.create(request);

        assertEquals("redirect:/ui/users", view);
        verify(appUserService, times(1)).create(request);
    }

    @Test
    @DisplayName("update: оновлює користувача і робить redirect")
    void update_validRequest_updatesAndRedirects() {
        AppUserRequest request = new AppUserRequest();

        String view = appUserWebController.update(3L, request);

        assertEquals("redirect:/ui/users", view);
        verify(appUserService, times(1)).update(3L, request);
    }

    @Test
    @DisplayName("update: пробрасує ResourceNotFoundException для неіснуючого користувача")
    void update_missingUser_propagatesResourceNotFoundException() {
        AppUserRequest request = new AppUserRequest();
        when(appUserService.update(4L, request)).thenThrow(new ResourceNotFoundException("AppUser not found with id: 4"));

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> appUserWebController.update(4L, request)
        );

        assertEquals("AppUser not found with id: 4", ex.getMessage());
    }

    @Test
    @DisplayName("delete: видаляє користувача і робить redirect")
    void delete_existingId_deletesAndRedirects() {
        Long id = 5L;

        String view = appUserWebController.delete(id);

        assertEquals("redirect:/ui/users", view);
        verify(appUserService, times(1)).delete(5L);
    }
}
