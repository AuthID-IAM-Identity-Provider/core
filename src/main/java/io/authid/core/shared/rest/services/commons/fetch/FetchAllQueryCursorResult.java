package io.authid.core.shared.rest.services.commons.fetch;

import io.authid.core.shared.rest.contracts.hooks.commons.FetchAllHooks;
import io.authid.core.shared.utils.UniPaginatedResult;
import io.authid.core.shared.utils.UniPagination;
import io.authid.core.shared.utils.UniPaginationType;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public class FetchAllQueryCursorResult {
    public static <T> UniPaginatedResult<T> getResult(
        Specification<T> specification,
        JpaSpecificationExecutor<T> repository,
        Pageable pageable,
        String cursor,
        FetchAllHooks<T> hooks
    ) {
        UUID cursorId = UUID.fromString(cursor);
        Specification<T> cursorSpec = (root, query, cb) -> cb.lessThan(root.get("id"), cursorId);
        Specification<T> finalSpec = specification.and(cursorSpec);

        Pageable page = PageRequest.of(0, pageable.getPageSize() + 1, pageable.getSort());
        List<T> result = repository.findAll(finalSpec, page).getContent();

        boolean hasMore = result.size() > page.getPageSize();
        List<T> data = hasMore ? result.subList(0, page.getPageSize()) : result;

        String nextCursor = hasMore ? hooks.getCursorValue(data.get(page.getPageSize() - 1)) : null;

        UniPagination pagination = UniPagination.builder()
            .type(UniPaginationType.CURSOR)
            .perPage(page.getPageSize())
            .hasMore(hasMore)
            .nextCursor(nextCursor)
            .build();

        return new UniPaginatedResult<T>(data, pagination);
    }
}
