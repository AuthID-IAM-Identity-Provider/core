package io.authid.core.containers.user.exceptions;

import io.authid.core.containers.user.enums.UserErrorCatalog;
import io.authid.core.shared.components.exception.BaseApplicationException;
import io.authid.core.shared.components.exception.contracts.ErrorCatalog;

public class UserNotFoundException extends BaseApplicationException {
    public UserNotFoundException(Object identifier) {
        super(identifier);
    }

    @Override
    public ErrorCatalog getErrorCatalog() {
        return UserErrorCatalog.USER_NOT_FOUND;
    }
}