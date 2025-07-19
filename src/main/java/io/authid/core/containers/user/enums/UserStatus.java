package io.authid.core.containers.user.enums;

import lombok.Getter;

@Getter
public enum UserStatus {
    PENDING_VERIFICATION("Menunggu Verifikasi", "Pengguna belum verifikasi email/telepon.", "orange", false, false, true),
    ACTIVE("Aktif", "Pengguna aktif dan dapat menggunakan aplikasi.", "green", true, true, false),
    INCOMPLETE_PROFILE("Profil Belum Lengkap", "Pengguna perlu melengkapi profil.", "blue", true, true, false),
    SUSPENDED("Ditangguhkan", "Akun dibekukan sementara oleh admin.", "yellow", true, false, false),
    LOCKED("Terkunci", "Akun terkunci karena gagal login berulang kali.", "gray", false, false, false),
    DORMANT("Dorman", "Akun tidak aktif dalam waktu lama.", "gray", false, false, false),
    DEACTIVATED("Dinonaktifkan", "Akun dinonaktifkan oleh pengguna.", "gray", false, false, false),
    BANNED("Diblokir", "Akun diblokir permanen oleh admin.", "red", false, false, false),
    DELETED("Dihapus", "Akun telah dihapus.", "darkred", false, false, false),
    INVITED("Diundang", "Undangan telah dikirim tapi pengguna belum mendaftar.", "lightblue", false, false, false);

    private final String label;
    private final String description;
    private final String color;
    private final boolean canLogin;
    private final boolean canPerformActions;
    private final boolean isDefault;

    UserStatus(String label, String description, String color, boolean canLogin, boolean canPerformActions, boolean isDefault) {
        this.label = label;
        this.description = description;
        this.color = color;
        this.canLogin = canLogin;
        this.canPerformActions = canPerformActions;
        this.isDefault = isDefault;
    }


    public static UserStatus getDefault() {
        for (UserStatus status : values()) {
            if (status.isDefault) {
                return status;
            }
        }
        // Fallback - should never happen if enum is properly configured
        return PENDING_VERIFICATION;
    }

    /**
     * Gets all valid transition targets from current status
     * @return array of valid target statuses
     */
    public UserStatus[] getValidTransitions() {
        return java.util.Arrays.stream(values())
                .filter(this::canTransitionTo)
                .toArray(UserStatus[]::new);
    }

    /**
     * Checks if transition to another status is allowed
     * @param newStatus the target status
     * @return true if transition is valid
     */
    public boolean canTransitionTo(UserStatus newStatus) {
        if (this == newStatus) {
            return false; // No self-transition
        }

        return switch (this) {
            case INVITED -> newStatus == PENDING_VERIFICATION || newStatus == DELETED;
            case PENDING_VERIFICATION, DORMANT, SUSPENDED -> newStatus == ACTIVE || newStatus == INCOMPLETE_PROFILE ||
                    newStatus == BANNED || newStatus == DELETED;
            case ACTIVE -> newStatus == INCOMPLETE_PROFILE || newStatus == SUSPENDED ||
                    newStatus == LOCKED || newStatus == DORMANT || newStatus == DEACTIVATED ||
                    newStatus == BANNED || newStatus == DELETED;
            case INCOMPLETE_PROFILE -> newStatus == ACTIVE || newStatus == SUSPENDED || newStatus == LOCKED ||
                    newStatus == DORMANT || newStatus == DEACTIVATED || newStatus == BANNED ||
                    newStatus == DELETED;
            case LOCKED -> newStatus == ACTIVE || newStatus == INCOMPLETE_PROFILE ||
                    newStatus == SUSPENDED || newStatus == BANNED || newStatus == DELETED;
            case DEACTIVATED -> newStatus == ACTIVE || newStatus == INCOMPLETE_PROFILE ||
                    newStatus == DELETED;
            case BANNED -> newStatus == DELETED; // Only admin can unban, but usually permanent

            case DELETED -> false; // No transitions from deleted state

            default -> false;
        };
    }

    /**
     * Checks if user is in an active state (can use the application normally)
     * @return true if user is active
     */
    public boolean isActive() {
        return this == ACTIVE || this == INCOMPLETE_PROFILE;
    }

    /**
     * Checks if user account is restricted by admin or system
     * @return true if account is restricted
     */
    public boolean isRestricted() {
        return this == SUSPENDED || this == LOCKED || this == BANNED;
    }

    /**
     * Checks if user account is inactive (user-initiated or system-initiated)
     * @return true if account is inactive
     */
    public boolean isInactive() {
        return this == DORMANT || this == DEACTIVATED || this == DELETED;
    }

    /**
     * Checks if user needs to complete some verification or setup
     * @return true if user needs to complete verification/setup
     */
    public boolean requiresVerification() {
        return this == PENDING_VERIFICATION || this == INVITED || this == INCOMPLETE_PROFILE;
    }

    /**
     * Checks if status represents a temporary restriction
     * @return true if restriction is temporary
     */
    public boolean isTemporaryRestriction() {
        return this == SUSPENDED || this == LOCKED || this == DORMANT;
    }

    /**
     * Checks if status represents a permanent restriction
     * @return true if restriction is permanent
     */
    public boolean isPermanentRestriction() {
        return this == BANNED || this == DELETED;
    }

    /**
     * Checks if user can be contacted (not deleted)
     * @return true if user can receive communications
     */
    public boolean canBeContacted() {
        return this != DELETED;
    }

    /**
     * Checks if account requires admin intervention to change status
     * @return true if admin intervention needed
     */
    public boolean requiresAdminIntervention() {
        return this == SUSPENDED || this == BANNED;
    }

    /**
     * Checks if user can self-reactivate their account
     * @return true if user can reactivate themselves
     */
    public boolean canSelfReactivate() {
        return this == DEACTIVATED || this == DORMANT;
    }
}