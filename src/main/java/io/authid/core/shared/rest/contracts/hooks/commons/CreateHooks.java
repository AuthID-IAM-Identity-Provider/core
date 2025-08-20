package io.authid.core.shared.rest.contracts.hooks.commons;

public interface CreateHooks<T, C> {
    void beforeCreate(C request);

    T onCreating(C request);

    void afterCreate(T entity);
}
