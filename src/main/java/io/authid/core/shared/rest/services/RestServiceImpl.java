package io.authid.core.shared.rest.services;
import io.authid.core.shared.components.database.repository.BaseRepository;
import io.authid.core.shared.rest.contracts.RestService;
import io.authid.core.shared.rest.specifications.GenericSpecificationBuilder;
import io.authid.core.shared.utils.UniPaginatedResult;
import io.authid.core.shared.utils.UniPagination;
import io.authid.core.shared.utils.UniPaginationType;
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
public abstract class RestServiceImpl<T, ID, C, U> implements RestService<T, ID, C, U> {

    public abstract <R extends JpaRepository<T, ID> & JpaSpecificationExecutor<T>> R getRepository();
    public abstract List<String> getSearchableColumns();
    public abstract List<String> getFilterableColumns();
    protected abstract String getCursorValue(T entity);

    @Override
    public long count(String searchTerm, Map<String, Object> filters, Pageable pageable, String cursor){
        return getRepository().count();
    }

    @Override
    public UniPaginatedResult<T> findAll(String searchTerm, Map<String, Object> filters, Pageable pageable, String cursor) {
        GenericSpecificationBuilder<T> specBuilder = new GenericSpecificationBuilder<>(getSearchableColumns(), getFilterableColumns());
        Specification<T> spec = specBuilder.build(searchTerm, filters);

        if (cursor != null && !cursor.isBlank()) {
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
    public T create(C createRequest) {
        return null;
    }

    @Override
    public T findById(ID id) {
        return null;
    }

    @Override
    public T update(ID id, U updateRequest) {
        return null;
    }

    @Override
    public void deleteById(ID id) {

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