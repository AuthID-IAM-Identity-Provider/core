package io.authid.core.shared.rest.contracts;

import io.authid.core.shared.utils.UniPaginatedResult;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface RestService<T, ID, C, U> {
    UniPaginatedResult<T> fetchAll(String searchTerm, Map<String, Object> filters, Pageable pageable, String cursor);

    long count(String searchTerm, Map<String, Object> filters, Pageable pageable, String cursor);

    T create(C createRequest);

    T findById(ID id);

    T update(ID id, U updateRequest);

    void deleteById(ID id);

    List<T> batchCreate(List<C> createRequests);

    List<T> batchUpdate(Map<ID, U> updateRequests);

    void batchDeleteByIds(List<ID> ids);
}
