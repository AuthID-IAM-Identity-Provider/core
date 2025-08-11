package io.authid.core.shared.rest.services.handlers;

import io.authid.core.shared.components.events.EntityLifecycleEvents;
import io.authid.core.shared.components.events.EventBus;
import io.authid.core.shared.rest.contracts.RestQueryHandler;
import io.authid.core.shared.rest.services.queries.FetchAllQuery;
import io.authid.core.shared.rest.services.utils.FetchAllQueryResult;
import io.authid.core.shared.rest.specifications.GenericSpecificationBuilder;
import io.authid.core.shared.utils.UniPaginatedResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class FetchAllQueryHandler<T, ID, C, U> implements RestQueryHandler<UniPaginatedResult<T>, FetchAllQuery> {

    private final EventBus EventBus;

    @Override
    public UniPaginatedResult<T> handle(FetchAllQuery query) {
        EventBus.dispatch(new EntityLifecycleEvents.BeforeFetchAllEvent());

        Specification<?> specification = FetchAllQuerySpecificationBuilder
            .create(
                List.of(""),
                List.of("")
            )
            .build(query.searchTerm(), query.filters());

        UniPaginatedResult<Object> cursorResult = FetchAllQueryResult.cursorResult();
        return null;
    }

    protected static class FetchAllQuerySpecificationBuilder {
        public static GenericSpecificationBuilder<?> create(List<String> searchableColumns, List<String> filterableColumns) {
            return new GenericSpecificationBuilder<>(searchableColumns, filterableColumns);
        }
    }
}
