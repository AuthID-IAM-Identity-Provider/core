package io.authid.core.containers.user.controllers;

import io.authid.core.containers.user.contracts.UserCommonService;
import io.authid.core.containers.user.entities.UserEntity;
import io.authid.core.shared.rest.contracts.RestService;
import io.authid.core.shared.rest.controllers.RestController;
import io.authid.core.shared.utils.UniResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;

@Slf4j
@RequestMapping("/api/v1/resources/user")
@org.springframework.web.bind.annotation.RestController
public class UserRestController extends RestController<UserEntity, UUID, Object, Object> {

    private final UserCommonService userService;

    public UserRestController(
            @Qualifier("userCommonServiceImpl") RestService<?,?,?,?> service
    ) {
        this.userService = (UserCommonService) service;
    }

    @Override
    public RestService<UserEntity, UUID, Object, Object> getService() {
        return this.userService;
    }

    @GetMapping("/check")
    public ResponseEntity<UniResponse<String>> check(){
        this.userService.sayHello();
        return ResponseEntity.ok(UniResponse.success(200, "response", this.userService.toString(), createMeta(null)));
    }
}
