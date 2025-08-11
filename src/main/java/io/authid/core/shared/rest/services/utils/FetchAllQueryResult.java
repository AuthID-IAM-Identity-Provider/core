package io.authid.core.shared.rest.services.utils; // Pindahkan ke package utilitas

import io.authid.core.shared.utils.UniPaginatedResult;
import io.authid.core.shared.utils.UniPagination;
import io.authid.core.shared.utils.UniPaginationType;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/**
 * Kelas utilitas (utility class) untuk membangun objek UniPaginatedResult.
 * Hanya berisi metode statis dan tidak bisa diinstansiasi.
 */
public final class FetchAllQueryResult {

    /**
     * Constructor private untuk mencegah pembuatan instance dari kelas utilitas ini.
     */
    private FetchAllQueryResult() {
    }

    /**
     * Membangun hasil paginasi dari objek Page (untuk paginasi offset/length-aware).
     *
     * @param page Objek Page dari Spring Data.
     * @return Hasil paginasi yang terstruktur.
     */
    public static <T> UniPaginatedResult<T> lengthAware(Page<T> page) {
        UniPagination pagination = UniPagination.builder()
            .type(UniPaginationType.LENGTH_AWARE)
            .page(page.getNumber() + 1).perPage(page.getSize())
            .totalPages(page.getTotalPages()).totalItems((int) page.getTotalElements())
            .build();

        return buildResult(page.getContent(), pagination);
    }

    /**
     * Membangun hasil paginasi dari List (untuk paginasi cursor).
     *
     * @param results              List data dari database.
     * @param pageSize             Ukuran halaman yang diminta.
     * @param cursorValueExtractor Fungsi untuk mengekstrak nilai cursor dari entitas.
     * @return Hasil paginasi yang terstruktur.
     */
    public static <T> UniPaginatedResult<T> cursorResult(List<T> results, int pageSize, Function<T, String> cursorValueExtractor) {
        boolean hasMore = results.size() > pageSize;
        List<T> data = hasMore ? results.subList(0, pageSize) : results;
        String nextCursor = (hasMore && !data.isEmpty()) ? cursorValueExtractor.apply(data.get(data.size() - 1)) : null;

        UniPagination pagination = UniPagination.builder()
            .type(UniPaginationType.CURSOR)
            .perPage(pageSize).hasMore(hasMore).nextCursor(nextCursor)
            .build();

        return buildResult(data, pagination);
    }

    /**
     * Metode helper private untuk membangun hasil akhir, menghindari duplikasi.
     */
    private static <T> UniPaginatedResult<T> buildResult(List<T> data, UniPagination pagination) {
        return new UniPaginatedResult<>(data, pagination);
    }
}