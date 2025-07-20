package io.authid.core.containers.user.enums;

import lombok.Getter;

@Getter
public enum DeviceType {
    MOBILE("Mobile", "Perangkat bergerak seperti smartphone atau tablet", "smartphone"),
    DESKTOP("Desktop", "Komputer desktop atau laptop", "monitor"),
    TABLET("Tablet", "Perangkat tablet", "tablet"),
    WEARABLE("Wearable", "Perangkat wearable seperti smartwatch", "watch"),
    UNKNOWN("Tidak Dikenal", "Jenis perangkat tidak teridentifikasi", "help");

    private final String label;
    private final String description;
    private final String icon;

    DeviceType(String label, String description, String icon) {
        this.label = label;
        this.description = description;
        this.icon = icon;
    }

    public boolean isMobile() {
        return this == MOBILE || this == TABLET;
    }
    
    public boolean isTablet() {
        return this == TABLET;
    }

    public boolean isDesktop() {
        return this == DESKTOP;
    }

    public boolean isWearable() {
        return this == WEARABLE;
    }

    public static DeviceType getDefault() {
        return UNKNOWN;
    }

    public static DeviceType fromString(String type) {
        return switch (type) {
            case "mobile" -> MOBILE;
            case "desktop" -> DESKTOP;
            case "tablet" -> TABLET;
            case "wearable" -> WEARABLE;
            default -> UNKNOWN;
        };
    }

    public static DeviceType fromUserAgent(String userAgent) {
        if (userAgent == null) return UNKNOWN;
        String ua = userAgent.toLowerCase();
        if (ua.contains("mobile")) return MOBILE;
        if (ua.contains("tablet")) return TABLET;
        if (ua.contains("windows") || ua.contains("macintosh") || ua.contains("linux")) return DESKTOP;
        if (ua.contains("smartwatch")) return WEARABLE;
        return UNKNOWN;
    }
}
