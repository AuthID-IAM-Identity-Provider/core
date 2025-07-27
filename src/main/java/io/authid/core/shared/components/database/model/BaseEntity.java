package io.authid.core.shared.components.database.model;

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
public abstract class BaseEntity<T extends Serializable> {

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
    @Column(name = "created_by", updatable = false)
    @Getter
    @Setter
    private String createdBy;

    @LastModifiedBy
    @Column(name = "updated_by") // atau last_modified_by
    @Getter
    @Setter
    private String updatedBy;

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
