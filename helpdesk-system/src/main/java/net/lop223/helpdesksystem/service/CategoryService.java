package net.lop223.helpdesksystem.service;

import net.lop223.helpdesksystem.dto.request.CategoryRequest;
import net.lop223.helpdesksystem.dto.response.CategoryResponse;

import java.util.List;

public interface CategoryService {
    List<CategoryResponse> getAll();
    CategoryResponse getById(Long id);
    CategoryResponse create(CategoryRequest request);
    CategoryResponse update(Long id, CategoryRequest request);
    void delete(Long id);
}