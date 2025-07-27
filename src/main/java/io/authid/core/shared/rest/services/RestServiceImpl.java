package io.authid.core.shared.rest.services;
import io.authid.core.shared.rest.contracts.RestService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public abstract class RestServiceImpl<T, ID, C, U> implements RestService<T, ID, C, U> {
    @Override
    public Page<T> findAll(Pageable pageable, Map<String, Object> filters) {
        return null;
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