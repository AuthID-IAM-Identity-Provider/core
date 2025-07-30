package io.authid.core.containers.user.response;

import io.authid.core.shared.rest.contracts.RestResponse;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;

@Data
@Builder
@EqualsAndHashCode(callSuper = true)
public class DetailUserResponse extends RestResponse {
    // --- Summary fields ---
    private String id;
    private String firstName;
    private String email;
    private String createdAt;

    // --- Raw Fields (Directly from UserEntity, possibly renamed/formatted) ---

    // --- Computed Fields (Derived from UserEntity's logic/state) ---
    private boolean isEmailVerified;
    private boolean isAccountLocked;
    private boolean hasTwoFactorAuth;
    private boolean canLogin;
    private String lastLoginAt; // Last login timestamp
    private String lastLoginIp;  // Last login IP address (can be sensitive, consider if to expose)
    private String loginCount;  // Total login count
    private String failedLoginAttempts; // Failed attempts (can be sensitive for public API, adjust as needed)
    private String daysSinceLastLogin; // Example: days since last login, for activity tracking

    // --- Optional: Fields for richer user profile ---
}
