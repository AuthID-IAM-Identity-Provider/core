package io.authid.core.shared.constants; // Atau package konstanta Anda

public final class ErrorConstants {

    private ErrorConstants() {
        // Private constructor to prevent instantiation
    }

    // Spring Profiles
    public static final String PROFILE_PRODUCTION = "production";

    // Error Codes
    public static final String CODE_INTERNAL_SERVER_ERROR = "500-INTERNAL";

    // Message Keys for i18n
    public static final String KEY_INTERNAL_ERROR_TITLE = "error.internal.server.title";
    public static final String KEY_INTERNAL_ERROR_MESSAGE_PROD = "error.internal.server.message.prod";
    public static final String KEY_INTERNAL_ERROR_MESSAGE_DEV = "error.internal.server.message.dev";
}