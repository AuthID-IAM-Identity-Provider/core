package io.authid.core.shared.components.i18n;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;

import java.util.List;
import java.util.Locale;

public class I18nAutoExtractorRunner {
    public static void main(String[] args) {
        List<Locale> locales = List.of(
                new Locale("id"),
                new Locale("en"),
                new Locale("fr"),
                new Locale("ar")
        );

        String basePackage = "io.authid.core";

        try (ScanResult scanResult = new ClassGraph()
                .enableClassInfo()
                .acceptPackages(basePackage)
                .scan()) {

            scanResult.getAllClasses().forEach(classInfo -> {
                try {
                    Class<?> clazz = classInfo.loadClass();
                    String sourceCode = I18nExtractorUtils.readSourceForClass(clazz);
                    if (sourceCode.contains("I18n.extract")) {
                        System.out.println("🔍 " + clazz.getName());

                        for (Locale locale : locales) {
                            System.out.println("🌐 Extracting for: " + locale.getLanguage());
                            I18nExtractor.extractAndWriteForClass(clazz, locale);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("❌ Gagal memproses: " + classInfo.getName() + " → " + e.getMessage());
                }
            });

            System.out.println("✅ Semua selesai.");
        }
    }
}
