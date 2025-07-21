package io.authid.core.shared.components.i18n.sources;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.AbstractMessageSource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class JsonMessageSource extends AbstractMessageSource {

    private static final String I18N_PATH_PATTERN = "classpath*:/i18n/%s/**/*.json";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

    // Cache untuk menyimpan pesan yang sudah dimuat. Kunci: Locale, Value: Map<Key, Pesan>
    private final Map<Locale, Map<String, String>> cachedMessages = new ConcurrentHashMap<>();

    @Override
    protected MessageFormat resolveCode(String code, Locale locale) {
        // Ambil pesan dari cache, jika tidak ada, muat dari file JSON
        Map<String, String> messages = cachedMessages.computeIfAbsent(locale, this::loadMessagesForLocale);
        String message = messages.get(code);

        if (message == null) {
            return null; // Jika tidak ditemukan, biarkan parent yang menangani (atau gagal)
        }

        return createMessageFormat(message, locale);
    }

    /**
     * Memuat semua file JSON untuk locale tertentu, menggabungkannya menjadi satu Map,
     * dan mengembalikannya.
     *
     * @param locale Locale yang akan dimuat.
     * @return Map gabungan dari semua file JSON.
     */
    private Map<String, String> loadMessagesForLocale(Locale locale) {
        String path = String.format(I18N_PATH_PATTERN, locale.getLanguage());
        log.info("Loading i18n messages for locale '{}' from path pattern: {}", locale.getLanguage(), path);

        Map<String, String> combinedMessages = new HashMap<>();
        try {
            Resource[] resources = resolver.getResources(path);
            if (resources.length == 0) {
                log.warn("No i18n JSON files found for locale '{}' using pattern '{}'", locale.getLanguage(), path);
                return combinedMessages;
            }

            TypeReference<Map<String, String>> typeReference = new TypeReference<>() {};

            for (Resource resource : resources) {
                try (InputStream inputStream = resource.getInputStream()) {
                    Map<String, String> messagesFromFile = objectMapper.readValue(inputStream, typeReference);
                    combinedMessages.putAll(messagesFromFile);
                } catch (IOException e) {
                    log.error("Failed to read or parse i18n JSON file: {}", resource.getFilename(), e);
                }
            }
            log.info("Successfully loaded {} message keys for locale '{}'", combinedMessages.size(), locale.getLanguage());

        } catch (IOException e) {
            log.error("Could not resolve i18n resources for locale '{}' using pattern '{}'", locale.getLanguage(), path, e);
        }

        return combinedMessages;
    }
}
