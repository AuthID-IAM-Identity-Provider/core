package io.authid.core.shared.rest.specifications;

import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.*;
import jakarta.persistence.metamodel.Attribute;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GenericSpecificationBuilder<T> {

    private final List<String> searchableColumns;
    private final List<String> filterableColumns;

    public GenericSpecificationBuilder(List<String> searchableColumns, List<String> filterableColumns) {
        this.searchableColumns = searchableColumns;
        this.filterableColumns = filterableColumns;
    }

    public Specification<T> build(String searchTerm, Map<String, Object> filters) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // --- LOGIKA SEARCH DENGAN DEFAULT ---
            if (searchTerm != null && !searchTerm.isBlank()) {
                List<Predicate> searchPredicates = new ArrayList<>();
                boolean searchSpecificColumns = (searchableColumns != null && !searchableColumns.isEmpty());

                if (searchSpecificColumns) {
                    // Perilaku lama: cari di kolom yang spesifik
                    for (String column : searchableColumns) {
                        searchPredicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get(column)), "%" + searchTerm.toLowerCase() + "%"));
                    }
                } else {
                    // Perilaku DEFAULT BARU: cari di SEMUA kolom bertipe String
                    for (Attribute<? super T, ?> attribute : root.getModel().getAttributes()) {
                        if (attribute.getJavaType().equals(String.class)) {
                            Expression<String> stringExpression = root.get(attribute.getName());
                            searchPredicates.add(criteriaBuilder.like(criteriaBuilder.lower(stringExpression), "%" + searchTerm.toLowerCase() + "%"));
                        }
                    }
                }
                if (!searchPredicates.isEmpty()) {
                    predicates.add(criteriaBuilder.or(searchPredicates.toArray(new Predicate[0])));
                }
            }

            // --- LOGIKA FILTER DENGAN DEFAULT ---
            if (filters != null && !filters.isEmpty()) {
                boolean allowAllFilters = (filterableColumns == null || filterableColumns.isEmpty());

                for (Map.Entry<String, Object> entry : filters.entrySet()) {
                    // Jika default (semua diizinkan) ATAU kolom ini ada di daftar yang diizinkan
                    if (allowAllFilters || filterableColumns.contains(entry.getKey())) {
                        try {
                            predicates.add(criteriaBuilder.equal(root.get(entry.getKey()), entry.getValue()));
                        } catch (IllegalArgumentException e) {
                            // Abaikan jika filter key tidak sesuai dengan nama kolom di entitas
                            System.err.println("Warning: Filter key '" + entry.getKey() + "' is not a valid attribute.");
                        }
                    }
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}