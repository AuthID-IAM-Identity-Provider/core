package io.authid.core.containers.user.response;

import io.authid.core.shared.rest.contracts.RestResponse;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Builder
@EqualsAndHashCode(callSuper = true)
public class CreateUserResponse extends RestResponse {
    private String id;
    private String firstName;
    private String email;
    private boolean isEmailVerified;
    private boolean isAccountLocked;
    private boolean hasTwoFactorAuth;
}
