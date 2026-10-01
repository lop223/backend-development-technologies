package net.lop223.helpdesksystem.mapper;

import net.lop223.helpdesksystem.dto.request.CategoryRequest;
import net.lop223.helpdesksystem.dto.response.CategoryResponse;
import net.lop223.helpdesksystem.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    Category toEntity(CategoryRequest request);

    CategoryResponse toResponse(Category entity);

    void updateEntityFromRequest(CategoryRequest request, @MappingTarget Category entity);
}