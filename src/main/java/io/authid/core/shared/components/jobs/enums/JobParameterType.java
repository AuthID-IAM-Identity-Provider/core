package io.authid.core.shared.components.jobs.enums;

import io.authid.core.shared.components.i18n.extractors.I18n;
import lombok.Getter;

@Getter
public enum JobParameterType {
    STRING(I18n.extract("job.parameter.type.string.label"), I18n.extract("job.parameter.type.string.description"), String.class, false, false),
    TEXT(I18n.extract("job.parameter.type.text.label"), I18n.extract("job.parameter.type.text.description"), String.class, false, false),
    CHAR(I18n.extract("job.parameter.type.char.label"), I18n.extract("job.parameter.type.char.description"), Character.class, false, false),
    BOOLEAN(I18n.extract("job.parameter.type.boolean.label"), I18n.extract("job.parameter.type.boolean.description"), Boolean.class, false, false),
    LONG(I18n.extract("job.parameter.type.long.label"), I18n.extract("job.parameter.type.long.description"), Long.class, false, false),
    INTEGER(I18n.extract("job.parameter.type.integer.label"), I18n.extract("job.parameter.type.integer.description"), Integer.class, false, false),
    DOUBLE(I18n.extract("job.parameter.type.double.label"), I18n.extract("job.parameter.type.double.description"), Double.class, false, false),
    DECIMAL(I18n.extract("job.parameter.type.decimal.label"), I18n.extract("job.parameter.type.decimal.description"), java.math.BigDecimal.class, false, false),
    DATE(I18n.extract("job.parameter.type.date.label"), I18n.extract("job.parameter.type.date.description"), java.time.LocalDate.class, false, false),
    TIME(I18n.extract("job.parameter.type.time.label"), I18n.extract("job.parameter.type.time.description"), java.time.LocalTime.class, false, false),
    DATETIME(I18n.extract("job.parameter.type.datetime.label"), I18n.extract("job.parameter.type.datetime.description"), java.time.LocalDateTime.class, false, false),
    TIMESTAMP(I18n.extract("job.parameter.type.timestamp.label"), I18n.extract("job.parameter.type.timestamp.description"), java.time.OffsetDateTime.class, false, false),
    JSON(I18n.extract("job.parameter.type.json.label"), I18n.extract("job.parameter.type.json.description"), String.class, true, true),
    UUID(I18n.extract("job.parameter.type.uuid.label"), I18n.extract("job.parameter.type.uuid.description"), java.util.UUID.class, false, false),
    ENUM(I18n.extract("job.parameter.type.enum.label"), I18n.extract("job.parameter.type.enum.description"), Enum.class, false, false),
    BINARY(I18n.extract("job.parameter.type.binary.label"), I18n.extract("job.parameter.type.binary.description"), byte[].class, false, false),
    LIST(I18n.extract("job.parameter.type.list.label"), I18n.extract("job.parameter.type.list.description"), java.util.List.class, true, true),
    MAP(I18n.extract("job.parameter.type.map.label"), I18n.extract("job.parameter.type.map.description"), java.util.Map.class, true, true);

    JobParameterType(String label, String description,Class<?> stringClass, boolean isComplex, boolean isCollectionLike) {
    }

    public boolean isString() {
        return this == STRING;
    }

    public boolean isText() {
        return this == TEXT;
    }

    public boolean isChar() {
        return this == CHAR;
    }

    public boolean isBoolean() {
        return this == BOOLEAN;
    }

    public boolean isLong() {
        return this == LONG;
    }

    public boolean isInteger() {
        return this == INTEGER;
    }

    public boolean isDouble() {
        return this == DOUBLE;
    }

    public boolean isDecimal() {
        return this == DECIMAL;
    }

    public boolean isDate() {
        return this == DATE;
    }

    public boolean isTime() {
        return this == TIME;
    }

    public boolean isDatetime() {
        return this == DATETIME;
    }

    public boolean isTimestamp() {
        return this == TIMESTAMP;
    }

    public boolean isJson() {
        return this == JSON;
    }

    public boolean isUuid() {
        return this == UUID;
    }

    public boolean isEnum() {
        return this == ENUM;
    }

    public boolean isBinary() {
        return this == BINARY;
    }

    public boolean isList() {
        return this == LIST;
    }

    public boolean isMap() {
        return this == MAP;
    }

    public boolean canTransitionTo(JobParameterType newType){
        return switch (this) {
            case STRING -> newType == TEXT || newType == JSON;
            case TEXT -> newType == STRING || newType == JSON;
            case CHAR, ENUM, UUID -> newType == STRING;
            case BOOLEAN -> false; // Booleans typically don't transition to other types
            case LONG -> newType == INTEGER || newType == DOUBLE || newType == DECIMAL || newType == STRING;
            case INTEGER -> newType == LONG || newType == DOUBLE || newType == DECIMAL || newType == STRING;
            case DOUBLE -> newType == DECIMAL || newType == STRING;
            case DECIMAL -> newType == DOUBLE || newType == STRING;
            case DATE, TIME -> newType == DATETIME || newType == TIMESTAMP || newType == STRING;
            case DATETIME -> newType == DATE || newType == TIME || newType == TIMESTAMP || newType == STRING;
            case TIMESTAMP -> newType == DATE || newType == TIME || newType == DATETIME || newType == STRING;
            case JSON -> newType == STRING || newType == TEXT || newType == LIST || newType == MAP;
            case BINARY -> false; // Binary data typically doesn't transition
            case LIST, MAP -> newType == JSON || newType == STRING;
            default -> false;
        };
    }
    public static JobParameterType getDefault() {
        return STRING;
    }
}
