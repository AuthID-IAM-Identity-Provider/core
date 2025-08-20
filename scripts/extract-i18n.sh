#!/bin/bash

# Skrip ini mengkompilasi proyek dan kemudian menjalankan kelas I18nAutoExtractorRunner secara langsung.
# Kelas ini akan membuat konteks Spring-nya sendiri yang minimal, tanpa memuat konfigurasi database.

echo "Compiling and running the I18n Auto-Extractor Script..."

mvn clean compile exec:java -Dexec.mainClass="io.authid.core.shared.components.i18n.extractors.I18nAutoExtractorRunner"

echo "I18n Auto-Extractor script finished."