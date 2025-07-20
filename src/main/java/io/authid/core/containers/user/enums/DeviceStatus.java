package io.authid.core.containers.user.enums;

import io.authid.core.shared.components.i18n.I18n;
import lombok.Getter;

@Getter
public enum DeviceStatus {
    ACTIVE(I18n.extract("device.active.label"), I18n.extract("device.active.description"), "green", true),
    TRUSTED(I18n.extract("device.trusted.label"), I18n.extract("device.trusted.description"), "blue", true),
    UNTRUSTED(I18n.extract("device.untrusted.label"), I18n.extract("device.untrusted.description"), "yellow", true),
    EXPIRED(I18n.extract("device.expired.label"), I18n.extract("device.expired.description"), "orange", false),
    REVOKED(I18n.extract("device.revoked.label"), I18n.extract("device.revoked.description"), "gray", false),
    SUSPICIOUS(I18n.extract("device.suspicious.label"), I18n.extract("device.suspicious.description"), "red", false),
    BLOCKED(I18n.extract("device.blocked.label"), I18n.extract("device.blocked.description"), "black", false);

    private final String label;
    private final String description;
    private final String color;
    private final boolean usable;

    DeviceStatus(String label, String description, String color, boolean usable) {
        this.label = label;
        this.description = description;
        this.color = color;
        this.usable = usable;
    }

    public boolean isTrusted() {
        return this == TRUSTED;
    }

    public boolean isRevoked() {
        return this == REVOKED || this == BLOCKED;
    }

    public boolean isExpiredOrRevoked() {
        return this == EXPIRED || isRevoked();
    }

    public boolean canLoginFromDevice() {
        return usable && this != SUSPICIOUS;
    }

    public static DeviceStatus getDefault() {
        return ACTIVE;
    }

    public boolean canTransitionTo(DeviceStatus newStatus) {
        return switch (this) {
            case ACTIVE -> newStatus == TRUSTED || newStatus == EXPIRED || newStatus == REVOKED || newStatus == BLOCKED;
            case UNTRUSTED -> false;
            case TRUSTED, EXPIRED -> newStatus == ACTIVE || newStatus == REVOKED || newStatus == BLOCKED;
            case SUSPICIOUS -> newStatus == BLOCKED || newStatus == REVOKED;
            case REVOKED -> newStatus == BLOCKED;
            case BLOCKED -> false; // terminal state
        };
    }
}
