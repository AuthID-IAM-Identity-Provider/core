package io.authid.core.shared.rest.controllers;

import io.authid.core.shared.rest.contracts.RestService;
import io.authid.core.shared.rest.transformer.RestTransformer;
import io.authid.core.shared.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public abstract class RestController<T, ID, CreateRequest, UpdateRequest, DeleteRequest, IndexResponse, DetailResponse, CreateResponse, UpdateResponse, DeleteResponse> {

    private static final int PAGINATION_THRESHOLD = 100;

    public abstract RestService<T, ID, CreateRequest, UpdateRequest> getService();

    public abstract RestTransformer<T, IndexResponse, DetailResponse, CreateResponse, UpdateResponse, DeleteResponse> getTransformer();

    @GetMapping
    public ResponseEntity<UniResponse<List<IndexResponse>>> findAll(
            Pageable pageable,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Map<String, Object> filters
    ) {

        filters.remove("q");
        filters.remove("cursor");
        filters.remove("page");
        filters.remove("size");

        boolean useCursor = (cursor != null) || (pageable.isPaged() && pageable.getPageNumber() >= PAGINATION_THRESHOLD);
        UniPaginatedResult<T> result = getService().findAll(q, filters, pageable, useCursor ? cursor : null);

        // Transform each raw entity (T) to IndexResponse
        List<IndexResponse> response = result
                .getData()
                .stream()
                .map(getTransformer()::toIndex) // Apply the toIndex transformation
                .collect(Collectors.toList());

        return UniResponseFactory.ok(response, "Success"); // Pass the transformed UniPaginatedResult
    }

    @GetMapping("/count")
    public ResponseEntity<UniResponse<Long>> count(
            Pageable pageable,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Map<String, Object> filters
    ) {
        long count = getService().count(q, filters, pageable, cursor);
        return UniResponseFactory.ok(count, "Success fetch all");
    }

    @GetMapping("/{id}")
    public ResponseEntity<UniResponse<DetailResponse>> findById(@PathVariable ID id) {
        T resource = getService().findById(id);
        DetailResponse response = getTransformer().toDetail(resource);
        return UniResponseFactory.ok(response, "Successfully get by ID");
    }

    @PostMapping
    public ResponseEntity<UniResponse<CreateResponse>> create(@RequestBody CreateRequest createRequest) {
        T resource = getService().create(createRequest);
        CreateResponse response = getTransformer().toCreateResponse(resource);
        return UniResponseFactory.created(URI.create("/"), response, "Create Success");
    }

    @PutMapping("/{id}")
    public ResponseEntity<UniResponse<UpdateResponse>> update(@PathVariable ID id, @RequestBody UpdateRequest updateRequest) {
        T resource = getService().update(id, updateRequest);
        UpdateResponse response = getTransformer().toUpdateResponse(resource);
        return UniResponseFactory.ok(response, "Update Success");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<DeleteResponse> delete(@PathVariable ID id) {
        getService().deleteById(id);
        return ResponseEntity.noContent().build();
    }
}