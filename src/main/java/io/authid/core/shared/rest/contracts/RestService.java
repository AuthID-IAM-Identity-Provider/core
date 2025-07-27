package io.authid.core.shared.rest.contracts;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface RestService<T, ID, C, U> {
    Page<T> findAll(Pageable pageable, Map<String, Object> filters);
    T create(C createRequest);
    T findById(ID id);
    T update(ID id, U updateRequest);
    void deleteById(ID id);
    List<T> batchCreate(List<C> createRequests);
    List<T> batchUpdate(Map<ID, U> updateRequests);
    void batchDeleteByIds(List<ID> ids);
}
