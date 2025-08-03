package io.authid.core.containers.user.enums;// GENERATED FILE - DO NOT MODIFY MANUALLY
// Generated from Excel Master Data: {0}

import io.authid.core.shared.components.exception.contracts.ErrorCatalog;
import io.authid.core.shared.components.i18n.annotations.ExtractableI18n;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;
import io.authid.core.shared.components.i18n.extractors.I18n;

@Getter
public enum UserErrorCatalog implements ErrorCatalog {
    @ExtractableI18n(value = "error.user.not.found", suffixes = {".title", ".debug", ".cause", ".action"})
    USER_NOT_FOUND("RES-USR-0001", "Resource", "User", HttpStatus.NOT_FOUND, ""),

    @ExtractableI18n(value = "error.user.already.exists", suffixes = {".title", ".debug", ".cause", ".action"})
    USER_ALREADY_EXISTS("RES-USR-0002", "Resource", "User", HttpStatus.CONFLICT, ""),

    @ExtractableI18n(value = "error.user.inactive", suffixes = {".title", ".debug", ".cause", ".action"})
    USER_INACTIVE("BUS-USR-0001", "Business", "User", HttpStatus.FORBIDDEN, ""),

    @ExtractableI18n(value = "error.user.account.locked", suffixes = {".title", ".debug", ".cause", ".action"})
    USER_ACCOUNT_LOCKED("AUTH-LOG-0001", "Authentication", "Login", HttpStatus.FORBIDDEN, ""),

    @ExtractableI18n(value = "error.user.suspended", suffixes = {".title", ".debug", ".cause", ".action"})
    USER_ACCOUNT_SUSPENDED("AUTHZ-USR-0001", "Authorization", "User", HttpStatus.FORBIDDEN, ""),

    @ExtractableI18n(value = "error.user.banned", suffixes = {".title", ".debug", ".cause", ".action"})
    USER_ACCOUNT_BANNED("AUTHZ-USR-0002", "Authorization", "User", HttpStatus.FORBIDDEN, ""),

    @ExtractableI18n(value = "error.email.not.verified", suffixes = {".title", ".debug", ".cause", ".action"})
    EMAIL_NOT_VERIFIED("AUTH-SEC-0001", "Authentication", "Security", HttpStatus.FORBIDDEN, ""),

    @ExtractableI18n(value = "error.password.mismatch", suffixes = {".title", ".debug", ".cause", ".action"})
    PASSWORD_MISMATCH("AUTH-SEC-0002", "Authentication", "Security", HttpStatus.BAD_REQUEST, ""),

    @ExtractableI18n(value = "validation.password.weak", suffixes = {".title", ".debug", ".cause", ".action"})
    PASSWORD_WEAK_DETECTED("VAL-SEC-0001", "Validation", "Security", HttpStatus.BAD_REQUEST, ""),

    @ExtractableI18n(value = "error.two.factor.required", suffixes = {".title", ".debug", ".cause", ".action"})
    TWO_FACTOR_REQUIRED("AUTH-SEC-0003", "Authentication", "Security", HttpStatus.FORBIDDEN, ""),

    @ExtractableI18n(value = "error.user.not.found", suffixes = {".title", ".debug", ".cause", ".action"})
    TWO_FACTOR_NOT_CONFIRMED("AUTH-SEC-0004", "Authentication", "Security", HttpStatus.FORBIDDEN, ""),

    @ExtractableI18n(value = "error.profile.incomplete", suffixes = {".title", ".debug", ".cause", ".action"})
    PROFILE_INCOMPLETE("BUS-USR-0002", "Business", "User", HttpStatus.BAD_REQUEST, ""),

    @ExtractableI18n(value = "error.user.not.deletable", suffixes = {".title", ".debug", ".cause", ".action"})
    USER_NOT_DELETABLE("BUS-USR-0003", "Business", "User", HttpStatus.BAD_REQUEST, ""),

    @ExtractableI18n(value = "error.invalid.user.status.transition", suffixes = {".title", ".debug", ".cause", ".action"})
    INVALID_USER_STATUS_TRANSITION("BUS-USR-0004", "Business", "User", HttpStatus.BAD_REQUEST, "")    ; // End of enum entries


    private final String code;
    private final String category;
    private final String module;
    private String baseMessageKey;
    private final HttpStatus httpStatus;
    private final List<String> additionalInfoVisibility;

    UserErrorCatalog(String code, String category, String module, HttpStatus httpStatus, String additionalInfoVisibilityString) {
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
            // Tangani exception, meskipun ini sangat jarang terjadi
            throw new RuntimeException("Failed to reflect on enum constant: " + this.name(), e);
        }
    }

    private List<String> parseVisibility(String visibilityStr) {
        if ("ALL".equalsIgnoreCase(visibilityStr)) {
            return List.of("DEV", "SIT", "UAT", "BET", "PROD");
        } else if (visibilityStr != null && !visibilityStr.isEmpty()) {
            return Arrays.stream(visibilityStr.toUpperCase().split(";")).map(String::trim).collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }
}
