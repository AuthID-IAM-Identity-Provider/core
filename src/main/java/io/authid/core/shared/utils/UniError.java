package io.authid.core.shared.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;
import java.util.Map;

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
    private final String title;
    private final String cause;
    private final String action;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, List<String>> fieldErrors;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<String, Object> debugInfo;
}
