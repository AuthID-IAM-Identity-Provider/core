// src/main/java/io/authid/core/shared/enums/SystemErrorCatalog.java
package io.authid.core.exceptions.enums;

import io.authid.core.shared.components.exception.contracts.ErrorCatalog;
import io.authid.core.shared.components.i18n.annotations.ExtractableI18n;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public enum SystemErrorCatalog implements ErrorCatalog {

    // --- GENERIC SYSTEM ERRORS (SYS) ---
    @ExtractableI18n(value = "error.system.internal.server", suffixes = {".title", ".cause", ".action"})
    INTERNAL_SERVER_ERROR("SYS-5001", "System", "Core", HttpStatus.INTERNAL_SERVER_ERROR, "ALL"),

    @ExtractableI18n(value = "error.system.service.unavailable", suffixes = {".title", ".cause", ".action"})
    SERVICE_UNAVAILABLE("SYS-5031", "System", "Core", HttpStatus.SERVICE_UNAVAILABLE, "ALL"),

    // --- REQUEST & ROUTING ERRORS (REQ) ---
    @ExtractableI18n(value = "error.request.not.found", suffixes = {".title", ".cause", ".action"})
    ROUTE_NOT_FOUND("REQ-4041", "Request", "Routing", HttpStatus.NOT_FOUND, "ALL"),

    @ExtractableI18n(value = "error.request.method.not.supported", suffixes = {".title", ".cause", ".action"})
    METHOD_NOT_SUPPORTED("REQ-4051", "Request", "Routing", HttpStatus.METHOD_NOT_ALLOWED, "ALL"),

    @ExtractableI18n(value = "error.request.malformed", suffixes = {".title", ".cause", ".action"})
    MALFORMED_REQUEST("REQ-4001", "Request", "Validation", HttpStatus.BAD_REQUEST, "ALL"),

    // --- VALIDATION ERRORS (VAL) ---
    @ExtractableI18n(value = "error.validation.general", suffixes = {".title", ".cause", ".action"})
    VALIDATION_ERROR("VAL-4001", "Validation", "Core", HttpStatus.BAD_REQUEST, "ALL"),

    // --- DATA ACCESS & PERSISTENCE ERRORS (DAT) ---
    @ExtractableI18n(value = "error.data.integrity.violation", suffixes = {".title", ".cause", ".action"})
    DATA_INTEGRITY_VIOLATION("DAT-4091", "Data", "Persistence", HttpStatus.CONFLICT, "ALL"),

    @ExtractableI18n(value = "error.data.concurrency.failure", suffixes = {".title", ".cause", ".action"})
    CONCURRENCY_FAILURE("DAT-4092", "Data", "Persistence", HttpStatus.CONFLICT, "ALL"),

    @ExtractableI18n(value = "error.data.cannot.connect", suffixes = {".title", ".cause", ".action"})
    DATABASE_CONNECTION_ERROR("DAT-5031", "Data", "Persistence", HttpStatus.SERVICE_UNAVAILABLE, "ALL"),
    
    @ExtractableI18n(value = "error.data.resource.not.found", suffixes = {".title", ".cause", ".action"})
    DATA_RESOURCE_NOT_FOUND("DAT-4041", "Data", "Persistence", HttpStatus.NOT_FOUND, "ALL"), // Untuk bisnis logic not found

    // --- SECURITY ERRORS (SEC) ---
    @ExtractableI18n(value = "error.security.unauthenticated", suffixes = {".title", ".cause", ".action"})
    UNAUTHENTICATED("SEC-4011", "Security", "Authentication", HttpStatus.UNAUTHORIZED, "ALL"),
    
    @ExtractableI18n(value = "error.security.access.denied", suffixes = {".title", ".cause", ".action"})
    ACCESS_DENIED("SEC-4031", "Security", "Authorization", HttpStatus.FORBIDDEN, "ALL");

    private final String code;
    private final String category;
    private final String module;
    private String baseMessageKey;
    private final HttpStatus httpStatus;
    private final List<String> additionalInfoVisibility;

    SystemErrorCatalog(String code, String category, String module, HttpStatus httpStatus, String additionalInfoVisibilityString) {
        this.code = code;
        this.category = category;
        this.module = module;
        this.httpStatus = httpStatus;
        this.additionalInfoVisibility = parseVisibility(additionalInfoVisibilityString);
        this.extractBaseMessageKey();
    }

    private void extractBaseMessageKey(){
        try {
            Field field = this.getClass().getField(this.name());
            ExtractableI18n annotation = field.getAnnotation(ExtractableI18n.class);
            if (annotation != null) {
                this.baseMessageKey = annotation.value();
            } else {
                this.baseMessageKey = "error.annotation.missing";
            }
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("Failed to reflect on enum constant: " + this.name(), e);
        }
    }

    private List<String> parseVisibility(String visibilityStr) {
        return getStrings(visibilityStr);
    }

    public static List<String> getStrings(String visibilityStr) {
        if ("ALL".equalsIgnoreCase(visibilityStr)) {
            return List.of("DEV", "SIT", "UAT", "BET", "PROD");
        } else if (visibilityStr != null && !visibilityStr.isEmpty()) {
            return Arrays.stream(visibilityStr.toUpperCase().split(";")).map(String::trim).collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getBaseMessageKey() {
        return baseMessageKey;
    }
}