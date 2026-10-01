package net.lop223.helpdesksystem.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.lop223.helpdesksystem.dto.request.CategoryRequest;
import net.lop223.helpdesksystem.dto.response.CategoryResponse;
import net.lop223.helpdesksystem.entity.Category;
import net.lop223.helpdesksystem.mapper.CategoryMapper;
import net.lop223.helpdesksystem.repository.CategoryRepository;
import net.lop223.helpdesksystem.service.CategoryService;
import net.lop223.helpdesksystem.utils.exception.ResourceNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService
{
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public List<CategoryResponse> getAll() {
        return categoryRepository.findAll().stream().map(categoryMapper::toResponse).toList();
    }

    @Override
    public CategoryResponse getById(Long id) {
        return categoryRepository.findById(id).map(categoryMapper::toResponse).orElse(null);
    }


    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public CategoryResponse create(CategoryRequest request) {
        Category category = categoryMapper.toEntity(request);
        Category saved = categoryRepository.save(category);
        return categoryMapper.toResponse(saved);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = findEntityById(id);
        categoryMapper.updateEntityFromRequest(request, category);
        Category saved = categoryRepository.save(category);
        return categoryMapper.toResponse(saved);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(Long id) {
        Category category = findEntityById(id);
        categoryRepository.delete(category);
    }

    private Category findEntityById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
    }
}
