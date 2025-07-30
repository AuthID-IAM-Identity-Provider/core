package io.authid.core.shared.utils;

import lombok.*;

import java.time.Instant;

@Data
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UniMeta {
    private String requestId;
    private String traceId;
    private String sessionId;
    private Instant timestamp;
    private UniPagination pagination;
}
