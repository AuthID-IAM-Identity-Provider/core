package io.authid.core.containers.user.contracts;

import io.authid.core.containers.user.entities.UserEntity;
import io.authid.core.shared.rest.contracts.RestService;

import java.util.UUID;

public interface UserCommonService extends RestService<UserEntity, UUID, Object, Object> {
    public  void sayHello();
}
