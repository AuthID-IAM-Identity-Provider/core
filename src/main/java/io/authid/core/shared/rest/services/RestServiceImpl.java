package io.authid.core.shared.rest.services;

import io.authid.core.shared.components.exception.BaseApplicationException;
import io.authid.core.shared.rest.contracts.RestRequest;
import io.authid.core.shared.rest.contracts.RestService;
import io.authid.core.shared.rest.contracts.hooks.RestServiceHooks;
import io.authid.core.shared.rest.services.handlers.FetchAllQueryHandler;
import io.authid.core.shared.rest.services.queries.FetchAllQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

@Slf4j
public abstract class RestServiceImpl<T, ID, C extends RestRequest, U extends RestRequest> implements RestService<T, ID, C, U> {

    protected abstract <R extends JpaRepository<T, ID> & JpaSpecificationExecutor<T>> R getRepository();

    protected abstract List<String> getSearchableColumns();

    protected abstract List<String> getFilterableColumns();

    protected abstract String getCursorValue(T entity);

    protected abstract T onCreating(C createRequest);

    protected abstract void onUpdate(U updateRequest, T entity);

    protected abstract RestServiceHooks<T, ID, C, U> getHooks();

     Hooks for FindAll
    protected void beforeFindAll(String searchTerm, Map<String, Object> filters) {}
    protected void afterFindAll(UniPaginatedResult<T> result) {}

     Hooks for FindById
    protected void beforeFindById(ID id) {}
    protected abstract BaseApplicationException onNotFound(ID id);
    protected void afterFindById(T entity) {}

     Hooks for Create
    protected void beforeCreate(C createRequest) {}
    protected void afterCreate(T savedEntity) {}

     Hooks for Update
    protected void beforeUpdate(ID id, U updateRequest) {}
    protected void afterUpdate(T updatedEntity) {}

     Hooks for Delete
    protected void beforeDelete(ID id) {}
    protected void afterDelete(ID id) {}

    @Override
    public long count(String searchTerm, Map<String, Object> filters, Pageable pageable, String cursor){
        return getRepository().count();
    }

    public UniPaginatedResult<T> fetchAll(String searchTerm, Map<String, Object> filters, Pageable pageable, String cursor) {
        FetchAllQuery<T> query = new FetchAllQuery<T>(
            searchTerm,filters,pageable, cursor, getRepository(), getHooks()
        );

        return new FetchAllQueryHandler<T>().handle(query);
    }

    @Override
    public T findById(ID id) {
        beforeFindById(id);
        T entity = getRepository().findById(id)
                .orElseThrow(() -> onNotFound(id));  Ganti dengan exception kustom
        afterFindById(entity);
        return entity;
    }

    @Override
    public T create(C createRequest) {
        beforeCreate(createRequest);
        T entity = onCreating(createRequest);
        T savedEntity = getRepository().save(entity);
        afterCreate(savedEntity);
        return savedEntity;
    }

    @Override
    public T update(ID id, U updateRequest) {
        T entity = findById(id);  Memanfaatkan findById yang sudah ada hook-nya
        beforeUpdate(id, updateRequest);
        onUpdate(updateRequest, entity);
        T updatedEntity = getRepository().save(entity);
        afterUpdate(updatedEntity);
        return updatedEntity;
    }

    @Override
    public void deleteById(ID id) {
        beforeDelete(id);
        getRepository().deleteById(id);
        afterDelete(id);
    }

    @Override
    public List<T> batchCreate(List<C> createRequests) {
        throw new UnsupportedOperationException("Batch create is not implemented for this service.");
    }

    @Override
    public List<T> batchUpdate(Map<ID, U> updateRequests) {
        throw new UnsupportedOperationException("Batch update is not implemented for this service.");
    }

    @Override
    public void batchDeleteByIds(List<ID> ids) {
        throw new UnsupportedOperationException("Batch delete is not implemented for this service.");
    }

    private List<T> processBatchSync(List<C> createRequests) {
        List<T> entities = createRequests.stream()
                .map(this::onCreating)
                .collect(Collectors.toUnmodifiableList());

        return getRepository().saveAll(entities);
    }
}