package io.authid.core.shared.rest.services.handlers;

import io.authid.core.shared.components.events.EntityLifecycleEvents;
import io.authid.core.shared.components.events.EventBus;
import io.authid.core.shared.rest.contracts.RestQueryHandler;
import io.authid.core.shared.rest.services.queries.FetchAllQuery;
import io.authid.core.shared.utils.UniPaginatedResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FetchAllQueryHandler<T, ID, C, U> implements RestQueryHandler<UniPaginatedResult<T>, FetchAllQuery> {

    private final EventBus EventBus;

    @Override
    public UniPaginatedResult<T> handle(FetchAllQuery query) {
        EventBus.dispatch(new EntityLifecycleEvents.BeforeFetchAllEvent());

        return null;
    }
}
