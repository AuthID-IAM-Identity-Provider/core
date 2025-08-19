package io.authid.core.shared.rest.services.commons.find;

import io.authid.core.shared.rest.contracts.hooks.commons.FetchByIdHooks;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public record FindByIdQuery<T, ID>(
    ID uuid,
    JpaSpecificationExecutor<T> repository,
    FetchByIdHooks<T, ID> hooks) {
}
