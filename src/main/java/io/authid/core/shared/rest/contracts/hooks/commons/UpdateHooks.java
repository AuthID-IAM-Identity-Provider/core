package io.authid.core.shared.rest.contracts.hooks.commons;

public interface UpdateHooks<T, ID, U> {
    void beforeUpdate(U request);

    T onUpdating(T entity, U updateRequest);

    void afterUpdate(T entity);
}
