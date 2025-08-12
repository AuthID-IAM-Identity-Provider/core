package io.authid.core.shared.rest.contracts.hooks.commons;

public interface DeleteByIdHooks<T, ID> {
    void beforeDelete(ID id);

    void onDeletingById(ID id);

    void afterDelete(ID id);
}
