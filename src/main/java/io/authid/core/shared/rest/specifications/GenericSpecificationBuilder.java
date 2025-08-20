package io.authid.core.shared.rest.specifications;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.metamodel.Attribute;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public record GenericSpecificationBuilder<T>(List<String> searchableColumns, List<String> filterableColumns) {

    public Specification<T> build(String searchTerm, Map<String, Object> filters) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Search Predicates
            buildSearchPredicate(searchTerm, root, criteriaBuilder)
                .ifPresent(predicates::add);

            // 2. Filter Predicates
            buildFilterPredicates(filters, root, criteriaBuilder)
                .forEach(predicates::add);

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Optional<Predicate> buildSearchPredicate(String searchTerm, Root<T> root, CriteriaBuilder criteriaBuilder) {
        if (searchTerm == null || searchTerm.isBlank()) {
            return Optional.empty();
        }

        boolean useSpecificColumns = searchableColumns != null && !searchableColumns.isEmpty();
        Stream<String> columnStream = useSpecificColumns
            ? searchableColumns.stream()
            : root.getModel().getAttributes().stream()
            .filter(attr -> attr.getJavaType().equals(String.class))
            .map(Attribute::getName);

        List<Predicate> predicates = columnStream
            .map(column -> criteriaBuilder.like(criteriaBuilder.lower(root.get(column)), "%" + searchTerm.toLowerCase() + "%"))
            .toList();

        return Optional.of(predicates)
            .filter(list -> !list.isEmpty())
            .map(list -> criteriaBuilder.or(list.toArray(new Predicate[0])));
    }

    private Stream<Predicate> buildFilterPredicates(Map<String, Object> filters, Root<T> root, CriteriaBuilder criteriaBuilder) {
        if (filters == null || filters.isEmpty()) {
            return Stream.empty();
        }

        boolean allowAllFilters = filterableColumns == null || filterableColumns.isEmpty();

        return filters.entrySet().stream()
            .filter(entry -> allowAllFilters || filterableColumns.contains(entry.getKey()))
            .flatMap(entry -> {
                try {
                    Predicate predicate = criteriaBuilder.equal(root.get(entry.getKey()), entry.getValue());
                    return Stream.of(predicate);
                } catch (IllegalArgumentException e) {
                    System.err.println("Warning: Filter key '" + entry.getKey() + "' is not a valid attribute.");
                    return Stream.empty();
                }
            });
    }
}