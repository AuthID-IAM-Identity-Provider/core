package io.authid.core.shared.rest.contracts;

public interface RestQueryHandler<R, C> {
    R handle(C query);
}
