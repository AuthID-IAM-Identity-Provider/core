package io.authid.core.shared.components.i18n;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Locale;

@Slf4j
public class I18nAutoExtractorRunner {
    public static void main(String[] args) {
        List<Locale> locales = List.of(
                new Locale("id"),
                new Locale("en"),
                new Locale("fr"),
                new Locale("ar")
        );

        log.info("Found {} locales to proceeds", locales.size());

        String basePackage = "io.authid.core";

        log.info("Get base package for extractions: {}", basePackage);


        try (ScanResult scanResult = new ClassGraph()
                .enableClassInfo()
                .acceptPackages(basePackage)
                .scan()) {

            log.info("Found {} classes", scanResult.getAllClasses().size());

            log.info("Processing classes...");
            scanResult.getAllClasses().forEach(classInfo -> {
                try {
                    Class<?> clazz = classInfo.loadClass();
                    String sourceCode = I18nExtractorUtils.readSourceForClass(clazz);
                    if (sourceCode.contains("I18n.extract")) {
                        log.info("Current Processing class : {}", clazz.getName());

                        for (Locale locale : locales) {
                            log.info("Check and Start Extraction for locales: {} Locale", locale.getLanguage());
                            I18nExtractor.extractAndWriteForClass(clazz, locale);
                        }
                    }
                } catch (Exception e) {
                    log.error("Failed processing class: {} → {}", classInfo.getName(), e.getMessage());
                }
            });

            log.info("All classes processed.");
        }
    }
}
