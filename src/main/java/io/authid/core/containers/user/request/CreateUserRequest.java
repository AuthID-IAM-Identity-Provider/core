package io.authid.core.containers.user.request;

import io.authid.core.containers.user.validation.annotations.PasswordMatches;
import io.authid.core.containers.user.validation.annotations.UniqueEmail;
import io.authid.core.shared.rest.contracts.RestRequest;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@PasswordMatches(message = "{user.password.mismatch}")
public class CreateUserRequest extends RestRequest {

    @NotBlank(message = "{user.name.required}")
    @Size(min = 3, max = 255, message = "{user.name.size}")
    private String name;

    @NotBlank(message = "{user.email.required}")
    @Email(message = "{user.email.format}")
    @UniqueEmail(message = "{user.email.unique}")
    private String email;

    @NotBlank(message = "{user.password.required}")
    @Size(min = 8, message = "{user.password.size}")
    private String password;

    @NotBlank(message = "{user.passwordConfirmation.required}")
    private String passwordConfirmation;

    @NotBlank(message = "{user.status.required}")
    private String status;
}