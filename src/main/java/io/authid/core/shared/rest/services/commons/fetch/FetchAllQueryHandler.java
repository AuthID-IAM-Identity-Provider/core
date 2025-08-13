package io.authid.core.shared.rest.services.commons.fetch;

import io.authid.core.shared.rest.contracts.RestQueryHandler;
import io.authid.core.shared.rest.contracts.hooks.commons.FetchAllHooks;
import io.authid.core.shared.rest.specifications.GenericSpecificationBuilder;
import io.authid.core.shared.utils.UniPaginatedResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class FetchAllQueryHandler<T> implements RestQueryHandler<UniPaginatedResult<T>, FetchAllQuery<T>> {

    @Override
    public UniPaginatedResult<T> handle(FetchAllQuery<T> query) {
        FetchAllHooks<T> hooks = query.hooks();

        hooks.beforeFetchAll(
            query.searchTerm(),
            query.filters(),
            query.pageable(),
            query.cursor()
        );

        GenericSpecificationBuilder<T> specificationBuilder = FetchAllQuerySpecificationBuilder.create(
            hooks.getSearchableColumns(),
            hooks.getFilterableColumns()
        );

        Specification<T> specification = specificationBuilder.build(
            query.searchTerm(),
            query.filters()
        );

        hooks.onFetchingAll();

        UniPaginatedResult<T> uniPaginatedResult = Optional.ofNullable(query.cursor())
            .filter(cursor -> !cursor.isBlank())
            .map(cursor -> FetchAllQueryCursorResult.getResult(
                specification,
                query.repository(),
                query.pageable(),
                cursor,
                hooks
            ))
            .orElseGet(() -> FetchAllQueryOffsetResult.getResult(
                specification,
                query.repository(),
                query.pageable()
            ));

        hooks.afterFetchAll(uniPaginatedResult);

        return uniPaginatedResult;
    }

    protected static class FetchAllQuerySpecificationBuilder {
        public static <T> GenericSpecificationBuilder<T> create(List<String> searchableColumns, List<String> filterableColumns) {
            return new GenericSpecificationBuilder<>(searchableColumns, filterableColumns);
        }
    }
}
