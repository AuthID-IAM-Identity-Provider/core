package io.authid.core.containers.user.enums;// GENERATED FILE - DO NOT MODIFY MANUALLY
// Generated from Excel Master Data: {0}

import org.springframework.http.HttpStatus;

import java.util.*;
import java.util.stream.Collectors;
import io.authid.core.shared.components.i18n.extractors.I18n;

public enum UserErrorCatalog {
    USER_NOT_FOUND("RES-USR-0001", "Resource", "User", "error.user.not.found", HttpStatus.NOT_FOUND, ""),
    USER_ALREADY_EXISTS("RES-USR-0002", "Resource", "User", "error.user.already.exists", HttpStatus.CONFLICT, ""),
    USER_INACTIVE("BUS-USR-0001", "Business", "User", "error.user.inactive", HttpStatus.FORBIDDEN, ""),
    USER_ACCOUNT_LOCKED("AUTH-LOG-0001", "Authentication", "Login", "error.user.account.locked", HttpStatus.FORBIDDEN, ""),
    USER_ACCOUNT_SUSPENDED("AUTHZ-USR-0001", "Authorization", "User", "error.user.suspended", HttpStatus.FORBIDDEN, ""),
    USER_ACCOUNT_BANNED("AUTHZ-USR-0002", "Authorization", "User", "error.user.banned", HttpStatus.FORBIDDEN, ""),
    EMAIL_NOT_VERIFIED("AUTH-SEC-0001", "Authentication", "Security", "error.email.not.verified", HttpStatus.FORBIDDEN, ""),
    PASSWORD_MISMATCH("AUTH-SEC-0002", "Authentication", "Security", "error.password.mismatch", HttpStatus.BAD_REQUEST, ""),
    PASSWORD_WEAK_DETECTED("VAL-SEC-0001", "Validation", "Security", "validation.password.weak", HttpStatus.BAD_REQUEST, ""),
    TWO_FACTOR_REQUIRED("AUTH-SEC-0003", "Authentication", "Security", "error.two.factor.required", HttpStatus.FORBIDDEN, ""),
    TWO_FACTOR_NOT_CONFIRMED("AUTH-SEC-0004", "Authentication", "Security", "error.two.factor.not.confirmed", HttpStatus.FORBIDDEN, ""),
    PROFILE_INCOMPLETE("BUS-USR-0002", "Business", "User", "error.profile.incomplete", HttpStatus.BAD_REQUEST, ""),
    USER_NOT_DELETABLE("BUS-USR-0003", "Business", "User", "error.user.not.deletable", HttpStatus.BAD_REQUEST, ""),
    INVALID_USER_STATUS_TRANSITION("BUS-USR-0004", "Business", "User", "error.invalid.user.status.transition", HttpStatus.BAD_REQUEST, "")    ; // End of enum entries

    private final String code;
    private final String category;
    private final String module;
    private final String baseMessageKey;
    private final HttpStatus httpStatus;
    private final List<String> additionalInfoVisibility;
    private final String titleKey;
    private final String debugDescriptionKey;
    private final String causeKey;
    private final String actionKey;

    UserErrorCatalog(String code, String category, String module, String baseMessageKey, HttpStatus httpStatus, String additionalInfoVisibilityString) {
        this.code = code;
        this.category = category;
        this.module = module;
        this.baseMessageKey = baseMessageKey;
        this.httpStatus = httpStatus;
        this.additionalInfoVisibility = parseVisibility(additionalInfoVisibilityString);

        I18n.setSourceClass(UserErrorCatalog.class);
        this.titleKey = I18n.extract(baseMessageKey + ".title");
        this.debugDescriptionKey = I18n.extract(baseMessageKey + ".debug");
        this.causeKey = I18n.extract(baseMessageKey + ".cause");
        this.actionKey = I18n.extract(baseMessageKey + ".action");
    }

    public String getCode() { return code; }
    public String getCategory() { return category; }
    public String getModule() { return module; }
    public String getBaseMessageKey() { return baseMessageKey; }
    public HttpStatus getHttpStatus() { return httpStatus; }
    public List<String> getAdditionalInfoVisibility() { return additionalInfoVisibility; }
    public String getMessageKey() { return baseMessageKey; }
    public String getTitleKey() { return titleKey; }
    public String getDebugDescriptionKey() { return debugDescriptionKey; }
    public String getCauseKey() { return causeKey; }
    public String getActionKey() { return actionKey; }

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
