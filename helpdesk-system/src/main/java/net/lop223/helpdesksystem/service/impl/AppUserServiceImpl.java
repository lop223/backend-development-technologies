package net.lop223.helpdesksystem.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.lop223.helpdesksystem.dto.request.AppUserRequest;
import net.lop223.helpdesksystem.dto.response.AppUserResponse;
import net.lop223.helpdesksystem.entity.AppUser;
import net.lop223.helpdesksystem.mapper.AppUserMapper;
import net.lop223.helpdesksystem.repository.AppUserRepository;
import net.lop223.helpdesksystem.service.AppUserService;
import net.lop223.helpdesksystem.utils.annotation.AuditAction;
import net.lop223.helpdesksystem.utils.exception.ResourceNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AppUserServiceImpl implements AppUserService {

    private final AppUserRepository appUserRepository;
    private final AppUserMapper appUserMapper;

    @Override
    @Transactional()
    public List<AppUserResponse> getAll() {
        return appUserRepository.findAll().stream()
                .map(appUserMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional()
    public AppUserResponse getById(Long id) {
        return appUserMapper.toResponse(findEntityById(id));
    }

    @Override
    @AuditAction(action = "USER_CREATED")
    @PreAuthorize("hasRole('ADMIN')")
    public AppUserResponse create(AppUserRequest request) {
        AppUser user = appUserMapper.toEntity(request);
        AppUser saved = appUserRepository.save(user);
        return appUserMapper.toResponse(saved);
    }

    @Override
    @AuditAction(action = "USER_UPDATED")
    @PreAuthorize("hasRole('ADMIN')")
    public AppUserResponse update(Long id, AppUserRequest request) {
        AppUser user = findEntityById(id);
        appUserMapper.updateEntityFromRequest(request, user);
        AppUser saved = appUserRepository.save(user);
        return appUserMapper.toResponse(saved);
    }

    @Override
    @AuditAction(action = "USER_DELETED")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(Long id) {
        AppUser user = findEntityById(id);
        appUserRepository.delete(user);
    }

    private AppUser findEntityById(Long id) {
        return appUserRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AppUser not found with id: " + id));
    }
}