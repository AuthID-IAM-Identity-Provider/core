package io.authid.core.containers.user.enums;

import lombok.Getter;

@Getter
public enum PasswordResetTokenStatus {
    PENDING("Menunggu", "Token sedang menunggu digunakan", "blue", true, false),
    COMPLETED("Selesai", "Token telah digunakan dengan sukses", "green", false, true),
    EXPIRED("Kedaluwarsa", "Token telah kedaluwarsa atau melebihi batas percobaan", "red", false, false),
    CANCELLED("Dibatalkan", "Token dibatalkan oleh sistem atau pengguna", "gray", false, false);

    private final String color;
    private final boolean isUsable;
    private final boolean isCompleted;

    PasswordResetTokenStatus(String label, String description, String color, boolean isUsable, boolean isCompleted) {
        this.color = color;
        this.isUsable = isUsable;
        this.isCompleted = isCompleted;
    }

    /**
     * Returns the default status for new tokens
     */
    public static PasswordResetTokenStatus getDefault() {
        return PENDING;
    }

    /**
     * Checks if transition to another status is allowed
     */
    public boolean canTransitionTo(PasswordResetTokenStatus newStatus) {
        if (this == newStatus) {
            return false; // No self-transition
        }

        return switch (this) {
            case PENDING -> newStatus == COMPLETED || newStatus == EXPIRED || newStatus == CANCELLED;
            case COMPLETED, EXPIRED, CANCELLED -> false; // Terminal states

            default -> false;
        };
    }

    /**
     * Gets all valid transition targets from current status
     */
    public PasswordResetTokenStatus[] getValidTransitions() {
        return java.util.Arrays.stream(values())
                .filter(this::canTransitionTo)
                .toArray(PasswordResetTokenStatus[]::new);
    }

    /**
     * Checks if this status represents an active/pending token
     */
    public boolean isActive() {
        return this == PENDING;
    }

    /**
     * Checks if this status represents a final state
     */
    public boolean isFinal() {
        return this == COMPLETED || this == EXPIRED || this == CANCELLED;
    }

    /**
     * Checks if this status indicates successful completion
     */
    public boolean isSuccess() {
        return this == COMPLETED;
    }

    /**
     * Checks if this status indicates failure or cancellation
     */
    public boolean isFailure() {
        return this == EXPIRED || this == CANCELLED;
    }
}