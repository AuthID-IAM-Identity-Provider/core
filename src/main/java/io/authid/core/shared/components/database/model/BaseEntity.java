package io.authid.core.shared.components.database.model;

import io.authid.core.shared.components.database.contracts.SoftDeletable;
import io.authid.core.shared.components.database.converters.InstantTimestampConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.Instant;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity<T extends Serializable> implements SoftDeletable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(updatable = false, nullable = false)
    @Getter
    private T id;

    @Getter
    @Setter
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    @Convert(converter = InstantTimestampConverter.class)
    private Instant createdAt;

    @Getter
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    @Convert(converter = InstantTimestampConverter.class)
    private Instant updatedAt;

    @CreatedBy
    @Column(name = "created_by")
    @Getter
    @Setter
    private String createdBy;

    @LastModifiedBy
    @Column(name = "updated_by") // atau last_modified_by
    @Getter
    @Setter
    private String updatedBy;

    @Getter
    @Setter
    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false; // Default: tidak terhapus

    @Getter
    @Setter
    @Column(name = "deleted_at") // Bisa nullable, karena hanya terisi saat deleted=true
    @Convert(converter = InstantTimestampConverter.class)
    private Instant deletedAt;

    @Getter
    @Setter
    @LastModifiedBy // Akan diisi otomatis oleh AuditorAware saat record di-soft delete atau di-undelete
    @Column(name = "deleted_by") // Siapa yang melakukan soft delete/undelete
    private String deletedBy;

    @Override
    public void markAsDeleted() {
        setDeleted(true);
        setDeletedAt(Instant.now());
        // setDeletedBy() akan otomatis diisi oleh @LastModifiedBy karena markAsDeleted memodifikasi entity
    }

    @Override
    public void markAsUndeleted() {
        setDeleted(false);
        setDeletedAt(null);
        setDeletedBy(null); // Bersihkan siapa yang menghapus sebelumnya
        // updatedBy akan otomatis diisi oleh @LastModifiedBy karena markAsUndeleted memodifikasi entity
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseEntity<?> that = (BaseEntity<?>) o;
        return java.util.Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(id);
    }
}
