package net.lop223.helpdesksystem.mapper;

import net.lop223.helpdesksystem.dto.request.AppUserRequest;
import net.lop223.helpdesksystem.dto.response.AppUserResponse;
import net.lop223.helpdesksystem.entity.AppUser;
import net.lop223.helpdesksystem.utils.enums.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.*;

class AppUserMapperTest {

    private final AppUserMapper mapper = Mappers.getMapper(AppUserMapper.class);

    private static AppUserRequest request() {
        AppUserRequest request = new AppUserRequest();
        request.setUsername("alice");
        request.setEmail("alice@mail.com");
        request.setFullName("Alice Smith");
        request.setRole(Role.ADMIN);
        return request;
    }

    @Test
    @DisplayName("toEntity: копіює всі поля запиту в нову сутність")
    void toEntity_validRequest_mapsAllFields() {
        AppUserRequest request = request();

        AppUser entity = mapper.toEntity(request);

        assertNotNull(entity);
        assertEquals("alice", entity.getUsername());
        assertEquals("alice@mail.com", entity.getEmail());
        assertEquals("Alice Smith", entity.getFullName());
        assertEquals(Role.ADMIN, entity.getRole());
        assertEquals(0L, entity.getId());
    }

    @Test
    @DisplayName("toEntity: повертає null для null запиту")
    void toEntity_nullRequest_returnsNull() {
        AppUserRequest request = null;

        AppUser entity = mapper.toEntity(request);

        assertNull(entity);
    }

    @Test
    @DisplayName("toResponse: копіює id, username, email, fullName і role")
    void toResponse_validEntity_mapsFields() {
        AppUser entity = AppUser.builder()
                .id(15L)
                .username("bob")
                .email("bob@mail.com")
                .fullName("Bob Brown")
                .role(Role.AGENT)
                .build();

        AppUserResponse response = mapper.toResponse(entity);

        assertNotNull(response);
        assertEquals(15L, response.getId());
        assertEquals("bob", response.getUsername());
        assertEquals("bob@mail.com", response.getEmail());
        assertEquals("Bob Brown", response.getFullName());
        assertEquals(Role.AGENT, response.getRole());
    }

    @Test
    @DisplayName("toResponse: повертає null для null сутності")
    void toResponse_nullEntity_returnsNull() {
        AppUser entity = null;

        AppUserResponse response = mapper.toResponse(entity);

        assertNull(response);
    }

    @Test
    @DisplayName("updateEntityFromRequest: перезаписує поля сутності, не змінюючи id")
    void updateEntityFromRequest_validRequest_overwritesFieldsKeepsId() {
        AppUser entity = AppUser.builder()
                .id(3L)
                .username("old")
                .email("old@mail.com")
                .fullName("Old Name")
                .role(Role.USER)
                .build();
        AppUserRequest request = request();

        mapper.updateEntityFromRequest(request, entity);

        assertEquals(3L, entity.getId());
        assertEquals("alice", entity.getUsername());
        assertEquals("alice@mail.com", entity.getEmail());
        assertEquals("Alice Smith", entity.getFullName());
        assertEquals(Role.ADMIN, entity.getRole());
    }

    @Test
    @DisplayName("updateEntityFromRequest: null запит залишає сутність без змін")
    void updateEntityFromRequest_nullRequest_leavesEntityUnchanged() {
        AppUser entity = AppUser.builder()
                .id(4L)
                .username("keep")
                .email("keep@mail.com")
                .role(Role.USER)
                .build();

        mapper.updateEntityFromRequest(null, entity);

        assertEquals(4L, entity.getId());
        assertEquals("keep", entity.getUsername());
        assertEquals("keep@mail.com", entity.getEmail());
        assertEquals(Role.USER, entity.getRole());
    }
}
