package net.lop223.helpdesksystem.controller;

import net.lop223.helpdesksystem.dto.request.CategoryRequest;
import net.lop223.helpdesksystem.dto.response.CategoryResponse;
import net.lop223.helpdesksystem.service.CategoryService;
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
class CategoryControllerTest {

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private CategoryController categoryController;

    private static CategoryResponse response(long id) {
        CategoryResponse response = new CategoryResponse();
        response.setId(id);
        return response;
    }

    @Test
    @DisplayName("getAll: повертає 200 зі списком категорій")
    void getAll_categoriesExist_returnsOk() {
        List<CategoryResponse> categories = List.of(response(1L), response(2L));
        when(categoryService.getAll()).thenReturn(categories);

        ResponseEntity<List<CategoryResponse>> result = categoryController.getAll();

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(2, result.getBody().size());
    }

    @Test
    @DisplayName("getById: повертає 200 з категорією")
    void getById_existingId_returnsOk() {
        when(categoryService.getById(1L)).thenReturn(response(1L));

        ResponseEntity<CategoryResponse> result = categoryController.getById(1L);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1L, result.getBody().getId());
    }

    @Test
    @DisplayName("getById: повертає 200 з порожнім тілом, коли сервіс повертає null")
    void getById_missingId_returnsOkWithNullBody() {
        when(categoryService.getById(404L)).thenReturn(null);

        ResponseEntity<CategoryResponse> result = categoryController.getById(404L);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNull(result.getBody());
    }

    @Test
    @DisplayName("create: повертає 201 зі створеною категорією")
    void create_validRequest_returnsCreated() {
        CategoryRequest request = new CategoryRequest();
        when(categoryService.create(request)).thenReturn(response(3L));

        ResponseEntity<CategoryResponse> result = categoryController.create(request);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(3L, result.getBody().getId());
        verify(categoryService, times(1)).create(request);
    }

    @Test
    @DisplayName("update: повертає 200 з оновленою категорією")
    void update_validRequest_returnsOk() {
        CategoryRequest request = new CategoryRequest();
        when(categoryService.update(4L, request)).thenReturn(response(4L));

        ResponseEntity<CategoryResponse> result = categoryController.update(4L, request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(4L, result.getBody().getId());
    }

    @Test
    @DisplayName("update: пробрасує ResourceNotFoundException із сервісу")
    void update_missingId_propagatesResourceNotFoundException() {
        CategoryRequest request = new CategoryRequest();
        when(categoryService.update(5L, request)).thenThrow(new ResourceNotFoundException("Category not found with id: 5"));

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> categoryController.update(5L, request)
        );

        assertEquals("Category not found with id: 5", ex.getMessage());
    }

    @Test
    @DisplayName("delete: повертає 204 і викликає сервіс")
    void delete_existingId_returnsNoContent() {
        long id = 6L;

        ResponseEntity<Void> result = categoryController.delete(id);

        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        verify(categoryService, times(1)).delete(6L);
    }

    @Test
    @DisplayName("delete: пробрасує ResourceNotFoundException із сервісу")
    void delete_missingId_propagatesResourceNotFoundException() {
        doThrow(new ResourceNotFoundException("Category not found with id: 7")).when(categoryService).delete(7L);

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> categoryController.delete(7L)
        );

        assertEquals("Category not found with id: 7", ex.getMessage());
    }
}
