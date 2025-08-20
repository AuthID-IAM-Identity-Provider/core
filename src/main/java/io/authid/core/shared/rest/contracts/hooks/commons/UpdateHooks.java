package io.authid.core.shared.rest.contracts.hooks.commons;

public interface UpdateHooks<T, C> {
    void beforeUpdate(C request);

    T onUpdating(C request);

    void afterUpdate(T entity);
}
