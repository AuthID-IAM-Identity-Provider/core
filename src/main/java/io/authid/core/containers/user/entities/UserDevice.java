package io.authid.core.containers.user.entities;

import io.authid.core.containers.user.enums.DeviceStatus;
import io.authid.core.containers.user.enums.DeviceType;
import io.authid.core.shared.components.database.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_devices", indexes = {
        @Index(name = "idx_user_devices", columnList = "user_id"),
        @Index(name = "idx_device_status", columnList = "status"),
        @Index(name = "idx_device_type", columnList = "device_type"),
        @Index(name = "idx_last_login_device", columnList = "last_login_at"),
        @Index(name = "idx_current_device", columnList = "is_current_device"),
        @Index(name = "idx_device_location", columnList = "country, city"),
        @Index(name = "idx_user_device_fingerprint", columnList = "user_id, device_fingerprint"),
        @Index(name = "idx_device_fingerprint", columnList = "device_fingerprint"),
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDevice extends BaseEntity<UUID> {
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "device_name", length = 255)
    private String deviceName;

    @Column(name = "device_fingerprint", nullable = false, unique = true)
    private String deviceFingerprint;

    @Column(name = "device_type", nullable = false)
    private DeviceType deviceType = DeviceType.UNKNOWN;

    @Column(name = "os_name", length = 100)
    private String osName;

    @Column(name = "os_version", length = 50)
    private String osVersion;

    @Column(name = "browser_name", length = 100)
    private String browserName;

    @Column(name = "browser_version", length = 50)
    private String browserVersion;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "country", length = 3)
    private String country;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "status", nullable = false)
    private DeviceStatus status = DeviceStatus.ACTIVE;

    @Column(name = "is_current_device", nullable = false)
    private boolean isCurrentDevice;

    @Column(name = "last_login_at", nullable = false)
    private String lastLoginAt;

    @Column(name = "first_seen_at", nullable = false)
    private String firstSeenAt;

    public void markAsCurrentDevice() {
        this.isCurrentDevice = true;
    }

    public void revokeCurrentDevice() {
        this.isCurrentDevice = false;
    }

    public void updateLastLogin() {
        this.lastLoginAt = String.valueOf(Instant.now());
    }

    public void trust() {
        this.status = DeviceStatus.TRUSTED;
    }

    public void untrust() {
        this.status = DeviceStatus.UNTRUSTED;
    }

    public void block() {
        this.status = DeviceStatus.BLOCKED;
    }

    public boolean isTrusted() {
        return this.status == DeviceStatus.TRUSTED;
    }

    public boolean isBlocked() {
        return this.status == DeviceStatus.BLOCKED;
    }

    public boolean isUntrusted() {
        return this.status == DeviceStatus.UNTRUSTED;
    }

    public boolean isSameFingerprint(String fingerprint) {
        return this.deviceFingerprint != null && this.deviceFingerprint.equals(fingerprint);
    }
}
