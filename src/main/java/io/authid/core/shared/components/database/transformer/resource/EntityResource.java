package io.authid.core.shared.components.database.transformer.resource;

import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Setter
@Getter
public abstract class EntityResource<T> {
    private T id;
    private Instant createdAt;
    private Instant updatedAt;
}