package io.authid.core.shared.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;

@Data
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor(force = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UniError {
    private final String code;
    private final String category;
    private final String module;
    private final String cause;
    private final String action;

    @JsonInclude(JsonInclude.Include.NON_EMPTY) // Hanya tampil jika list tidak kosong
    private final List<FieldErrorDetail> fieldErrors;
}
