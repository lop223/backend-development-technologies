package net.lop223.userservice.service.impl;

import net.lop223.userservice.dto.request.AppUserRequest;
import net.lop223.userservice.dto.request.AppUserResponse;
import net.lop223.userservice.entity.AppUser;
import net.lop223.userservice.repository.AppUserRepository;
import net.lop223.userservice.service.AppUserService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class AppUserServiceImpl implements AppUserService {
    private final AppUserRepository repository;

    public AppUserServiceImpl(AppUserRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<AppUserResponse> getAll() {
        return repository.findAll().stream()
                .map(AppUserResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public AppUserResponse getById(Long id) {
        return AppUserResponse.from(findById(id));
    }

    public AppUserResponse create(AppUserRequest request) {
        AppUser user = new AppUser(
                request.username(),
                request.email(),
                request.fullName(),
                request.role()
        );
        return AppUserResponse.from(repository.save(user));
    }

    public AppUserResponse update(Long id, AppUserRequest request) {
        AppUser user = findById(id);
        user.update(
                request.username(),
                request.email(),
                request.fullName(),
                request.role()
        );
        return AppUserResponse.from(repository.save(user));
    }

    public void delete(Long id) {
        repository.delete(findById(id));
    }

    private AppUser findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found: " + id
                ));
    }

}
