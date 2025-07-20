package io.authid.core.containers.user.enums;

import io.authid.core.shared.components.i18n.I18n;
import lombok.Getter;

@Getter
public enum PasswordResetTokenStatus {
    PENDING(I18n.extract("token.status.pending.label"), I18n.extract("token.status.pending.description"), "blue", true, false),
    COMPLETED(I18n.extract("token.status.completed.label"), I18n.extract("token.status.completed.description"), "green", false, true),
    EXPIRED(I18n.extract("token.status.expired.label"), I18n.extract("token.status.expired.description"), "red", false, false),
    CANCELLED(I18n.extract("token.status.cancelled.label"), I18n.extract("token.status.cancelled.description"), "gray", false, false);

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