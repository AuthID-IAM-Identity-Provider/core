package io.authid.core.shared.rest.services;

import io.authid.core.shared.rest.contracts.RestRequest;
import io.authid.core.shared.rest.contracts.RestService;
import io.authid.core.shared.rest.contracts.hooks.RestServiceHooks;
import io.authid.core.shared.rest.services.commons.fetch.FetchAllQuery;
import io.authid.core.shared.rest.services.commons.fetch.FetchAllQueryHandler;
import io.authid.core.shared.rest.services.commons.find.FindByIdQuery;
import io.authid.core.shared.rest.services.commons.find.FindByIdQueryHandler;
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
        FetchAllQuery<T> allQuery = new FetchAllQuery<>(
            searchTerm,
            filters,
            pageable,
            cursor,
            getRepository(),
            getHooks()
        );

        return new FetchAllQueryHandler<T>().handle(allQuery);
    }

    @Override
    public T findById(ID id) {

        FindByIdQuery<T, ID> byIdQuery = new FindByIdQuery<>(id, getRepository(), getHooks());

        return new FindByIdQueryHandler<T, ID>().handle(byIdQuery);
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
        T entity = findById(id);
        return getRepository().save(entity);
    }

    @Override
    public void deleteById(ID id) {
        getRepository().deleteById(id);
    }
}