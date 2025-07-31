package io.authid.core.shared.components.database.repository;

import io.authid.core.shared.components.database.model.BaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@NoRepositoryBean
public interface BaseRepository<E extends BaseEntity<T>, T extends Serializable> extends JpaRepository<E, T>, JpaSpecificationExecutor<E> {
}