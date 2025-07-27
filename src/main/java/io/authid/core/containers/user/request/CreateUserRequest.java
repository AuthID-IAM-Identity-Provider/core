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
@PasswordMatches(message = "{user.password.mismatch}") // <-- Diperbaiki
public class CreateUserRequest extends RestRequest {

    @NotBlank(message = "{user.name.required}") // <-- Diperbaiki
    @Size(min = 3, max = 255, message = "{user.name.size}") // <-- Diperbaiki
    private String name;

    @NotBlank(message = "{user.email.required}") // <-- Diperbaiki
    @Email(message = "{user.email.format}") // <-- Diperbaiki
    @UniqueEmail(message = "{user.email.unique}") // <-- Diperbaiki
    private String email;

    @NotBlank(message = "{user.password.required}") // <-- Diperbaiki
    @Size(min = 8, message = "{user.password.size}") // <-- Diperbaiki
    private String password;

    @NotBlank(message = "{user.passwordConfirmation.required}") // <-- Diperbaiki
    private String passwordConfirmation;
}