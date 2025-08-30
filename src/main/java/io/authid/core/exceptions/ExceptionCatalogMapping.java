package io.authid.core.exceptions; // Atau package yang sesuai

import io.authid.core.exceptions.enums.SystemErrorCatalog;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.net.ConnectException;
import java.util.Map;
import java.util.Optional;

public final class ExceptionCatalogMapping {

    private ExceptionCatalogMapping() {}

    private static final Map<Class<? extends Throwable>, SystemErrorCatalog> MAPPINGS = Map.ofEntries(
        // --- Spring Web & MVC ---
        Map.entry(NoResourceFoundException.class, SystemErrorCatalog.ROUTE_NOT_FOUND), // 404
        Map.entry(HttpRequestMethodNotSupportedException.class, SystemErrorCatalog.METHOD_NOT_SUPPORTED), // 405
        Map.entry(HttpMessageNotReadableException.class, SystemErrorCatalog.MALFORMED_REQUEST), // 400
        Map.entry(MethodArgumentTypeMismatchException.class, SystemErrorCatalog.MALFORMED_REQUEST), // 400

        // --- Spring Data & Transaction ---
        Map.entry(DataIntegrityViolationException.class, SystemErrorCatalog.DATA_INTEGRITY_VIOLATION), // 409
        Map.entry(OptimisticLockingFailureException.class, SystemErrorCatalog.CONCURRENCY_FAILURE), // 409
        Map.entry(PessimisticLockingFailureException.class, SystemErrorCatalog.CONCURRENCY_FAILURE), // 409
        Map.entry(CannotCreateTransactionException.class, SystemErrorCatalog.DATABASE_CONNECTION_ERROR), // 503

        // --- Spring Security ---
        Map.entry(AccessDeniedException.class, SystemErrorCatalog.ACCESS_DENIED), // 403
        Map.entry(AuthenticationException.class, SystemErrorCatalog.UNAUTHENTICATED), // 401

        // --- Jakarta Validation ---
        Map.entry(ConstraintViolationException.class, SystemErrorCatalog.VALIDATION_ERROR), // 400

        // --- General Java / Network ---
        Map.entry(ConnectException.class, SystemErrorCatalog.DATABASE_CONNECTION_ERROR), // 503
        Map.entry(IllegalArgumentException.class, SystemErrorCatalog.MALFORMED_REQUEST) // 400
    );

    /**
     * Mencari SystemErrorCatalog yang cocok untuk Throwable yang diberikan.
     * @param throwable Exception yang terjadi.
     * @return Optional yang berisi SystemErrorCatalog jika mapping ditemukan.
     */
    public static Optional<SystemErrorCatalog> getMappedCatalog(Throwable throwable) {
        if (throwable == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(MAPPINGS.get(throwable.getClass()));
    }
}