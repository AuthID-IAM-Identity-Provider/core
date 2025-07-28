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
public class DeleteUserRequest extends RestRequest {

}