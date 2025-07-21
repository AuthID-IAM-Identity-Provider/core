package io.authid.core.shared.components.i18n;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@Slf4j
public class I18nExtractorUtils {

    public static String readSourceForClass(Class<?> clazz) throws IOException {
        String path = clazz.getName().replace('.', '/');
        // Handle inner classes correctly, which contain '$'
        path = path.replaceAll("\\$.*", "");
        Path sourcePath = Paths.get("src/main/java/" + path + ".java");
        return Files.readString(sourcePath);
    }

    // Helper method to avoid reading the file content twice
    public static boolean sourceContains(Class<?> clazz, String text) {
        try {
            return readSourceForClass(clazz).contains(text);
        } catch (IOException e) {
            log.warn("Could not read source for class {}: {}", clazz.getName(), e.getMessage());
            return false;
        }
    }


    // OPTIMIZED: Added 'synchronized' to make the method thread-safe.
    public static synchronized void writeToJson(Locale locale, String fullPrefix, Map<String, String> i18nMap) {
        String language = locale.getLanguage();
        String relativePath = fullPrefix.replace('.', '/') + ".json";
        Path filePath = Paths.get("src/main/resources/i18n", language, relativePath);

        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        Map<String, String> finalMap = new LinkedHashMap<>();

        try {
            Files.createDirectories(filePath.getParent());

            // 🟡 Step 1: Baca file JSON lama (jika ada)
            if (Files.exists(filePath)) {
                try (InputStream in = Files.newInputStream(filePath)) {
                    finalMap = mapper.readValue(in, new TypeReference<Map<String, String>>() {});
                } catch (IOException e) {
                    log.warn("Failed reading existing JSON file, a new one will be created. File: {}", filePath, e);
                }
            }

            // 🟡 Step 2: Tambahkan key baru jika belum ada
            boolean hasChanges = false;
            for (Map.Entry<String, String> entry : i18nMap.entrySet()) {
                if (!finalMap.containsKey(entry.getKey())) {
                    finalMap.put(entry.getKey(), entry.getValue()); // "" sebagai placeholder
                    hasChanges = true;
                }
            }

            // 🟡 Step 3: Tulis ulang file hanya jika ada perubahan
            if (hasChanges) {
                Files.writeString(filePath, mapper.writeValueAsString(finalMap));
                log.info("Updated file: {}", filePath);
            } else {
                // This log can be noisy in a large project, consider removing or lowering its level.
                // log.info("No changes to write: {}", filePath);
            }
        } catch (IOException e) {
            log.error("Failed writing to: {}: {}", filePath, e.getMessage());
        }
    }
}