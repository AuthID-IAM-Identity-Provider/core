package io.authid.core.containers.user.repositories;

import io.authid.core.containers.user.entities.PasswordResetTokenEntity;
import io.authid.core.containers.user.entities.UserEntity;
import io.authid.core.shared.components.database.repository.BaseRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
@Qualifier("userRepository")
public interface UserRepository extends BaseRepository<UserEntity, UUID> {
}

