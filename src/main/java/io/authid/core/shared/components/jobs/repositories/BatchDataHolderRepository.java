package io.authid.core.shared.components.jobs.repositories;

import io.authid.core.shared.components.database.repository.BaseRepository;
import io.authid.core.shared.components.jobs.entites.BatchDataHolderEntity;

import java.util.List;
import java.util.UUID;

public interface BatchDataHolderRepository extends BaseRepository<BatchDataHolderEntity, UUID> {
    List<BatchDataHolderEntity> findByBatchId(String batchId);
}
