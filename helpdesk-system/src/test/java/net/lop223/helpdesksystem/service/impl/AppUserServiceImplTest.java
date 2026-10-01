package net.lop223.helpdesksystem.service.impl;

import net.lop223.helpdesksystem.dto.request.AppUserRequest;
import net.lop223.helpdesksystem.dto.response.AppUserResponse;
import net.lop223.helpdesksystem.entity.AppUser;
import net.lop223.helpdesksystem.mapper.AppUserMapper;
import net.lop223.helpdesksystem.repository.AppUserRepository;
import net.lop223.helpdesksystem.utils.enums.Role;
import net.lop223.helpdesksystem.utils.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppUserServiceImplTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private AppUserMapper appUserMapper;

    @InjectMocks
    private AppUserServiceImpl appUserService;

    private static AppUser user(long id, String username) {
        return AppUser.builder()
                .id(id)
                .username(username)
                .email(username + "@mail.com")
                .fullName("Full " + username)
                .role(Role.USER)
                .build();
    }

    private static AppUserResponse response(long id, String username) {
        AppUserResponse response = new AppUserResponse();
        response.setId(id);
        response.setUsername(username);
        return response;
    }

    private static AppUserRequest request(String username) {
        AppUserRequest request = new AppUserRequest();
        request.setUsername(username);
        request.setEmail(username + "@mail.com");
        request.setFullName("Full " + username);
        request.setRole(Role.AGENT);
        return request;
    }

    @Test
    @DisplayName("getAll: повертає список DTO для всіх користувачів із репозиторію")
    void getAll_usersExist_returnsMappedResponses() {
        AppUser first = user(1L, "alice");
        AppUser second = user(2L, "bob");
        when(appUserRepository.findAll()).thenReturn(List.of(first, second));
        when(appUserMapper.toResponse(first)).thenReturn(response(1L, "alice"));
        when(appUserMapper.toResponse(second)).thenReturn(response(2L, "bob"));

        List<AppUserResponse> result = appUserService.getAll();

        assertEquals(2, result.size());
        assertEquals("alice", result.get(0).getUsername());
        assertEquals("bob", result.get(1).getUsername());
        verify(appUserRepository, times(1)).findAll();
        verify(appUserMapper, times(2)).toResponse(any(AppUser.class));
    }

    @Test
    @DisplayName("getAll: повертає порожній список, коли користувачів немає")
    void getAll_noUsers_returnsEmptyList() {
        when(appUserRepository.findAll()).thenReturn(List.of());

        List<AppUserResponse> result = appUserService.getAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verifyNoInteractions(appUserMapper);
    }

    @Test
    @DisplayName("getById: повертає DTO, коли користувача знайдено")
    void getById_existingId_returnsResponse() {
        AppUser entity = user(5L, "carol");
        AppUserResponse expected = response(5L, "carol");
        when(appUserRepository.findById(5L)).thenReturn(Optional.of(entity));
        when(appUserMapper.toResponse(entity)).thenReturn(expected);

        AppUserResponse result = appUserService.getById(5L);

        assertSame(expected, result);
        verify(appUserRepository).findById(5L);
    }

    @Test
    @DisplayName("getById: викидає ResourceNotFoundException, коли користувача не знайдено")
    void getById_missingId_throwsResourceNotFoundException() {
        when(appUserRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> appUserService.getById(99L)
        );

        assertEquals("AppUser not found with id: 99", ex.getMessage());
        verifyNoInteractions(appUserMapper);
    }

    @Test
    @DisplayName("getById: викидає ResourceNotFoundException для null id")
    void getById_nullId_throwsResourceNotFoundException() {
        when(appUserRepository.findById(null)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> appUserService.getById(null)
        );

        assertEquals("AppUser not found with id: null", ex.getMessage());
        verifyNoInteractions(appUserMapper);
    }

    @Test
    @DisplayName("create: мапить запит у сутність, зберігає та повертає DTO")
    void create_validRequest_savesAndReturnsResponse() {
        AppUserRequest request = request("dave");
        AppUser mapped = user(0L, "dave");
        AppUser saved = user(10L, "dave");
        AppUserResponse expected = response(10L, "dave");
        when(appUserMapper.toEntity(request)).thenReturn(mapped);
        when(appUserRepository.save(any(AppUser.class))).thenReturn(saved);
        when(appUserMapper.toResponse(saved)).thenReturn(expected);

        AppUserResponse result = appUserService.create(request);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        ArgumentCaptor<AppUser> captor = ArgumentCaptor.forClass(AppUser.class);
        verify(appUserRepository, times(1)).save(captor.capture());
        assertSame(mapped, captor.getValue());
        assertEquals("dave", captor.getValue().getUsername());
    }

    @Test
    @DisplayName("create: пробрасує виняток репозиторію та не викликає мапінг відповіді")
    void create_repositoryFails_propagatesExceptionAndSkipsResponseMapping() {
        AppUserRequest request = request("eve");
        AppUser mapped = user(0L, "eve");
        when(appUserMapper.toEntity(request)).thenReturn(mapped);
        when(appUserRepository.save(mapped)).thenThrow(new IllegalStateException("Duplicate username"));

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> appUserService.create(request)
        );

        assertEquals("Duplicate username", ex.getMessage());
        verify(appUserMapper, never()).toResponse(any());
    }

    @Test
    @DisplayName("update: оновлює наявну сутність із запиту та зберігає її")
    void update_existingId_updatesAndSaves() {
        AppUserRequest request = request("frank");
        AppUser existing = user(3L, "old");
        AppUserResponse expected = response(3L, "frank");
        when(appUserRepository.findById(3L)).thenReturn(Optional.of(existing));
        when(appUserRepository.save(existing)).thenReturn(existing);
        when(appUserMapper.toResponse(existing)).thenReturn(expected);

        AppUserResponse result = appUserService.update(3L, request);

        assertSame(expected, result);
        verify(appUserMapper, times(1)).updateEntityFromRequest(request, existing);
        ArgumentCaptor<AppUser> captor = ArgumentCaptor.forClass(AppUser.class);
        verify(appUserRepository).save(captor.capture());
        assertEquals(3L, captor.getValue().getId());
    }

    @Test
    @DisplayName("update: викидає ResourceNotFoundException і нічого не зберігає, коли користувача немає")
    void update_missingId_throwsResourceNotFoundException() {
        AppUserRequest request = request("ghost");
        when(appUserRepository.findById(42L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> appUserService.update(42L, request)
        );

        assertEquals("AppUser not found with id: 42", ex.getMessage());
        verify(appUserRepository, never()).save(any());
        verifyNoInteractions(appUserMapper);
    }

    @Test
    @DisplayName("delete: видаляє знайденого користувача")
    void delete_existingId_deletesEntity() {
        AppUser existing = user(7L, "henry");
        when(appUserRepository.findById(7L)).thenReturn(Optional.of(existing));

        appUserService.delete(7L);

        ArgumentCaptor<AppUser> captor = ArgumentCaptor.forClass(AppUser.class);
        verify(appUserRepository, times(1)).delete(captor.capture());
        assertEquals(7L, captor.getValue().getId());
    }

    @Test
    @DisplayName("delete: викидає ResourceNotFoundException і не видаляє, коли користувача немає")
    void delete_missingId_throwsResourceNotFoundException() {
        when(appUserRepository.findById(0L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> appUserService.delete(0L)
        );

        assertEquals("AppUser not found with id: 0", ex.getMessage());
        verify(appUserRepository, never()).delete(any());
    }
}
