package net.lop223.userservice.service;

import net.lop223.userservice.dto.request.AppUserRequest;
import net.lop223.userservice.dto.request.AppUserResponse;

import java.util.List;

public interface AppUserService {
    List<AppUserResponse> getAll();
    AppUserResponse getById(Long id);
    AppUserResponse getByUsername(String username);
    AppUserResponse create(AppUserRequest request);
    AppUserResponse update(Long id, AppUserRequest request);
    void delete(Long id);
}
