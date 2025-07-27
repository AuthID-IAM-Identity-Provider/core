package io.authid.core.containers.user.services;

import io.authid.core.containers.user.contracts.UserCommonService;
import io.authid.core.containers.user.entities.UserEntity;
import io.authid.core.containers.user.repositories.UserRepository;
import io.authid.core.containers.user.request.CreateUserRequest;
import io.authid.core.containers.user.request.UpdateUserRequest;
import io.authid.core.shared.rest.contracts.RestRequest;
import io.authid.core.shared.rest.services.RestServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Qualifier("userCommonServiceImpl")
@RequiredArgsConstructor
public class UserCommonServiceImpl extends RestServiceImpl<UserEntity, UUID, CreateUserRequest, UpdateUserRequest> implements UserCommonService {

    private final UserRepository repository;

    @Override
    @SuppressWarnings("unchecked")
    public UserRepository getRepository() {
        return this.repository;
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
    protected String getCursorValue(UserEntity entity) {
        return entity.getId().toString();
    }

    @Override
    protected UserEntity onCreating(CreateUserRequest createRequest) {
        return null;
    }

    @Override
    protected void onUpdate(UpdateUserRequest updateRequest, UserEntity entity) {

    }
}
