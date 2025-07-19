package io.authid.core.containers.user.entities;

import io.authid.core.containers.user.enums.PasswordResetTokenStatus;
import io.authid.core.shared.components.database.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "password_reset_tokens", indexes = {
        @Index(name = "idx_user_id_status", columnList = "userId, status"),
        @Index(name = "idx_token_hash", columnList = "tokenHash"),
        @Index(name = "idx_expires_at", columnList = "expiresAt"),
        @Index(name = "idx_status_expires", columnList = "status, expiresAt")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetTokenEntity extends BaseEntity<UUID> {
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "token_hash", nullable = false, unique = true)
    private String tokenHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private PasswordResetTokenStatus status = PasswordResetTokenStatus.PENDING;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    // Request tracking
    @Column(name = "ip_address_request", length = 45)
    private String ipAddressRequest;

    @Column(name = "user_agent_request", columnDefinition = "TEXT")
    private String userAgentRequest;

    // Completion tracking
    @Column(name = "ip_address_completion", length = 45)
    private String ipAddressCompletion;

    @Column(name = "user_agent_completion", columnDefinition = "TEXT")
    private String userAgentCompletion;

    @Column(name = "completed_at")
    private Instant completedAt;

    // Security fields
    @Column(name = "attempt_count", nullable = false)
    @Builder.Default
    private Integer attemptCount = 0;

    @Column(name = "max_attempts", nullable = false)
    @Builder.Default
    private Integer maxAttempts = 3;

    @Column(name = "blocked_until")
    private Instant blockedUntil;

    @Column(name = "hash_algorithm", length = 20)
    @Builder.Default
    private String hashAlgorithm = "SHA256";
}
