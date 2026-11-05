package net.lop223.userservice.service;

import net.lop223.userservice.dto.request.AppUserRequest;
import net.lop223.userservice.dto.request.AppUserResponse;

import java.util.List;

public interface AppUserService {
    public List<AppUserResponse> getAll();
    public AppUserResponse getById(Long id);
    public AppUserResponse create(AppUserRequest request);
    public AppUserResponse update(Long id, AppUserRequest request);
    public void delete(Long id);
}
