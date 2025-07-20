package io.authid.core.containers.user.repositories;

import io.authid.core.containers.user.entities.PasswordResetTokenEntity;
import io.authid.core.shared.components.database.repository.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PasswordResetTokenRepository extends BaseRepository<PasswordResetTokenEntity, UUID> {
}
