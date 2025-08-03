package io.authid.core.shared.components.exception;

import io.authid.core.shared.components.exception.contracts.ErrorCatalog;
import lombok.Getter;

@Getter
public abstract class BaseApplicationException extends RuntimeException {
    private final Object[] args;
    public BaseApplicationException(Object... args) {
        super();
        this.args = args;
    }
    public abstract ErrorCatalog getErrorCatalog();
}
