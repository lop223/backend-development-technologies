package net.lop223.helpdesksystem.controller.web;

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
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryWebControllerTest {

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private CategoryWebController categoryWebController;

    @Test
    @DisplayName("list: кладе категорії в модель і повертає categories/list")
    void list_categoriesExist_populatesModel() {
        Model model = new ExtendedModelMap();
        List<CategoryResponse> categories = List.of(new CategoryResponse());
        when(categoryService.getAll()).thenReturn(categories);

        String view = categoryWebController.list(model);

        assertEquals("categories/list", view);
        assertEquals(categories, model.getAttribute("categories"));
    }

    @Test
    @DisplayName("newForm: кладе порожній запит і isEdit=false")
    void newForm_always_populatesEmptyRequest() {
        Model model = new ExtendedModelMap();

        String view = categoryWebController.newForm(model);

        assertEquals("categories/form", view);
        assertInstanceOf(CategoryRequest.class, model.getAttribute("categoryRequest"));
        assertEquals(false, model.getAttribute("isEdit"));
        verifyNoInteractions(categoryService);
    }

    @Test
    @DisplayName("editForm: заповнює запит даними категорії і ставить isEdit=true")
    void editForm_existingCategory_prefillsRequest() {
        Model model = new ExtendedModelMap();
        CategoryResponse category = new CategoryResponse();
        category.setName("Hardware");
        category.setDescription("Devices");
        when(categoryService.getById(1L)).thenReturn(category);

        String view = categoryWebController.editForm(1L, model);

        assertEquals("categories/form", view);
        CategoryRequest request = (CategoryRequest) model.getAttribute("categoryRequest");
        assertNotNull(request);
        assertEquals("Hardware", request.getName());
        assertEquals("Devices", request.getDescription());
        assertEquals(1L, model.getAttribute("categoryId"));
        assertEquals(true, model.getAttribute("isEdit"));
    }

    @Test
    @DisplayName("editForm: викидає NullPointerException, коли категорію не знайдено (сервіс повертає null)")
    void editForm_missingCategory_throwsNullPointerException() {
        Model model = new ExtendedModelMap();
        when(categoryService.getById(2L)).thenReturn(null);

        assertThrows(NullPointerException.class, () -> categoryWebController.editForm(2L, model));

        assertTrue(model.asMap().isEmpty());
    }

    @Test
    @DisplayName("create: створює категорію і робить redirect")
    void create_validRequest_createsAndRedirects() {
        CategoryRequest request = new CategoryRequest();

        String view = categoryWebController.create(request);

        assertEquals("redirect:/ui/categories", view);
        verify(categoryService, times(1)).create(request);
    }

    @Test
    @DisplayName("update: оновлює категорію і робить redirect")
    void update_validRequest_updatesAndRedirects() {
        CategoryRequest request = new CategoryRequest();

        String view = categoryWebController.update(3L, request);

        assertEquals("redirect:/ui/categories", view);
        verify(categoryService, times(1)).update(3L, request);
    }

    @Test
    @DisplayName("update: пробрасує ResourceNotFoundException для неіснуючої категорії")
    void update_missingCategory_propagatesResourceNotFoundException() {
        CategoryRequest request = new CategoryRequest();
        when(categoryService.update(4L, request)).thenThrow(new ResourceNotFoundException("Category not found with id: 4"));

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> categoryWebController.update(4L, request)
        );

        assertEquals("Category not found with id: 4", ex.getMessage());
    }

    @Test
    @DisplayName("delete: видаляє категорію і робить redirect")
    void delete_existingId_deletesAndRedirects() {
        Long id = 5L;

        String view = categoryWebController.delete(id);

        assertEquals("redirect:/ui/categories", view);
        verify(categoryService, times(1)).delete(5L);
    }
}
