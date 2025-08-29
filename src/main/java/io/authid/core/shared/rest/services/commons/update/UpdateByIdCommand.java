package io.authid.core.shared.rest.services.commons.update;

import org.springframework.data.jpa.repository.JpaRepository;

import io.authid.core.shared.rest.contracts.hooks.RestServiceHooks;

public record UpdateByIdCommand<T, ID, C, U>(
    ID uuid,
    U updateRequest,
    JpaRepository<T, ID> repository,
    RestServiceHooks<T, ID, C, U> hooks
){
}
