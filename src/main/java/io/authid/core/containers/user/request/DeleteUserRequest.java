package io.authid.core.containers.user.request;

import io.authid.core.containers.user.validation.annotations.PasswordMatches;
import io.authid.core.shared.rest.contracts.RestRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@PasswordMatches(message = "{user.password.mismatch}")
public class DeleteUserRequest extends RestRequest {

}