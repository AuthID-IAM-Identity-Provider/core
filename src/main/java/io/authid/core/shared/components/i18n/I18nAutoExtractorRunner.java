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

            log.info("Found {} classes to process", scanResult.getAllClasses().size());

            log.info("Processing classes in parallel...");
            // OPTIMIZED: Changed from forEach to parallelStream().forEach()
            scanResult.getAllClasses().parallelStream().forEach(classInfo -> {
                try {
                    Class<?> clazz = classInfo.loadClass();
                    // Optimization: Read a source only once if it contains the keyword
                    if (I18nExtractorUtils.sourceContains(clazz, "I18n.extract")) {
                        log.info("Current Processing class : {}", clazz.getName());

                        for (Locale locale : locales) {
                            // The log below might interleave in parallel execution, which is expected.
                            // log.info("Check and Start Extraction for locales: {} Locale", locale.getLanguage());
                            I18nExtractor.extractAndWriteForClass(clazz, locale);
                        }
                    }
                } catch (Exception e) {
                    // It's better to log the full stack trace for better debugging in parallel context
                    log.error("Failed processing class: {}", classInfo.getName(), e);
                }
            });

            log.info("All classes processed.");
        }
    }
}