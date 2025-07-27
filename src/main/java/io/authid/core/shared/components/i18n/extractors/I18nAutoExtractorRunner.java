package io.authid.core.shared.components.i18n.extractors;

import io.authid.core.shared.components.i18n.configurations.I18nConfig;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Import;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A self-contained script to extract I18n keys.
 * This class is NOT a Spring @Component of the main application.
 * It has its own main method that bootstraps a minimal, non-web Spring context.
 * This prevents it from running accidentally when the main web application starts.
 */
@Slf4j
//@EnableAutoConfiguration(exclude = {
//        DataSourceAutoConfiguration.class,
//        HibernateJpaAutoConfiguration.class,
//        LiquibaseAutoConfiguration.class
//})
//@Import(I18nConfig.class) // <-- Secara eksplisit impor konfigurasi yang dibutuhkan
@RequiredArgsConstructor
@ShellComponent
public class I18nAutoExtractorRunner{

    @Qualifier("supportedLocales")
    private final List<Locale> locales;

//    /**
//     * The main entry point for the script. This is the ONLY way to run this class.
//     */
//    public static void main(String[] args) {
//        new SpringApplicationBuilder(I18nAutoExtractorRunner.class)
//                .web(WebApplicationType.NONE)
//                .run(args);
//    }

    @ShellMethod("Runs the i18n key extraction script.")
    public void extractI18n() {
        log.info("============================================================");
        log.info("=== I18N AUTO-EXTRACTOR SCRIPT STARTING                ===");
        log.info("============================================================");

        log.info("Found {} locales to process from context: {}", locales.size(), locales);
        String basePackage = "io.authid";
        log.info("Base package for extraction: {}", basePackage);

        Set<String> allValidationKeys = ConcurrentHashMap.newKeySet();

        try (ScanResult scanResult = new ClassGraph().enableAllInfo().acceptPackages(basePackage).scan()) {
            log.info("Found {} classes to process", scanResult.getAllClasses().size());
            log.info("Processing classes in parallel...");
            scanResult.getAllClasses().parallelStream().forEach(classInfo -> {
                try {
                    if (!classInfo.isInterfaceOrAnnotation() && classInfo.isPublic()) {
                        Class<?> clazz = classInfo.loadClass();
                        String source = I18nExtractorUtils.readSourceForClass(clazz);

                        Set<String> i18nKeys = I18nExtractor.findI18nKeys(source);
                        Set<String> validationKeys = I18nExtractor.findValidationKeys(source);

                        if (!i18nKeys.isEmpty()) {
                            log.info("Processing class: {}", clazz.getName());
                            for (Locale locale : locales) {
                                I18nExtractor.extractAndWriteForClass(clazz, locale);
                            }
                        }

                        if (!validationKeys.isEmpty()) {
                            log.info("Found {} validation keys in {}", validationKeys.size(), clazz.getSimpleName());
                            allValidationKeys.addAll(validationKeys);
                        }
                    }
                } catch (Throwable e) {
                    log.error("Failed processing class: {} -> {}", classInfo.getName(), e.getMessage());
                }
            });
            log.info("All classes processed.");
        } catch (Exception e) {
            log.error("An error occurred during the i18n extraction process.", e);
        }

        log.info("Found {} unique validation keys in total.", allValidationKeys.size());
        if (!allValidationKeys.isEmpty()) {
            for (Locale locale : locales) {
                try {
                    I18nExtractorUtils.writeToProperties(locale, allValidationKeys);
                } catch (IOException e) {
                    log.error("Failed to write ValidationMessages.properties for locale {}: {}", locale, e.getMessage());
                }
            }
        }

        log.info("========================================================");
        log.info("=== I18N AUTO-EXTRACTOR SCRIPT FINISHED              ===");
        log.info("========================================================");
    }
}
