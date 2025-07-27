package io.authid.core.containers.user.request;

import io.authid.core.containers.user.validation.annotations.PasswordMatches; // <-- Tambahkan ini
import io.authid.core.shared.rest.contracts.RestRequest;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@PasswordMatches(message = "{user.password.mismatch}") // <-- Tambahkan validasi password
public class UpdateUserRequest extends RestRequest {

    @Size(min = 3, max = 255, message = "{user.name.size}") // <-- Diperbaiki
    private String name;

    @Email(message = "{user.email.format}") // <-- Diperbaiki
    private String email;

    @Size(min = 8, message = "{user.password.size}") // <-- Diperbaiki
    private String password;

    private String passwordConfirmation;
}