package io.authid.core.containers.user.enums;

public enum UserStatus {
    // Active states
    ACTIVE("Aktif", "Akun aktif dan dapat digunakan", "green", true, true, false),
    INCOMPLETE_PROFILE("Belum Lengkap", "Profil belum lengkap", "orange", true, false, false),
    PENDING_VERIFICATION("Menunggu Verifikasi", "Akun belum diverifikasi", "yellow", false, false, false),
    INACTIVE("Tidak Aktif", "Akun dinonaktifkan oleh pengguna", "gray", false, false, false),

    // System-related states
    LOCKED("Terkunci", "Akun terkunci karena keamanan", "red", false, false, false),
    DORMANT("Dormant", "Akun tidak aktif dalam waktu lama", "gray", false, false, false),
    DEACTIVATED("Dinonaktifkan", "Akun dinonaktifkan oleh pengguna", "gray", false, false, false),

    // Admin/system actions
    SUSPENDED("Ditangguhkan", "Akun ditangguhkan oleh sistem", "red", false, false, false),
    BANNED("Diblokir", "Akun diblokir permanen", "red", false, false, true),
    DELETED("Dihapus", "Akun dihapus secara permanen", "black", false, false, true);

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
