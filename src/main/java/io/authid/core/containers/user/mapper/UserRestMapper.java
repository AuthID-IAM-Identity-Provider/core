package io.authid.core.containers.user.mapper;

import org.mapstruct.Mapper;

import io.authid.core.containers.user.entities.UserEntity;
import io.authid.core.containers.user.request.CreateUserRequest;
import io.authid.core.containers.user.request.UpdateUserRequest;
import io.authid.core.shared.rest.mapper.RestMapper;

@Mapper(
  componentModel = "spring"
)
public interface UserRestMapper extends RestMapper<UserEntity, CreateUserRequest, UpdateUserRequest> {
  
}
