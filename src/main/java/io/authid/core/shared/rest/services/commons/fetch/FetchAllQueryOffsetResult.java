package io.authid.core.shared.rest.services.commons.fetch;

import io.authid.core.shared.utils.UniPaginatedResult;
import io.authid.core.shared.utils.UniPagination;
import io.authid.core.shared.utils.UniPaginationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public class FetchAllQueryOffsetResult {
    public static <T> UniPaginatedResult<T> getResult(
        Specification<T> specification,
        JpaSpecificationExecutor<T> repository,
        Pageable pageable
    ) {
        Page<T> page = repository.findAll(specification, pageable);

        UniPagination pagination = UniPagination.builder()
            .type(UniPaginationType.OFFSET)
            .page(page.getNumber() + 1)
            .perPage(page.getSize())
            .totalPages(page.getTotalPages())
            .totalItems((int) page.getTotalElements())
            .build();
        return new UniPaginatedResult<>(page.getContent(), pagination);
    }
}
