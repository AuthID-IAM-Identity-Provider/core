package io.authid.core.shared.components.i18n;

import java.util.HashMap;
import java.util.Map;

public class I18n {

    private static final Map<String, String> i18nKeys = new HashMap<>();
    private static final ThreadLocal<String> sourceClass = new ThreadLocal<>();

    /**
     * Memanggil key dan mencatatnya dalam map sementara.
     * @param key sub-key seperti "label.success"
     * @return full key dengan path class
     */
    public static String extract(String key) {
        String base = sourceClass.get();
        if (base == null) {
            base = "unknown";
        }
        String fullKey = base + "." + key;
        i18nKeys.putIfAbsent(fullKey, "");
        return fullKey;
    }

    public static void setSourceClass(Class<?> clazz) {
        sourceClass.set(clazz.getName());
    }

    public static Map<String, String> getKeys() {
        return new HashMap<>(i18nKeys);
    }

    public static void clear() {
        i18nKeys.clear();
    }
}
