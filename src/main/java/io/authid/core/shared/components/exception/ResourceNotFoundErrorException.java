package io.authid.core.shared.components.exception;

import java.util.UUID;

import io.authid.core.shared.components.exception.contracts.ErrorCatalog;
import io.authid.core.shared.enums.SystemErrorCatalog;

public class ResourceNotFoundErrorException extends BaseApplicationException {
  public ResourceNotFoundErrorException(UUID uuid, Object ...args) {
    super(uuid, args);
  }

  @Override
  public ErrorCatalog getErrorCatalog() {
    return SystemErrorCatalog.RESOURCE_NOT_FOUND;
  }
}
