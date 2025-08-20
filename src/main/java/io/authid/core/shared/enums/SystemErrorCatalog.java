// src/main/java/io/authid/core/shared/enums/SystemErrorCatalog.java
package io.authid.core.shared.enums;

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

    // --- Error Level Rendah Baru ---

    @ExtractableI18n(value = "error.system.internal.server.error", suffixes = {".title", ".debug", ".cause", ".action"})
    INTERNAL_SERVER_ERROR("SYS-0001", "System", "Core", HttpStatus.INTERNAL_SERVER_ERROR, "ALL"),

    @ExtractableI18n(value = "error.system.database.error", suffixes = {".title", ".debug", ".cause", ".action"})
    DATABASE_ERROR("SYS-0101", "Infrastructure", "Database", HttpStatus.INTERNAL_SERVER_ERROR, "ALL"), // Masalah DB umum

    @ExtractableI18n(value = "error.system.network.error", suffixes = {".title", ".debug", ".cause", ".action"})
    NETWORK_ERROR("SYS-0102", "Infrastructure", "Network", HttpStatus.SERVICE_UNAVAILABLE, "ALL"), // Masalah konektivitas jaringan

    @ExtractableI18n(value = "error.system.io.error", suffixes = {".title", ".debug", ".cause", ".action"})
    IO_ERROR("SYS-0103", "Infrastructure", "IO", HttpStatus.INTERNAL_SERVER_ERROR, "ALL"), // Masalah input/output (file, stream)

    @ExtractableI18n(value = "error.system.third.party.integration.error", suffixes = {".title", ".debug", ".cause", ".action"})
    THIRD_PARTY_INTEGRATION_ERROR("SYS-0104", "Integration", "External", HttpStatus.SERVICE_UNAVAILABLE, "ALL"), // Masalah API eksternal

    @ExtractableI18n(value = "error.system.configuration.error", suffixes = {".title", ".debug", ".cause", ".action"})
    CONFIGURATION_ERROR("SYS-0105", "Infrastructure", "Configuration", HttpStatus.INTERNAL_SERVER_ERROR, "DEV;SIT"), // Masalah konfigurasi (mungkin hanya terlihat di non-prod)

    @ExtractableI18n(value = "error.system.route.not.found", suffixes = {".title", ".debug", ".cause", ".action"})
    ROUTE_NOT_FOUND("SYS-0106", "System", "Routing", HttpStatus.NOT_FOUND, "ALL"), // Kode baru, kategori Routing

    @ExtractableI18n(value = "error.system.null.pointer.exception", suffixes = {".title", ".debug", ".cause", ".action"})
    NULL_POINTER_EXCEPTION("SYS-0107", "System", "Runtime", HttpStatus.INTERNAL_SERVER_ERROR, "ALL"), // Kode baru, kategori Routing

    @ExtractableI18n(value = "error.system.resource.not.found", suffixes = {".title", ".debug", ".cause", ".action"})
    RESOURCE_NOT_FOUND("SYS-0107", "System", "Runtime", HttpStatus.NOT_FOUND, "ALL");

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