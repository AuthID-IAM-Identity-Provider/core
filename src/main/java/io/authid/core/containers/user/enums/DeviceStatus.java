package io.authid.core.containers.user.enums;

public enum DeviceStatus {
    ACTIVE("Aktif", "Perangkat aktif dan digunakan", "green", true),
    TRUSTED("Dipercaya", "Perangkat telah dipercaya oleh pengguna", "blue", true),
    UNTRUSTED("Tidak Dipercaya", "Perangkat tidak dipercaya oleh pengguna", "yellow", true),
    EXPIRED("Kedaluwarsa", "Token atau sesi perangkat telah habis", "orange", false),
    REVOKED("Dicabut", "Akses perangkat dicabut", "gray", false),
    SUSPICIOUS("Mencurigakan", "Aktivitas mencurigakan terdeteksi", "red", false),
    BLOCKED("Diblokir", "Perangkat diblokir oleh sistem", "black", false);

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

    public String getLabel() { return label; }
    public String getDescription() { return description; }
    public String getColor() { return color; }
    public boolean isUsable() { return usable; }

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
