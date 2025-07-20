package io.authid.core.containers.user.repositories;

import io.authid.core.containers.user.entities.PasswordResetTokenEntity;
import io.authid.core.shared.components.database.repository.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PasswordResetTokenRepository extends BaseRepository<PasswordResetTokenEntity, UUID> {
    PasswordResetTokenEntity findByToken(String token);
    boolean existsByToken(String token);
    boolean existsByTokenAndUserId(String token, UUID userId);
    void deleteByToken(String token);
    void deleteByUserId(UUID userId);
    void deleteByTokenAndUserId(String token, UUID userId);
}
