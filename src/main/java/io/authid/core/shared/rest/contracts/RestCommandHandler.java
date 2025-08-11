package io.authid.core.shared.rest.contracts;

public interface RestCommandHandler<R, C> {
    R handle(C command);
}
