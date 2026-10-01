package net.lop223.helpdesksystem.service;

import net.lop223.helpdesksystem.dto.request.AppUserRequest;
import net.lop223.helpdesksystem.dto.response.AppUserResponse;

import java.util.List;

public interface AppUserService {
    List<AppUserResponse> getAll();
    AppUserResponse getById(Long id);
    AppUserResponse create(AppUserRequest request);
    AppUserResponse update(Long id, AppUserRequest request);
    void delete(Long id);
}