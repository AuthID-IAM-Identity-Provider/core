package io.authid.core.shared.components.i18n.extractors;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class I18n {

    private static final Map<String, String> i18nKeys = new HashMap<>();
    private static final ThreadLocal<String> sourceClass = new ThreadLocal<>();

    public static String extract(String key) {
        String base = sourceClass.get();
        if (base == null) {
            base = "unknown";
        }
        String fullKey = base + "." + key;
        i18nKeys.putIfAbsent(fullKey, "");
        log.info("key : {}", fullKey);
        return fullKey;
    }

    public static void setSourceClass(Class clazz) {
        sourceClass.set(clazz.getName());
    }

    public static Map<String, String> getKeys() {
        return new HashMap<>(i18nKeys);
    }

    public static void clear() {
        i18nKeys.clear();
    }
}
