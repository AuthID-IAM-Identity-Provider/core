package io.authid.core.shared.components.i18n.extractors;

import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class I18nExtractor {

    private static final Pattern I18N_PATTERN = Pattern.compile("I18n\\.extract\\(\"([^\"]+)\"");
    private static final Pattern VALIDATION_MESSAGE_PATTERN = Pattern.compile("message\\s*=\\s*\"\\{([a-zA-Z0-9_.-]+)}\"");

    public static void extractAndWriteForClass(Class<?> clazz, Locale locale) {
        try {
            String source = I18nExtractorUtils.readSourceForClass(clazz);
            Matcher matcher = I18N_PATTERN.matcher(source);

            Map<String, String> i18nMap = new LinkedHashMap<>();
            String fullPrefix = clazz.getName();

            while (matcher.find()) {
                String key = matcher.group(1);
                String fullKey = fullPrefix + "." + key;
                i18nMap.put(fullKey, "");
            }

            if (!i18nMap.isEmpty()) {
                I18nExtractorUtils.writeToJson(locale, fullPrefix, i18nMap);
            }

            // Recurse if inner class
            for (Class<?> inner : clazz.getDeclaredClasses()) {
                extractAndWriteForClass(inner, locale);
            }

        } catch (Exception e) {
            log.error("Error extracting class:{}: {}", clazz.getName(), e.getMessage());
        }
    }

    public static Set<String> findI18nKeys(String content) {
        Set<String> keys = new HashSet<>();
        Matcher matcher = I18N_PATTERN.matcher(content);
        while (matcher.find()) {
            keys.add(matcher.group(1));
        }
        return keys;
    }

    public static Set<String> findValidationKeys(String content) {
        Set<String> keys = new HashSet<>();
        Matcher matcher = VALIDATION_MESSAGE_PATTERN.matcher(content);
        while (matcher.find()) {
            keys.add(matcher.group(1));
        }
        return keys;
    }
}
