package net.lop223.helpdesksystem.mapper;

import net.lop223.helpdesksystem.dto.request.AppUserRequest;
import net.lop223.helpdesksystem.dto.response.AppUserResponse;
import net.lop223.helpdesksystem.entity.AppUser;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface AppUserMapper {

    AppUser toEntity(AppUserRequest request);

    AppUserResponse toResponse(AppUser entity);

    void updateEntityFromRequest(AppUserRequest request, @MappingTarget AppUser entity);
}