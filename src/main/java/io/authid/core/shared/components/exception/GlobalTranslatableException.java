package io.authid.core.shared.components.exception;

import lombok.Getter;

@Getter
public class GlobalTranslatableException extends RuntimeException {

    private final Object[] args;

    public GlobalTranslatableException(String messageKey, Object[] args) {
        super(messageKey);
        this.args = args;
    }
}
