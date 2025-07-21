package io.authid.core.shared.components.i18n.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.stereotype.Component;
import java.util.Locale;

@Component
public class I18nService {

    private final MessageSource messageSource;

    @Autowired
    public I18nService(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String translate(String key, Locale locale, Object... args) {
        try {
            return messageSource.getMessage(key, args, locale);
        } catch (NoSuchMessageException e) {
            return key;
        }
    }
}