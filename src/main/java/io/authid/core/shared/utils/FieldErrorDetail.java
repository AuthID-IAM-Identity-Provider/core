package io.authid.core.shared.utils;

import lombok.*;

@Data
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class FieldErrorDetail {
    private final String field;
    private final String message;
}
