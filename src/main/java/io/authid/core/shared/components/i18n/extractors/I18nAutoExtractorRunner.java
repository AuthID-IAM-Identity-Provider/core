package io.authid.core.shared.components.i18n.extractors;

import io.authid.core.shared.components.i18n.annotations.ExtractableI18n;
import io.authid.core.shared.components.i18n.annotations.KeyType;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RequiredArgsConstructor
@ShellComponent
public class I18nAutoExtractorRunner {

    @Qualifier("supportedLocales")
    private final List<Locale> locales;

    @ShellMethod("Runs the i18n key extraction script with class-based JSON output.")
    public void extractI18n() {
        log.info("============================================================");
        log.info("=== I18N AUTO-EXTRACTOR (CLASS-BASED JSON MODE) STARTING ===");
        log.info("============================================================");

        String basePackage = "io.authid";

        // ✨ KUNCI PERUBAHAN: Simpan kunci per kelas dalam sebuah Map
        Map<Class<?>, Set<String>> jsonKeysPerClass = new ConcurrentHashMap<>();
        Set<String> validationPropKeys = ConcurrentHashMap.newKeySet();

        try (ScanResult scanResult = new ClassGraph().enableAllInfo().acceptPackages(basePackage).scan()) {
            scanResult.getAllClasses().parallelStream().forEach(classInfo -> {
                try {
                    if (classInfo.isInterfaceOrAnnotation() || !classInfo.isPublic()) return;
                    Class<?> clazz = classInfo.loadClass();

                    // Alur tetap sama: anotasi dulu, baru regex
                    boolean processedByAnnotation = processKeysFromAnnotations(clazz, jsonKeysPerClass, validationPropKeys);
                    if (!processedByAnnotation) {
                        processKeysFromLegacyRegex(clazz, jsonKeysPerClass, validationPropKeys);
                    }
                } catch (Throwable e) {
                    log.error("Failed processing class: {} -> {}", classInfo.getName(), e.getMessage());
                }
            });
        }

        // ✨ BLOK PENULISAN FILE YANG DIPERBAIKI

        // 1. Tulis kunci JSON, satu file per kelas
        log.info("Found keys in {} classes to be written to JSON.", jsonKeysPerClass.size());
        jsonKeysPerClass.forEach((clazz, keys) -> {
            if (!keys.isEmpty()) {
                Map<String, String> keysMap = new LinkedHashMap<>();
                keys.stream().sorted().forEach(key -> keysMap.put(key, ""));
                for (Locale locale : locales) {
                    I18nExtractorUtils.writeToJson(locale, clazz.getName(), keysMap);
                }
            }
        });

        // 2. Tulis kunci validasi ke file .properties (tidak berubah)
        log.info("Found {} unique validation keys to be written to .properties.", validationPropKeys.size());
        if (!validationPropKeys.isEmpty()) {
            for (Locale locale : locales) {
                try {
                    I18nExtractorUtils.writeToProperties(locale, validationPropKeys);
                } catch (IOException e) {
                    log.error("Failed to write ValidationMessages.properties for locale {}: {}", locale, e.getMessage());
                }
            }
        }

        log.info("========================================================");
        log.info("=== I18N AUTO-EXTRACTOR SCRIPT FINISHED              ===");
        log.info("========================================================");
    }

    /**
     * [HELPER REFLEKSI] - Diperbarui untuk menyimpan hasil ke Map
     */
    private boolean processKeysFromAnnotations(Class<?> clazz, Map<Class<?>, Set<String>> jsonKeyStore, Set<String> validationKeyStore) {
        boolean annotationFound = false;
        if (clazz.isEnum()) {
            String fullClassNamePrefix = clazz.getName() + ".";
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isEnumConstant()) {
                    ExtractableI18n annotation = field.getAnnotation(ExtractableI18n.class);
                    if (annotation != null) {
                        annotationFound = true;
                        String baseKey = annotation.value();

                        if (annotation.type() == KeyType.VALIDATION) {
                            validationKeyStore.add(baseKey);
                        } else {
                            Set<String> keysForClass = jsonKeyStore.computeIfAbsent(clazz, k -> ConcurrentHashMap.newKeySet());
                            // ✨ Tambahkan prefix ke kunci
                            keysForClass.add(fullClassNamePrefix + baseKey);
                            for (String suffix : annotation.suffixes()) {
                                keysForClass.add(fullClassNamePrefix + baseKey + suffix);
                            }
                        }
                    }
                }
            }
        }
        return annotationFound;
    }

    /**
     * [HELPER REGEX] - Diperbarui untuk menyimpan hasil ke Map
     */
    private void processKeysFromLegacyRegex(Class<?> clazz, Map<Class<?>, Set<String>> jsonKeyStore, Set<String> validationKeyStore) {
        try {
            String source = I18nExtractorUtils.readSourceForClass(clazz);
            String fullClassNamePrefix = clazz.getName() + ".";

            // Kunci dari I18n.extract() ditambahkan dengan prefix
            Set<String> i18nKeys = I18nExtractor.findI18nKeys(source);
            if (!i18nKeys.isEmpty()) {
                Set<String> keysForClass = jsonKeyStore.computeIfAbsent(clazz, k -> ConcurrentHashMap.newKeySet());
                i18nKeys.forEach(key -> keysForClass.add(fullClassNamePrefix + key));
            }

            // Kunci validasi tidak perlu prefix karena global
            Set<String> validationKeys = I18nExtractor.findValidationKeys(source);
            validationKeyStore.addAll(validationKeys);
        } catch(Exception e) {
            log.warn("Could not read source for legacy class {}: {}", clazz.getName(), e.getMessage());
        }
    }
}