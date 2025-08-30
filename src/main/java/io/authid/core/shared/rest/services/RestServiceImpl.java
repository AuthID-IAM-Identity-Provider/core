package io.authid.core.shared.rest.services;

import io.authid.core.shared.rest.contracts.RestRequest;
import io.authid.core.shared.rest.contracts.RestService;
import io.authid.core.shared.rest.contracts.hooks.RestServiceHooks;
import io.authid.core.shared.rest.services.commons.fetch.FetchAllQuery;
import io.authid.core.shared.rest.services.commons.fetch.FetchAllQueryHandler;
import io.authid.core.shared.rest.services.commons.find.FindByIdQuery;
import io.authid.core.shared.rest.services.commons.find.FindByIdQueryHandler;
import io.authid.core.shared.rest.services.commons.update.UpdateByIdCommand;
import io.authid.core.shared.rest.services.commons.update.UpdateByIdCommandHandler;
import io.authid.core.shared.utils.UniPaginatedResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.Map;

@Slf4j
public abstract class RestServiceImpl<T, ID, C extends RestRequest, U extends RestRequest> implements RestService<T, ID, C, U> {

    protected abstract <R extends JpaRepository<T, ID> & JpaSpecificationExecutor<T>> R getRepository();

    protected abstract RestServiceHooks<T, ID, C, U> getHooks();

    @Override
    public long count(String searchTerm, Map<String, Object> filters, Pageable pageable, String cursor) {
        return getRepository().count();
    }

    @Override
    public UniPaginatedResult<T> fetchAll(String searchTerm, Map<String, Object> filters, Pageable pageable, String cursor) {
        return new FetchAllQueryHandler<T>().handle(
            new FetchAllQuery<>(
                searchTerm,
                filters,
                pageable,
                cursor,
                getRepository(),
                getHooks()
            )
        );
    }

    @Override
    public T findById(ID id) {
        return new FindByIdQueryHandler<T, ID>().handle(
            new FindByIdQuery<>(id, getRepository(), getHooks())
        );
    }

    @Override
    public T create(
        C createRequest) {
        return getRepository().save(
            getHooks().onCreating(createRequest)
        );
    }

    @Override
    public T update(ID id, U updateRequest) {
        return new UpdateByIdCommandHandler<T, ID, C, U>().handle(
            new UpdateByIdCommand<T, ID, C, U>(
                id,
                updateRequest,
                getRepository(),
                getHooks()
            )
        );
    }

    @Override
    public void deleteById(ID id) {
        getRepository().deleteById(id);
    }
}