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
import java.util.List;
import java.util.Locale;

/**
 * A self-contained script to extract I18n keys.
 * This class is NOT a Spring @Component of the main application.
 * It has its own main method that bootstraps a minimal, non-web Spring context.
 * This prevents it from running accidentally when the main web application starts.
 */
@Slf4j
@EnableAutoConfiguration(exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        LiquibaseAutoConfiguration.class
})
@Import(I18nConfig.class) // <-- Secara eksplisit impor konfigurasi yang dibutuhkan
@RequiredArgsConstructor
public class I18nAutoExtractorRunner implements CommandLineRunner {

    @Qualifier("supportedLocales")
    private final List<Locale> locales;

    /**
     * The main entry point for the script. This is the ONLY way to run this class.
     */
    public static void main(String[] args) {
        new SpringApplicationBuilder(I18nAutoExtractorRunner.class)
                .web(WebApplicationType.NONE)
                .run(args);
    }

    @Override
    public void run(String... args) {
        log.info("============================================================");
        log.info("=== I18N AUTO-EXTRACTOR SCRIPT STARTING                ===");
        log.info("============================================================");

        log.info("Found {} locales to process from context: {}", locales.size(), locales);
        String basePackage = "io.authid.utils";
        log.info("Base package for extraction: {}", basePackage);

        try (ScanResult scanResult = new ClassGraph().enableAllInfo().acceptPackages(basePackage).scan()) {
            log.info("Found {} classes to process", scanResult.getAllClasses().size());
            log.info("Processing classes in parallel...");
            scanResult.getAllClasses().parallelStream().forEach(classInfo -> {
                try {
                    if (!classInfo.isInterfaceOrAnnotation() && classInfo.isPublic()) {
                        Class<?> clazz = classInfo.loadClass();
                        if (I18nExtractorUtils.sourceContains(clazz, "I18n.extract")) {
                            log.info("Processing class: {}", clazz.getName());
                            for (Locale locale : locales) {
                                I18nExtractor.extractAndWriteForClass(clazz, locale);
                            }
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

        log.info("========================================================");
        log.info("=== I18N AUTO-EXTRACTOR SCRIPT FINISHED              ===");
        log.info("========================================================");
    }
}
