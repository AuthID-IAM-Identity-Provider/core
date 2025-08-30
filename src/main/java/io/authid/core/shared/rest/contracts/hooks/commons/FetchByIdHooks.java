package io.authid.core.shared.rest.contracts.hooks.commons;

import io.authid.core.shared.components.exception.BussinessApplicationException;

public interface FetchByIdHooks<T, ID> {
    void beforeFindById(ID id);

    void onFindingById(ID id);

    void afterFindById(T entity);

    BussinessApplicationException onNotFound(ID id);
}