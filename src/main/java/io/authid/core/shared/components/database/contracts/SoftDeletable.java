package io.authid.core.shared.components.database.contracts;

import java.time.Instant;

public interface SoftDeletable {
    void setDeletedAt(Instant deletedAt);

    Instant getDeletedAt();

    default boolean isDeleted() {
        return getDeletedAt() != null;
    }
}