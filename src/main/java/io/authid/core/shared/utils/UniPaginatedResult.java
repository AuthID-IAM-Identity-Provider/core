package io.authid.core.shared.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import java.util.List;

/**
 * DTO internal untuk membawa hasil dari Service ke Controller.
 */
@Data
@Getter
@AllArgsConstructor
public class UniPaginatedResult<T> {
    private final List<T> data;
    private final UniPagination pagination;
}