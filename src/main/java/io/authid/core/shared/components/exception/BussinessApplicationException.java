package io.authid.core.shared.components.exception;

import io.authid.core.shared.components.exception.contracts.ErrorCatalog;
import lombok.Getter;

@Getter
public abstract class BussinessApplicationException extends RuntimeException {
    private final Object[] args;
    public BussinessApplicationException(Object... args) {
        super();
        this.args = args;
    }
    public abstract ErrorCatalog getErrorCatalog();
}
