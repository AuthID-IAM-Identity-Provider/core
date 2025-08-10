package io.authid.core.shared.components.jobs.entites;

import io.authid.core.shared.components.database.model.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "batch_data_holder")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BatchDataHolderEntity extends BaseEntity<UUID> {
    @Column(nullable = false, updatable = false)
    private String batchId;

    @Column(nullable = false, updatable = false)
    private String entityType;

    @Lob // Large Object
    @Column(nullable = false, updatable = false, columnDefinition = "CLOB") // CLOB untuk teks panjang
    private String payload;
}
