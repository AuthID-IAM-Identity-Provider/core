package io.authid.core.shared.rest.transformer;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Data transformer
 *
 * @param <T>              Entity Type
 * @param <IndexResponse>  DTO for list/summary/fetchAll
 * @param <DetailResponse> DTO for detail/findById
 * @param <CreateResponse> DTO for response after create
 * @param <UpdateResponse> DTO for response after update
 */
public interface RestTransformer<T, IndexResponse, DetailResponse, CreateResponse, UpdateResponse, DeleteResponse> {

    IndexResponse toIndex(T entity);

    DetailResponse toDetail(T entity);

    CreateResponse toCreateResponse(T entity);

    UpdateResponse toUpdateResponse(T entity);

    DeleteResponse toDeleteResponse(T entity);

    default List<IndexResponse> toIndex(List<T> entities) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }
        return entities.stream()
            .map(this::toIndex)
            .collect(Collectors.toList());
    }
}