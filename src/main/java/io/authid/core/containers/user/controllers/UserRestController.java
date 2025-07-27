package io.authid.core.containers.user.controllers;

import io.authid.core.containers.user.contracts.UserCommonService;
import io.authid.core.containers.user.entities.UserEntity;
import io.authid.core.containers.user.request.CreateUserRequest;
import io.authid.core.containers.user.request.UpdateUserRequest;
import io.authid.core.shared.rest.contracts.RestService;
import io.authid.core.shared.rest.controllers.RestController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;

@Slf4j
@RequestMapping("/api/v1/resources/users")
@org.springframework.web.bind.annotation.RestController
public class UserRestController extends RestController<UserEntity, UUID, CreateUserRequest, UpdateUserRequest> {

    private final UserCommonService userService;

    public UserRestController(
            @Qualifier("userCommonServiceImpl") RestService<UserEntity, UUID, CreateUserRequest, UpdateUserRequest> service
    ) {
        this.userService = (UserCommonService) service;
    }

    @Override
    public RestService<UserEntity, UUID, CreateUserRequest, UpdateUserRequest> getService() {
        return this.userService;
    }
}
