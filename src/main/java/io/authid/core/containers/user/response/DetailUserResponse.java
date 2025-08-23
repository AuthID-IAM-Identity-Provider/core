package io.authid.core.containers.user.response;

import io.authid.core.shared.rest.contracts.RestResponse;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Builder
@EqualsAndHashCode(callSuper = true)
public class DetailUserResponse extends RestResponse {
    private String id;
    private String firstName;
    private String email;
    private String createdAt;
    private boolean isEmailVerified;
    private boolean isAccountLocked;
    private boolean hasTwoFactorAuth;
    private boolean canLogin;
    private String lastLoginAt;
    private String lastLoginIp;
    private String loginCount;
    private String failedLoginAttempts;
    private String daysSinceLastLogin;
    private String daysSinceCreated;
}
