package io.authid.core.shared.rest.controllers;

import io.authid.core.shared.rest.contracts.RestService;
import io.authid.core.shared.utils.UniMeta;
import io.authid.core.shared.utils.UniPagination;
import io.authid.core.shared.utils.UniPaginationType;
import io.authid.core.shared.utils.UniResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public abstract class RestController<T, ID, C, U> {
    public abstract RestService<T, ID, C, U> getService();

    // --- Endpoint Handler ---
    @GetMapping
    public ResponseEntity<UniResponse<List<T>>> findAll(
            Pageable pageable,
            @RequestParam(required = false) Map<String, Object> filters) {
        Page<T> page = getService().findAll(pageable, filters);
        return paginated(page, Function.identity());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UniResponse<T>> findById(@PathVariable ID id) {
        T resource = getService().findById(id);
        return ok(resource);
    }

    @PostMapping
    public ResponseEntity<UniResponse<T>> create(@RequestBody C createRequest) {
        T resource = getService().create(createRequest);
        return created(resource);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UniResponse<T>> update(@PathVariable ID id, @RequestBody U updateRequest) {
        T resource = getService().update(id, updateRequest);
        return ok(resource);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable ID id) {
        getService().deleteById(id);
        return ResponseEntity.noContent().build();
    }

    protected ResponseEntity<UniResponse<T>> ok(T data) {
        return ResponseEntity.ok(
                UniResponse.success(HttpStatus.OK.value(), "Success", data, createMeta(null))
        );
    }

    protected ResponseEntity<UniResponse<T>> created(T data) {
        return new ResponseEntity<>(
                UniResponse.success(HttpStatus.CREATED.value(), "Resource created successfully", data, createMeta(null)),
                HttpStatus.CREATED
        );
    }

    protected <E> ResponseEntity<UniResponse<List<E>>> paginated(Page<E> page, Function<E, E> converter) {
        UniPagination pagination = UniPagination.builder()
                .type(UniPaginationType.LENGTH_AWARE)
                .page(page.getNumber() + 1)
                .perPage(page.getSize())
                .totalPages(page.getTotalPages())
                .totalItems((int) page.getTotalElements())
                .build();

        List<E> data = page.getContent().stream().map(converter).toList();

        return ResponseEntity.ok(
                UniResponse.success(HttpStatus.OK.value(), "Success", data, createMeta(pagination))
        );
    }

    protected UniMeta createMeta(UniPagination pagination) {
        return UniMeta.builder()
                .timestamp(Instant.parse(Instant.now().toString()))
                .requestId(UUID.randomUUID().toString())
                .build();
    }
}