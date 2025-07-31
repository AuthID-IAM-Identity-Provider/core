package io.authid.core.shared.components.database.contracts; // Sesuaikan package

import java.time.Instant; // Atau LocalDateTime, sesuai kebutuhan Anda

public interface SoftDeletable {
    boolean isDeleted();
    void setDeleted(boolean deleted);
    Instant getDeletedAt();
    void setDeletedAt(Instant deletedAt);
    String getDeletedBy();
    void setDeletedBy(String deletedBy);

    // Default methods for convenience (dapat ditimpa di BaseEntity)
    default void markAsDeleted() {
        setDeleted(true);
        setDeletedAt(Instant.now());
    }

    default void markAsUndeleted() {
        setDeleted(false);
        setDeletedAt(null);
        setDeletedBy(null);
    }
}