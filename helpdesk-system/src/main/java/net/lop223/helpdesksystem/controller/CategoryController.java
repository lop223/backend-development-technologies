package net.lop223.helpdesksystem.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.lop223.helpdesksystem.dto.request.CategoryRequest;
import net.lop223.helpdesksystem.dto.response.CategoryResponse;
import net.lop223.helpdesksystem.service.CategoryService;
import net.lop223.helpdesksystem.utils.annotation.CreatedPostMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController
{
    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAll() {
        return ResponseEntity.ok(categoryService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getById(@PathVariable long id) {
        return ResponseEntity.ok(categoryService.getById(id));
    }

    @CreatedPostMapping
    public CategoryResponse create(@Valid @RequestBody CategoryRequest request) {
        return categoryService.create(request);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> update(@PathVariable long id,
                                                   @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(categoryService.update(id,request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
