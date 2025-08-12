package io.authid.core.containers.user.services;

import io.authid.core.containers.user.contracts.UserCommonService;
import io.authid.core.containers.user.entities.UserEntity;
import io.authid.core.containers.user.exceptions.UserNotFoundException;
import io.authid.core.containers.user.repositories.UserRepository;
import io.authid.core.containers.user.request.CreateUserRequest;
import io.authid.core.containers.user.request.UpdateUserRequest;
import io.authid.core.shared.components.exception.BaseApplicationException;
import io.authid.core.shared.rest.contracts.hooks.RestServiceHooks;
import io.authid.core.shared.rest.services.RestServiceImpl;
import io.authid.core.shared.utils.UniPaginatedResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@Qualifier("userCommonServiceImpl")
@RequiredArgsConstructor
public class UserCommonServiceImpl extends RestServiceImpl<
    UserEntity,
    UUID,
    CreateUserRequest,
    UpdateUserRequest> implements UserCommonService {

    private final UserRepository repository;

    private final PasswordEncoder passwordEncoder;

    @Override
    @SuppressWarnings("unchecked")
    protected UserRepository getRepository() {
        return this.repository;
    }

    @Override
    protected List<String> getSearchableColumns() {
        return List.of();
    }

    @Override
    protected List<String> getFilterableColumns() {
        return List.of();
    }

    @Override
    protected String getCursorValue(UserEntity entity) {
        return entity.getId().toString();
    }

    @Override
    protected UserEntity onCreating(CreateUserRequest request) {
        return UserEntity.builder()
            .name(request.getName())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .emailVerifiedAt(null)
            .failedLoginAttempts(null)
            .loginCount(0)
            .build();

    }

    @Override
    protected void onUpdate(UpdateUserRequest updateRequest, UserEntity entity) {
        // To do validate update request via hooks
    }

    @Override
    protected RestServiceHooks<UserEntity, UUID, CreateUserRequest, UpdateUserRequest> getHooks() {
        return new RestServiceHooks<UserEntity, UUID, CreateUserRequest, UpdateUserRequest>() {

            @Override
            public void beforeUpdate(UpdateUserRequest request) {
                log.info("Executing before update actions: {}", request);
            }

            @Override
            public UserEntity onUpdating(UpdateUserRequest request) {
                return null;
            }

            @Override
            public void afterUpdate(UserEntity entity) {

            }

            @Override
            public void beforeFindById(UUID uuid) {

            }

            @Override
            public void onFindingById(UUID uuid) {

            }

            @Override
            public void afterFindById(UserEntity entity) {

            }

            @Override
            public BaseApplicationException onNotFound(UUID uuid) {
                return null;
            }

            @Override
            public List<String> getSearchableColumns() {
                return List.of();
            }

            @Override
            public List<String> getFilterableColumns() {
                return List.of();
            }

            @Override
            public String getCursorValue(UserEntity entity) {
                return entity.getId().toString();
            }

            @Override
            public void beforeFetchAll(String searchTerm, Map<String, Object> filters, Pageable pageable, String cursor) {

            }

            @Override
            public void onFetchingAll() {

            }

            @Override
            public void afterFetchAll(UniPaginatedResult<UserEntity> result) {

            }

            @Override
            public void beforeDelete(UUID uuid) {

            }

            @Override
            public void onDeletingById(UUID uuid) {

            }

            @Override
            public void afterDelete(UUID uuid) {

            }

            @Override
            public void beforeCreate(CreateUserRequest request) {

            }

            @Override
            public UserEntity onCreating(CreateUserRequest request) {
                return null;
            }

            @Override
            public void afterCreate(UserEntity entity) {

            }
        };
    }

    @Override
    protected BaseApplicationException onNotFound(UUID uuid) {
        return new UserNotFoundException(uuid);
    }
}
