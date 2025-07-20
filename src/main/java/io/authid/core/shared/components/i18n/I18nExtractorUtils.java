package io.authid.core.shared.components.i18n;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class I18nExtractorUtils {

    public static String readSourceForClass(Class<?> clazz) throws IOException {
        String path = clazz.getName().replace('.', '/');
        Path sourcePath = Paths.get("src/main/java/" + path + ".java");
        return Files.readString(sourcePath);
    }

    public static void writeToJson(Locale locale, String fullPrefix, Map<String, String> i18nMap) throws IOException {
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
                    System.err.println("⚠️ Gagal baca file lama: " + filePath);
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
                System.out.println("✅ Diperbarui: " + filePath);
            } else {
                System.out.println("ℹ️ Tidak ada key baru untuk: " + filePath);
            }
        } catch (IOException e) {
            System.err.println("❌ Gagal menulis ke " + filePath + ": " + e.getMessage());
        }
    }
}
