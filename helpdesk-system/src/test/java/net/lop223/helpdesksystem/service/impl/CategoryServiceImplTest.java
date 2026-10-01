package net.lop223.helpdesksystem.service.impl;

import net.lop223.helpdesksystem.dto.request.CategoryRequest;
import net.lop223.helpdesksystem.dto.response.CategoryResponse;
import net.lop223.helpdesksystem.entity.Category;
import net.lop223.helpdesksystem.mapper.CategoryMapper;
import net.lop223.helpdesksystem.repository.CategoryRepository;
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
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private static Category category(long id, String name) {
        return Category.builder().id(id).name(name).description(name + " issues").build();
    }

    private static CategoryResponse response(long id, String name) {
        CategoryResponse response = new CategoryResponse();
        response.setId(id);
        response.setName(name);
        return response;
    }

    private static CategoryRequest request(String name) {
        CategoryRequest request = new CategoryRequest();
        request.setName(name);
        request.setDescription(name + " issues");
        return request;
    }

    @Test
    @DisplayName("getAll: повертає список DTO для всіх категорій")
    void getAll_categoriesExist_returnsMappedResponses() {
        Category hardware = category(1L, "Hardware");
        Category software = category(2L, "Software");
        when(categoryRepository.findAll()).thenReturn(List.of(hardware, software));
        when(categoryMapper.toResponse(hardware)).thenReturn(response(1L, "Hardware"));
        when(categoryMapper.toResponse(software)).thenReturn(response(2L, "Software"));

        List<CategoryResponse> result = categoryService.getAll();

        assertEquals(2, result.size());
        assertEquals("Hardware", result.get(0).getName());
        assertEquals("Software", result.get(1).getName());
        verify(categoryRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getAll: повертає порожній список, коли категорій немає")
    void getAll_noCategories_returnsEmptyList() {
        when(categoryRepository.findAll()).thenReturn(List.of());

        List<CategoryResponse> result = categoryService.getAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verifyNoInteractions(categoryMapper);
    }

    @Test
    @DisplayName("getById: повертає DTO, коли категорію знайдено")
    void getById_existingId_returnsResponse() {
        Category entity = category(4L, "Network");
        CategoryResponse expected = response(4L, "Network");
        when(categoryRepository.findById(4L)).thenReturn(Optional.of(entity));
        when(categoryMapper.toResponse(entity)).thenReturn(expected);

        CategoryResponse result = categoryService.getById(4L);

        assertSame(expected, result);
        assertEquals("Network", result.getName());
    }

    @Test
    @DisplayName("getById: повертає null (а не виняток), коли категорію не знайдено")
    void getById_missingId_returnsNull() {
        when(categoryRepository.findById(404L)).thenReturn(Optional.empty());

        CategoryResponse result = categoryService.getById(404L);

        assertNull(result);
        verifyNoInteractions(categoryMapper);
    }

    @Test
    @DisplayName("create: мапить запит у сутність, зберігає та повертає DTO")
    void create_validRequest_savesAndReturnsResponse() {
        CategoryRequest request = request("Access");
        Category mapped = category(0L, "Access");
        Category saved = category(11L, "Access");
        CategoryResponse expected = response(11L, "Access");
        when(categoryMapper.toEntity(request)).thenReturn(mapped);
        when(categoryRepository.save(any(Category.class))).thenReturn(saved);
        when(categoryMapper.toResponse(saved)).thenReturn(expected);

        CategoryResponse result = categoryService.create(request);

        assertNotNull(result);
        assertEquals(11L, result.getId());
        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
        verify(categoryRepository, times(1)).save(captor.capture());
        assertSame(mapped, captor.getValue());
        assertEquals("Access", captor.getValue().getName());
    }

    @Test
    @DisplayName("create: пробрасує виняток репозиторію та не викликає мапінг відповіді")
    void create_repositoryFails_propagatesExceptionAndSkipsResponseMapping() {
        CategoryRequest request = request("Duplicate");
        Category mapped = category(0L, "Duplicate");
        when(categoryMapper.toEntity(request)).thenReturn(mapped);
        when(categoryRepository.save(mapped)).thenThrow(new IllegalStateException("Category name must be unique"));

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> categoryService.create(request)
        );

        assertEquals("Category name must be unique", ex.getMessage());
        verify(categoryMapper, never()).toResponse(any());
    }

    @Test
    @DisplayName("update: оновлює наявну категорію із запиту та зберігає її")
    void update_existingId_updatesAndSaves() {
        CategoryRequest request = request("Renamed");
        Category existing = category(2L, "Old");
        CategoryResponse expected = response(2L, "Renamed");
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(existing));
        when(categoryRepository.save(existing)).thenReturn(existing);
        when(categoryMapper.toResponse(existing)).thenReturn(expected);

        CategoryResponse result = categoryService.update(2L, request);

        assertSame(expected, result);
        verify(categoryMapper, times(1)).updateEntityFromRequest(request, existing);
        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
        verify(categoryRepository).save(captor.capture());
        assertEquals(2L, captor.getValue().getId());
    }

    @Test
    @DisplayName("update: викидає ResourceNotFoundException і нічого не зберігає, коли категорії немає")
    void update_missingId_throwsResourceNotFoundException() {
        CategoryRequest request = request("Nope");
        when(categoryRepository.findById(-1L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> categoryService.update(-1L, request)
        );

        assertEquals("Category not found with id: -1", ex.getMessage());
        verify(categoryRepository, never()).save(any());
        verifyNoInteractions(categoryMapper);
    }

    @Test
    @DisplayName("delete: видаляє знайдену категорію")
    void delete_existingId_deletesEntity() {
        Category existing = category(8L, "Printers");
        when(categoryRepository.findById(8L)).thenReturn(Optional.of(existing));

        categoryService.delete(8L);

        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
        verify(categoryRepository, times(1)).delete(captor.capture());
        assertEquals("Printers", captor.getValue().getName());
    }

    @Test
    @DisplayName("delete: викидає ResourceNotFoundException і не видаляє, коли категорії немає")
    void delete_missingId_throwsResourceNotFoundException() {
        when(categoryRepository.findById(100L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> categoryService.delete(100L)
        );

        assertEquals("Category not found with id: 100", ex.getMessage());
        verify(categoryRepository, never()).delete(any());
    }
}
