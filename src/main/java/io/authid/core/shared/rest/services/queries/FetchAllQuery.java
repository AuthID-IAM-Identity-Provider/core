package io.authid.core.shared.rest.services.queries;

import org.springframework.data.domain.Pageable;

import java.util.Map;

/**
 * The Query object for fetch all entity
 *
 * @param searchTerm The query search term
 * @param filters    The available filters
 * @param pageable   The pagination object
 * @param cursor     The cursor oject
 */
public record FetchAllQuery(
    String searchTerm, Map<String, Object> filters, Pageable pageable, String cursor
) {
}
