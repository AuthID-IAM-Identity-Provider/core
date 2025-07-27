package io.authid.core.containers.user.entities;

import io.authid.core.containers.user.enums.UserStatus;
import io.authid.core.shared.components.database.converters.InstantTimestampConverter;
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
    @Convert(converter = InstantTimestampConverter.class)
    private Instant emailVerifiedAt;

    @Column(name = "usr_pwd", nullable = false)
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
    @Convert(converter = InstantTimestampConverter.class)
    private Instant twoFactorConfirmedAt;

    @Column(name = "password_changed_at")
    @Convert(converter = InstantTimestampConverter.class)
    private Instant passwordChangedAt;

    @Column(name = "failed_login_attempts")
    private Integer failedLoginAttempts = 0;

    @Column(name = "locked_until")
    @Convert(converter = InstantTimestampConverter.class)
    private Instant lockedUntil;

    @Column(name = "last_login_at")
    @Convert(converter = InstantTimestampConverter.class)
    private Instant lastLoginAt;

    @Column(name = "last_login_ip", length = 45)
    private String lastLoginIp;

    @Column(name = "last_activity_at")
    @Convert(converter = InstantTimestampConverter.class)
    private Instant lastActivityAt;

    @Column(name = "login_count")
    private Integer loginCount = 0;

    // 1. ACCOUNT LIFECYCLE
    public boolean isNewUser() {
        return this.getCreatedAt() != null && Instant.now().minusSeconds(86400).isBefore(this.getCreatedAt());
    }
    public boolean isOldUser() {
        return !isNewUser();
    }

    // 2. STATUS CHECKS
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

    // 3. EMAIL HANDLING
    public boolean isVerified() {
        return this.emailVerifiedAt != null;
    }
    public boolean isUnverified() {
        return !isVerified();
    }
    public void markVerifiedEmail() {
        this.emailVerifiedAt = Instant.now();
    }
    public void markUnverifiedEmail() {
        this.emailVerifiedAt = null;
    }

    // 4. PASSWORD HANDLING
    public boolean isPasswordChanged() {
        return passwordChangedAt != null;
    }
    public boolean isPasswordNotChanged() {
        return !isPasswordChanged();
    }
    public void markPasswordChanged() {
        this.passwordChangedAt = Instant.now();
    }
    public void markPasswordNotChanged() {
        this.passwordChangedAt = null;
    }
    public void changePassword(String newPassword) {
        this.password = newPassword;
        this.passwordChangedAt = Instant.now();
    }

    // 5. TWO-FACTOR AUTHENTICATION
    public boolean hasTwoFactor() {
        return hasConfirmedTwoFactor();
    }
    public boolean hasNoTwoFactor() {
        return !hasTwoFactor();
    }
    public boolean hasConfirmedTwoFactor() {
        return twoFactorConfirmedAt != null;
    }
    public void confirmTwoFactor() {
        this.twoFactorConfirmedAt = Instant.now();
    }
    public void markTwoFactorConfirmed() {
        this.twoFactorConfirmedAt = Instant.now();
    }
    public void markTwoFactorNotConfirmed() {
        this.twoFactorConfirmedAt = null;
    }
    public boolean hasTwoFactorEnabled() {
        return this.twoFactorSecret != null && !this.twoFactorSecret.isEmpty();
    }
    public boolean hasTwoFactorDisabled() {
        return !hasTwoFactorEnabled();
    }
    public void markTwoFactorEnabled() {
        // Usually would generate and set secret
        this.twoFactorSecret = null;
    }
    public void markTwoFactorDisabled() {
        this.twoFactorSecret = null;
    }

    // 6. LOGIN & ACTIVITY TRACKING
    public void recordLogin(String ipAddress) {
        this.lastLoginAt = Instant.now();
        this.lastLoginIp = ipAddress;
        this.loginCount = (this.loginCount == null ? 1 : this.loginCount + 1);
    }
    public void recordActivity() {
        this.lastActivityAt = Instant.now();
    }

    // 7. LOCKING & SECURITY
    public boolean isAccountLocked() {
        return lockedUntil != null && lockedUntil.isAfter(Instant.now());
    }
    public void lockAccount(Instant lockUntil) {
        this.lockedUntil = lockUntil;
    }
    public void unlockAccount() {
        this.lockedUntil = null;
    }
    public void incrementFailedAttempts() {
        this.failedLoginAttempts = (this.failedLoginAttempts == null ? 1 : this.failedLoginAttempts + 1);
    }
    public void resetFailedAttempts() {
        this.failedLoginAttempts = 0;
    }
    public void markAccountLocked(Instant lockUntil) {
        this.lockedUntil = lockUntil;
    }
    public void markAccountUnlocked() {
        this.lockedUntil = null;
    }

    // 8. FIELD PRESENCE CHECKS
    public boolean hasEmail() {
        return this.email != null && !this.email.isEmpty();
    }
    public boolean hasNoEmail() {
        return !hasEmail();
    }
    public boolean hasName() {
        return this.name != null && !this.name.isEmpty();
    }
    public boolean hasNoName() {
        return !hasName();
    }
    public boolean hasPassword() {
        return this.password != null && !this.password.isEmpty();
    }
    public boolean hasNoPassword() {
        return !hasPassword();
    }
    public boolean hasStatus() {
        return this.status != null;
    }
    public boolean hasNoStatus() {
        return !hasStatus();
    }
    public boolean hasRememberToken() {
        return this.rememberToken != null && !this.rememberToken.isEmpty();
    }
    public boolean hasNoRememberToken() {
        return !hasRememberToken();
    }

    // 9. BOOLEAN STATE ACCESSORS
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

    // 10. MUTATORS / ACTIONS
    public void activate() {
        this.status = UserStatus.ACTIVE;
    }
    public void deactivate() {
        this.status = UserStatus.INACTIVE;
    }
    public void markAccountActive() {
        this.status = UserStatus.ACTIVE;
    }
    public void markAccountInactive() {
        this.status = UserStatus.INACTIVE;
    }
    public void markAccountPendingVerification() {
        this.status = UserStatus.PENDING_VERIFICATION;
    }
    public void markAccountIncompleteProfile() {
        this.status = UserStatus.INCOMPLETE_PROFILE;
    }
    public void markAccountDeactivated() {
        this.status = UserStatus.DEACTIVATED;
    }
    public void markAccountSuspended() {
        this.status = UserStatus.SUSPENDED;
    }
    public void markAccountBanned() {
        this.status = UserStatus.BANNED;
    }
    public void markAccountDeleted() {
        this.status = UserStatus.DELETED;
    }
    public void markAccountDeletedPermanently() {
        this.status = UserStatus.DELETED;
    }
    public void markAccountDormant() {
        this.status = UserStatus.DORMANT;
    }
    public void markAccountLocked() {
        this.status = UserStatus.LOCKED;
    }
}