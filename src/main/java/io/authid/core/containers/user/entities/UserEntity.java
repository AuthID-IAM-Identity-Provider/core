package io.authid.core.containers.user.entities;

import io.authid.core.containers.user.enums.UserStatus;
import io.authid.core.shared.components.database.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_email", columnList = "email"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_email", columnList = "email"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_email_status", columnList = "email, status"),
        @Index(name = "idx_email_verified", columnList = "email_verified_at"),
        @Index(name = "idx_last_login", columnList = "last_login_at"),
        @Index(name = "idx_last_activity", columnList = "last_activity_at"),
        @Index(name = "idx_password_changed", columnList = "password_changed_at"),
        @Index(name = "idx_failed_attempts", columnList = "failed_login_attempts"),
        @Index(name = "idx_locked_until", columnList = "locked_until"),
        @Index(name = "idx_two_factor", columnList = "two_factor_confirmed_at"),

        @Index(name = "idx_status_login", columnList = "status"),
        @Index(name = "idx_status_actions", columnList = "status"),
        @Index(name = "idx_security_check", columnList = "failed_login_attempts, locked_until"),
        @Index(name = "idx_activity_tracking", columnList = "last_login_at, last_activity_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity extends BaseEntity<UUID> {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "email_verified_at")
    private Instant emailVerifiedAt;

    @Column(name = "password", nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private UserStatus status = UserStatus.getDefault();

    @Column(name = "remember_token", length = 100)
    private String rememberToken;

    @Column(name = "two_factor_secret", length = 255)
    private String twoFactorSecret;

    @Column(name = "two_factor_recovery_codes", columnDefinition = "json")
    private String twoFactorRecoveryCodes;

    @Column(name = "two_factor_confirmed_at")
    private Instant twoFactorConfirmedAt;

    @Column(name = "password_changed_at")
    private Instant passwordChangedAt;

    @Column(name = "failed_login_attempts")
    private Integer failedLoginAttempts = 0;

    @Column(name = "locked_until")
    private Instant lockedUntil;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @Column(name = "last_login_ip", length = 45)
    private String lastLoginIp;

    @Column(name = "last_activity_at")
    private Instant lastActivityAt;

    @Column(name = "login_count")
    private Integer loginCount = 0;

    public boolean isAccountLocked() {
        return lockedUntil != null && lockedUntil.isAfter(Instant.now());
    }

    public boolean hasConfirmedTwoFactor() {
        return twoFactorConfirmedAt != null;
    }

    public void incrementFailedAttempts() {
        this.failedLoginAttempts = (this.failedLoginAttempts == null ? 1 : this.failedLoginAttempts + 1);
    }

    public void lockAccount(Instant lockUntil) {
        this.lockedUntil = lockUntil;
    }

    public void resetFailedAttempts() {
        this.failedLoginAttempts = 0;
    }

    public void unlockAccount() {
        this.lockedUntil = null;
    }

    public void confirmTwoFactor() {
        this.twoFactorConfirmedAt = Instant.now();
    }

    public void recordLogin(String ipAddress) {
        this.lastLoginAt = Instant.now();
        this.lastLoginIp = ipAddress;
        this.loginCount = (this.loginCount == null ? 1 : this.loginCount + 1);
    }

    public void recordActivity() {
        this.lastActivityAt = Instant.now();
    }

    public boolean isActive() {
        return this.status == UserStatus.ACTIVE;
    }

    public boolean isInactive() {
        return this.status == UserStatus.INACTIVE;
    }

    public boolean isPendingVerification() {
        return this.status == UserStatus.PENDING_VERIFICATION;
    }

    public boolean isIncompleteProfile() {
        return this.status == UserStatus.INCOMPLETE_PROFILE;
    }

    public boolean isSuspended() {
        return this.status == UserStatus.SUSPENDED;
    }

    public boolean isLocked() {
        return this.status == UserStatus.LOCKED;
    }

    public boolean isDormant() {
        return this.status == UserStatus.DORMANT;
    }

    public boolean isDeleted() {
        return this.status == UserStatus.DELETED;
    }

    public boolean isBanned() {
        return this.status == UserStatus.BANNED;
    }

    public boolean isDeactivated() {
        return this.status == UserStatus.DEACTIVATED;
    }

    public boolean isVerified() {
        return this.emailVerifiedAt != null;
    }

    public boolean isUnverified() {
        return !isVerified();
    }

    public boolean isPasswordChanged() {
        return passwordChangedAt != null;
    }

    public boolean isPasswordNotChanged() {
        return !isPasswordChanged();
    }

    public boolean canLogin() {
        return isActive() || isIncompleteProfile();
    }

    public boolean cannotLogin() {
        return !canLogin();
    }

    public boolean canPerformActions() {
        return isActive();
    }

    public boolean cannotPerformActions() {
        return !canPerformActions();
    }

    public boolean hasTwoFactor() {
        return hasConfirmedTwoFactor();
    }

    public boolean hasNoTwoFactor() {
        return !hasTwoFactor();
    }

    public void activate() {
        this.status = UserStatus.ACTIVE;
    }

    public void deactivate() {
        this.status = UserStatus.INACTIVE;
    }
}