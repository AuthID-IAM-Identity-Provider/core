package io.authid.core.containers.user.controllers;

import io.authid.core.containers.user.contracts.UserCommonService;
import io.authid.core.containers.user.entities.UserEntity;
import io.authid.core.containers.user.request.CreateUserRequest;
import io.authid.core.containers.user.request.DeleteUserRequest;
import io.authid.core.containers.user.request.UpdateUserRequest;
import io.authid.core.containers.user.response.*;
import io.authid.core.containers.user.transformer.UserRestTransformer;
import io.authid.core.shared.rest.contracts.RestService;
import io.authid.core.shared.rest.controllers.RestController;
import io.authid.core.shared.rest.transformer.RestTransformer;
import io.authid.core.shared.utils.UniResponseFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;

@Slf4j
@RequestMapping("/api/v1/resources/users")
@org.springframework.web.bind.annotation.RestController
public class UserRestController extends RestController<UserEntity, UUID, CreateUserRequest, UpdateUserRequest, DeleteUserRequest, IndexUserResponse, DetailUserResponse, CreateUserResponse, UpdateUserResponse, DeleteUserResponse> {

    private final UserCommonService userService;

    private final UserRestTransformer transformer;

    private final UniResponseFactory response;

    public UserRestController(
            UniResponseFactory responseFactory,
            @Qualifier("userCommonServiceImpl") RestService<UserEntity, UUID, CreateUserRequest, UpdateUserRequest> service, UserRestTransformer userRestTransformer
    ) {
        this.userService = (UserCommonService) service;
        this.transformer = userRestTransformer;
        this.response = responseFactory;
    }

    @Override
    public RestService<UserEntity, UUID, CreateUserRequest, UpdateUserRequest> getService() {
        return this.userService;
    }

    @Override
    public RestTransformer<UserEntity, IndexUserResponse, DetailUserResponse, CreateUserResponse, UpdateUserResponse, DeleteUserResponse> getTransformer() {
        return this.transformer;
    }

    @Override
    public UniResponseFactory getResponseFactory() {
        return this.response;
    }
}
