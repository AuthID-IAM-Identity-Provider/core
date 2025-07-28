package io.authid.core.containers.user.response;


import io.authid.core.shared.rest.contracts.RestResponse;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Builder
@EqualsAndHashCode(callSuper = true)
public class IndexUserResponse extends RestResponse {
    private String id;
    private String firstName;
    private String email;
    private String createdAt;
}
