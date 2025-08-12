package io.authid.core.shared.rest.contracts.hooks.commons;

import io.authid.core.shared.utils.UniPaginatedResult;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface FetchAllHooks<T> {

    List<String> getSearchableColumns();

    List<String> getFilterableColumns();

    String getCursorValue(T entity);

    void beforeFetchAll(String searchTerm, Map<String, Object> filters, Pageable pageable, String cursor);

    void onFetchingAll();

    void afterFetchAll(UniPaginatedResult<T> result);
}
