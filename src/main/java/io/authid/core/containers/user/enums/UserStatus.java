package io.authid.core.containers.user.enums;

import io.authid.core.shared.components.i18n.I18n;

public enum UserStatus {
    // Active states
    ACTIVE(I18n.extract("user.status.active.label"), I18n.extract("user.status.active.description"), "green", true, true, false),
    INCOMPLETE_PROFILE(I18n.extract("user.status.incomplete.profile.label"), I18n.extract("user.status.incomplete.profile.description"), "orange", true, false, false),
    PENDING_VERIFICATION(I18n.extract("user.status.pending.verification.label"), I18n.extract("user.status.pending.verification.description"), "yellow", false, false, false),
    INACTIVE(I18n.extract("user.status.inactive.label"), I18n.extract("user.status.inactive.description"), "gray", false, false, false),

    // System-related states
    LOCKED(I18n.extract("user.status.locked.label"), I18n.extract("user.status.locked.description"), "red", false, false, false),
    DORMANT(I18n.extract("user.status.dormant.label"), I18n.extract("user.status.dormant.description"), "gray", false, false, false),
    DEACTIVATED(I18n.extract("user.status.deactivated.label"), I18n.extract("user.status.deactivated.description"), "gray", false, false, false),

    // Admin/system actions
    SUSPENDED(I18n.extract("user.status.suspended.label"), I18n.extract("user.status.suspended.description"), "red", false, false, false),
    BANNED(I18n.extract("user.status.banned.label"), I18n.extract("user.status.banned.description"), "red", false, false, true),
    DELETED(I18n.extract("user.status.deleted.label"), I18n.extract("user.status.deleted.description"), "black", false, false, true);

    private final boolean canLogin;
    private final boolean canPerformActions;

    UserStatus(String label, String description, String color,
               boolean canLogin, boolean canPerformActions, boolean isDeleted) {
        this.canLogin = canLogin;
        this.canPerformActions = canPerformActions;
    }

    public boolean canLogin() { return canLogin; }
    public boolean canPerformActions() { return canPerformActions; }

    // Basic checks
    public boolean isActive() {
        return this == ACTIVE;
    }

    public boolean isInactive() {
        return this == INACTIVE || this == DEACTIVATED || this == DORMANT;
    }

    public boolean isRestricted() {
        return this != LOCKED && this != SUSPENDED && this != BANNED;
    }

    public boolean isSoftDeleted() {
        return this == DEACTIVATED || this == INACTIVE || this == DORMANT;
    }

    public boolean isPermanentRestriction() {
        return this == BANNED || this == DELETED;
    }

    public boolean isSystemRestricted() {
        return this == LOCKED || this == DORMANT;
    }

    public boolean isUserInitiatedInactive() {
        return this == INACTIVE || this == DEACTIVATED;
    }

    public boolean isLoginAllowed() {
        return canLogin && isRestricted();
    }

    public boolean isAccountUsable() {
        return canLogin && canPerformActions && isRestricted();
    }

    public boolean isRecoverable() {
        return isSoftDeleted() && !isPermanentRestriction();
    }

    // Default fallback
    public static UserStatus getDefault() {
        return INCOMPLETE_PROFILE;
    }

    // Valid transitions
    public boolean canTransitionTo(UserStatus newStatus) {
        return switch (this) {
            case INCOMPLETE_PROFILE ->
                    newStatus == ACTIVE || newStatus == PENDING_VERIFICATION;
            case PENDING_VERIFICATION ->
                    newStatus == ACTIVE || newStatus == SUSPENDED || newStatus == DELETED;
            case ACTIVE ->
                    newStatus == INCOMPLETE_PROFILE || newStatus == SUSPENDED || newStatus == LOCKED ||
                            newStatus == DORMANT || newStatus == DEACTIVATED || newStatus == INACTIVE || newStatus == BANNED || newStatus == DELETED;
            case LOCKED, DORMANT, INACTIVE, DEACTIVATED ->
                    newStatus == ACTIVE || newStatus == DELETED;
            case SUSPENDED ->
                    newStatus == ACTIVE || newStatus == DELETED || newStatus == BANNED;
            case BANNED ->
                    newStatus == DELETED;
            case DELETED ->
                    false; // terminal state
        };
    }
}
