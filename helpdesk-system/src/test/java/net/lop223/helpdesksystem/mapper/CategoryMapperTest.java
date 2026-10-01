package net.lop223.helpdesksystem.mapper;

import net.lop223.helpdesksystem.dto.request.CategoryRequest;
import net.lop223.helpdesksystem.dto.response.CategoryResponse;
import net.lop223.helpdesksystem.entity.Category;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.*;

class CategoryMapperTest {

    private final CategoryMapper mapper = Mappers.getMapper(CategoryMapper.class);

    private static CategoryRequest request(String name, String description) {
        CategoryRequest request = new CategoryRequest();
        request.setName(name);
        request.setDescription(description);
        return request;
    }

    @Test
    @DisplayName("toEntity: копіює name і description у нову сутність")
    void toEntity_validRequest_mapsAllFields() {
        CategoryRequest request = request("Hardware", "Broken devices");

        Category entity = mapper.toEntity(request);

        assertNotNull(entity);
        assertEquals("Hardware", entity.getName());
        assertEquals("Broken devices", entity.getDescription());
        assertEquals(0L, entity.getId());
    }

    @Test
    @DisplayName("toEntity: повертає null для null запиту")
    void toEntity_nullRequest_returnsNull() {
        CategoryRequest request = null;

        Category entity = mapper.toEntity(request);

        assertNull(entity);
    }

    @Test
    @DisplayName("toResponse: копіює id, name і description")
    void toResponse_validEntity_mapsFields() {
        Category entity = Category.builder().id(12L).name("Network").description("VPN, Wi-Fi").build();

        CategoryResponse response = mapper.toResponse(entity);

        assertNotNull(response);
        assertEquals(12L, response.getId());
        assertEquals("Network", response.getName());
        assertEquals("VPN, Wi-Fi", response.getDescription());
    }

    @Test
    @DisplayName("toResponse: повертає null для null сутності")
    void toResponse_nullEntity_returnsNull() {
        Category entity = null;

        CategoryResponse response = mapper.toResponse(entity);

        assertNull(response);
    }

    @Test
    @DisplayName("updateEntityFromRequest: перезаписує поля, зберігаючи id")
    void updateEntityFromRequest_validRequest_overwritesFieldsKeepsId() {
        Category entity = Category.builder().id(5L).name("Old").description("Old desc").build();
        CategoryRequest request = request("New", null);

        mapper.updateEntityFromRequest(request, entity);

        assertEquals(5L, entity.getId());
        assertEquals("New", entity.getName());
        assertNull(entity.getDescription());
    }

    @Test
    @DisplayName("updateEntityFromRequest: null запит залишає сутність без змін")
    void updateEntityFromRequest_nullRequest_leavesEntityUnchanged() {
        Category entity = Category.builder().id(6L).name("Keep").description("Keep desc").build();

        mapper.updateEntityFromRequest(null, entity);

        assertEquals(6L, entity.getId());
        assertEquals("Keep", entity.getName());
        assertEquals("Keep desc", entity.getDescription());
    }
}
