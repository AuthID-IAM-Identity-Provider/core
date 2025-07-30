package io.authid.core.shared.utils;

import lombok.*;

@Data
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UniPagination {
    private UniPaginationType type;
    private Integer perPage;
    private Integer page;
    private Integer size;
    private Integer totalPages;
    private Integer totalItems;
    private String nextCursor;
    private String prevCursor;
    private Boolean hasMore;
}
