package io.authid.core.shared.components.i18n.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.stereotype.Component;
import org.springframework.context.i18n.LocaleContextHolder; // Import this for default locale
import java.util.Locale;

@Component
public class I18nService {

    private final MessageSource messageSource;

    @Autowired
    public I18nService(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /**
     * Translates a message key using the specified locale.
     * If the message key is not found, the key itself is returned.
     *
     * @param key The message key to translate.
     * @param locale The locale to use for translation.
     * @param args Optional arguments for message formatting.
     * @return The translated message, or the key if not found.
     */
    // This is the original method. No @Override here as it's not overriding a superclass method.
    public String translate(String key, Locale locale, Object... args) {
        try {
            return messageSource.getMessage(key, args, locale);
        } catch (NoSuchMessageException e) {
            return key; // Fallback: return the key itself
        }
    }

    /**
     * Translates a message key using the default message if not found, and the specified locale.
     *
     * @param key The message key to translate.
     * @param defaultMessage The default message to return if the key is not found.
     * @param locale The locale to use for translation.
     * @param args Optional arguments for message formatting.
     * @return The translated message, or the default message if the key is not found.
     */
    public String translate(String key, String defaultMessage, Locale locale, Object... args) {
        return messageSource.getMessage(key, args, defaultMessage, locale);
    }

    /**
     * Translates a message key using the locale from LocaleContextHolder.
     * If the message key is not found, the key itself is returned.
     *
     * @param key The message key to translate.
     * @param args Optional arguments for message formatting.
     * @return The translated message, or the key if not found.
     */
    public String translate(String key, Object... args) {
        return translate(key, LocaleContextHolder.getLocale(), args); // Delegates to the specific locale method
    }

    /**
     * Translates a message key using the default message if not found, and the locale from LocaleContextHolder.
     *
     * @param key The message key to translate.
     * @param defaultMessage The default message to return if the key is not found.
     * @param args Optional arguments for message formatting.
     * @return The translated message, or the default message if the key is not found.
     */
    public String translate(String key, String defaultMessage, Object... args) {
        return messageSource.getMessage(key, args, defaultMessage, LocaleContextHolder.getLocale());
    }
}