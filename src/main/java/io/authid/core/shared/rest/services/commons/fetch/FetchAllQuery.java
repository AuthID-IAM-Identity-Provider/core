package io.authid.core.shared.rest.services.commons.fetch;

import io.authid.core.shared.rest.contracts.hooks.commons.FetchAllHooks;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Map;

public record FetchAllQuery<T>(
    String searchTerm,
    Map<String, Object> filters,
    Pageable pageable,
    String cursor,
    JpaSpecificationExecutor<T> repository,
    FetchAllHooks<T> hooks
) {
}
