package io.authid.core.shared.components.i18n.configurations; // Atau di package lain yang sesuai

import io.authid.core.shared.components.i18n.extractors.I18n;
import jakarta.validation.MessageInterpolator;

import java.util.Locale;

public record CustomMessageInterpolator(MessageInterpolator defaultInterpolator) implements MessageInterpolator {
    @Override
    public String interpolate(String messageTemplate, Context context) {
        if (messageTemplate.startsWith("{") && messageTemplate.endsWith("}")) {
            return defaultInterpolator.interpolate(messageTemplate, context);
        }
        return I18n.extract(messageTemplate);
    }

    @Override
    public String interpolate(String messageTemplate, Context context, Locale locale) {
        if (messageTemplate.startsWith("{") && messageTemplate.endsWith("}")) {
            return defaultInterpolator.interpolate(messageTemplate, context, locale);
        }
        return I18n.extract(messageTemplate);
    }
}