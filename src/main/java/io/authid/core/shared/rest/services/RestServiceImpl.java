package io.authid.core.shared.rest.services;
import io.authid.core.shared.components.database.repository.BaseRepository;
import io.authid.core.shared.rest.contracts.RestRequest;
import io.authid.core.shared.rest.contracts.RestService;
import io.authid.core.shared.rest.specifications.GenericSpecificationBuilder;
import io.authid.core.shared.utils.UniPaginatedResult;
import io.authid.core.shared.utils.UniPagination;
import io.authid.core.shared.utils.UniPaginationType;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;
import java.util.List;
import java.util.Map;

@Slf4j
public abstract class RestServiceImpl<T, ID, C extends RestRequest, U extends RestRequest> implements RestService<T, ID, C, U> {

    public abstract <R extends JpaRepository<T, ID> & JpaSpecificationExecutor<T>> R getRepository();
    public abstract List<String> getSearchableColumns();
    public abstract List<String> getFilterableColumns();
    protected abstract String getCursorValue(T entity);
    protected abstract T onCreating(C createRequest);
    protected abstract void onUpdate(U updateRequest, T entity);


    // Hooks for FindAll
    protected void beforeFindAll(String searchTerm, Map<String, Object> filters) {}
    protected void afterFindAll(UniPaginatedResult<T> result) {}

    // Hooks for FindById
    protected void beforeFindById(ID id) {}
    protected void afterFindById(T entity) {}

    // Hooks for Create
    protected void beforeCreate(C createRequest) {}
    protected void afterCreate(T savedEntity) {}

    // Hooks for Update
    protected void beforeUpdate(ID id, U updateRequest) {}
    protected void afterUpdate(T updatedEntity) {}

    // Hooks for Delete
    protected void beforeDelete(ID id) {}
    protected void afterDelete(ID id) {}


    @Override
    public long count(String searchTerm, Map<String, Object> filters, Pageable pageable, String cursor){
        return getRepository().count();
    }

    @Override
    @Transactional(readOnly = true)
    public UniPaginatedResult<T> findAll(String searchTerm, Map<String, Object> filters, Pageable pageable, String cursor) {
        beforeFindAll(searchTerm, filters);
        GenericSpecificationBuilder<T> specBuilder = new GenericSpecificationBuilder<>(getSearchableColumns(), getFilterableColumns());
        Specification<T> spec = specBuilder.build(searchTerm, filters);

        UniPaginatedResult<T> result;
        if (cursor != null && !cursor.isBlank()) {
            result = findWithCursor(spec, cursor, pageable);
        } else {
            result = findWithLengthAware(spec, pageable);
        }
        afterFindAll(result);
        return result;
    }

    private UniPaginatedResult<T> findWithCursor(Specification<T> spec, String cursor, Pageable pageable) {
        try {
            UUID cursorId = UUID.fromString(cursor);
            Specification<T> cursorSpec = (root, query, cb) -> cb.lessThan(root.get("id"), cursorId);
            spec = spec.and(cursorSpec);
            Pageable cursorPageable = PageRequest.of(0, pageable.getPageSize() + 1, pageable.getSort());
            List<T> results = getRepository().findAll(spec, cursorPageable).getContent();
            return buildCursorResult(results, pageable.getPageSize());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid UUID format for cursor: {}", cursor);
            return buildCursorResult(List.of(), pageable.getPageSize());
        }
    }

    private UniPaginatedResult<T> findWithLengthAware(Specification<T> spec, Pageable pageable) {
        Page<T> page = getRepository().findAll(spec, pageable);
        return buildLengthAwareResult(page);
    }

    private UniPaginatedResult<T> buildLengthAwareResult(Page<T> page) {
        UniPagination pagination = UniPagination.builder()
                .type(UniPaginationType.LENGTH_AWARE)
                .page(page.getNumber() + 1)
                .perPage(page.getSize())
                .totalPages(page.getTotalPages())
                .totalItems((int) page.getTotalElements())
                .build();

        return new UniPaginatedResult<>(page.getContent(), pagination);
    }

    private UniPaginatedResult<T> buildCursorResult(List<T> results, int pageSize) {
        boolean hasMore = results.size() > pageSize;
        List<T> data = hasMore ? results.subList(0, pageSize) : results;

        String nextCursor = hasMore ? getCursorValue(data.get(pageSize - 1)) : null;

        UniPagination pagination = UniPagination.builder()
                .type(UniPaginationType.CURSOR)
                .perPage(pageSize)
                .hasMore(hasMore)
                .nextCursor(nextCursor)
                .build();

        return new UniPaginatedResult<>(data, pagination);
    }

    @Override
    @Transactional(readOnly = true)
    public T findById(ID id) {
        beforeFindById(id);
        T entity = getRepository().findById(id)
                .orElseThrow(() -> new RuntimeException("Resource with ID " + id + " not found.")); // Ganti dengan exception kustom
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
        T entity = findById(id); // Memanfaatkan findById yang sudah ada hook-nya
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
}