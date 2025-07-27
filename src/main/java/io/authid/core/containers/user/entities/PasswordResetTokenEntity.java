package io.authid.core.containers.user.entities;

import io.authid.core.containers.user.enums.PasswordResetTokenStatus;
import io.authid.core.shared.components.database.converters.InstantTimestampConverter;
import io.authid.core.shared.components.database.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "password_reset_tokens", indexes = {
        @Index(name = "idx_expires_at", columnList = "expires_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetTokenEntity extends BaseEntity<UUID> {
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "token_hash", nullable = false, unique = true)
    private String tokenHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PasswordResetTokenStatus status = PasswordResetTokenStatus.PENDING;

    @Column(name = "expires_at", nullable = false)
    @Convert(converter = InstantTimestampConverter.class)
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
    @Convert(converter = InstantTimestampConverter.class)
    private Instant completedAt;

    // Security fields
    @Column(name = "attempt_count", nullable = false)
    private Integer attemptCount = 0;

    @Column(name = "max_attempts", nullable = false)
    private Integer maxAttempts = 3;

    @Column(name = "blocked_until")
    @Convert(converter = InstantTimestampConverter.class)
    private Instant blockedUntil;

    @Column(name = "hash_algorithm", length = 20)
    private String hashAlgorithm = "SHA256";

    @Column(name = "hash_iterations", nullable = false)
    private Integer hashIterations;

    @Column(name = "hash_salt", columnDefinition = "TEXT")
    private String hashSalt;

    @Column(name = "hash_key", columnDefinition = "TEXT")
    private String hashKey;

    @Column(name = "hash_iv", columnDefinition = "TEXT")
    private String hashIv;

    @Column(name = "hash_tag", columnDefinition = "TEXT")
    private String hashTag;

    // ----------------------------
    // Business Logic - Token Status
    // ----------------------------

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt) || attemptCount >= maxAttempts;
    }

    public boolean isUsable() {
        return status.isUsable() && !isExpired();
    }

    public boolean isBlockedNow() {
        return blockedUntil != null && Instant.now().isBefore(blockedUntil);
    }

    public void cancel() {
        if (!status.isFinal()) {
            this.status = PasswordResetTokenStatus.CANCELLED;
        }
    }

    // ----------------------------
    // Business Logic - Completion
    // ----------------------------

    public void complete(String ipAddress, String userAgent) {
        if (!isUsable()) {
            throw new IllegalStateException("Cannot complete an unusable token");
        }
        this.status = PasswordResetTokenStatus.COMPLETED;
        this.ipAddressCompletion = ipAddress;
        this.userAgentCompletion = userAgent;
        this.completedAt = Instant.now();
    }

    // ----------------------------
    // Business Logic - Attempts
    // ----------------------------

    public void incrementAttempt() {
        this.attemptCount++;
    }

    public void registerFailedAttempt(int blockMinutes) {
        incrementAttempt();
        if (attemptCount >= maxAttempts) {
            this.status = PasswordResetTokenStatus.EXPIRED;
        } else {
            blockTemporarily(blockMinutes);
        }
    }

    // ----------------------------
    // Business Logic - Blocking
    // ----------------------------

    public void blockTemporarily(int minutes) {
        this.blockedUntil = Instant.now().plusSeconds(minutes * 60L);
    }

    // ----------------------------
    // Business Logic - Expiration
    // ----------------------------
    public void expire() {
        this.status = PasswordResetTokenStatus.EXPIRED;
    }
}
