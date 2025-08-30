package io.authid.core.shared.rest.controllers;

import io.authid.core.shared.rest.contracts.RestService;
import io.authid.core.shared.rest.transformer.RestTransformer;
import io.authid.core.shared.utils.UniPaginatedResult;
import io.authid.core.shared.utils.UniResponse;
import io.authid.core.shared.utils.UniResponseFactory;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public abstract class RestController<T, ID, CreateRequest, UpdateRequest, DeleteRequest, IndexResponse, DetailResponse, CreateResponse, UpdateResponse, DeleteResponse> {

    private static final int PAGINATION_THRESHOLD = 100;

    public abstract RestService<T, ID, CreateRequest, UpdateRequest> getService();

    public abstract RestTransformer<T, IndexResponse, DetailResponse, CreateResponse, UpdateResponse, DeleteResponse> getTransformer();

    public abstract UniResponseFactory getResponseFactory();

    @GetMapping
    public ResponseEntity<UniResponse<List<IndexResponse>>> findAll(
        Pageable pageable,
        @RequestParam(required = false) String q,
        @RequestParam(required = false) String cursor,
        @RequestParam(required = false) Map<String, Object> filters
    ) {

        log.info("hello updated module: {}", cursor);
        filters.remove("q");
        filters.remove("cursor");
        filters.remove("page");
        filters.remove("size");

        boolean useCursor = (cursor != null) || (pageable.isPaged() && pageable.getPageNumber() >= PAGINATION_THRESHOLD);
        UniPaginatedResult<T> result = getService().fetchAll(q, filters, pageable, useCursor ? cursor : null);

        List<IndexResponse> response = result
            .getData()
            .stream()
            .map(getTransformer()::toIndex)
            .collect(Collectors.toList());

        UniPaginatedResult<IndexResponse> transformedResult = new UniPaginatedResult<>(response, result.getPagination());
        return getResponseFactory().ok(transformedResult);
    }

    @GetMapping("/count")
    public ResponseEntity<UniResponse<Long>> count(
        Pageable pageable,
        @RequestParam(required = false) String q,
        @RequestParam(required = false) String cursor,
        @RequestParam(required = false) Map<String, Object> filters
    ) {
        long count = getService().count(q, filters, pageable, cursor);
        return getResponseFactory().ok(count);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UniResponse<DetailResponse>> findById(@PathVariable ID id) {
        T resource = getService().findById(id);
        DetailResponse response = getTransformer().toDetail(resource);
        return getResponseFactory().ok(response);
    }

    @PostMapping
    public ResponseEntity<UniResponse<CreateResponse>> create(@Valid @RequestBody CreateRequest createRequest) {
        T resource = getService().create(createRequest);
        CreateResponse response = getTransformer().toCreateResponse(resource);
        return getResponseFactory().created(URI.create("/"), response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UniResponse<UpdateResponse>> update(@PathVariable ID id, @RequestBody UpdateRequest updateRequest) {
        T resource = getService().update(id, updateRequest);
        UpdateResponse response = getTransformer().toUpdateResponse(resource);
        return getResponseFactory().ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<DeleteResponse> delete(@PathVariable ID id) {
        getService().deleteById(id);
        return ResponseEntity.noContent().build();
    }

}