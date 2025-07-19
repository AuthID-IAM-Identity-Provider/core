#!/bin/bash

# ==============================================================================
# Skrip ini meniru 'php artisan migrate:fresh --seed' untuk Spring + Liquibase
# PERINGATAN: Ini akan MENGHAPUS TOTAL SEMUA DATA di database lokal Anda.
# ==============================================================================

# Konfigurasi
CHANGELOG_DIR="src/main/resources/db/changelog"
MASTER_LOG="${CHANGELOG_DIR}/db.changelog-master.yaml"

# Safety Check - Konfirmasi dari pengguna
read -p "⚠️  PERINGATAN: Ini akan menghapus semua tabel di database Anda. Lanjutkan? (y/n) " -n 1 -r
echo # Pindah ke baris baru
if [[ ! $REPLY =~ ^[Yy]$ ]]
then
    echo "Proses dibatalkan."
    exit 1
fi

# --- LANGKAH 1: Hapus semua tabel (migrate:fresh) ---
echo "🔥 [1/5] Menghapus semua tabel (liquibase:dropAll)..."
mvn liquibase:dropAll
if [ $? -ne 0 ]; then echo "❌ Gagal drop tables."; exit 1; fi

# --- LANGKAH 2: Bersihkan file changelog lama ---
# Ini menghapus semua file .yaml KECUALI file master itu sendiri.
echo "🧹 [2/5] Membersihkan file changelog lama..."
find "$CHANGELOG_DIR" -type f ! -name 'db.changelog-master.yaml' -delete

# --- LANGKAH 3: Reset file master changelog ---
echo "🔄 [3/5] Mereset master changelog..."
echo "databaseChangeLog:" > "$MASTER_LOG"

# --- LANGKAH 4: Buat snapshot skema baru yang bersih ---
echo "✨ [4/5] Membuat snapshot skema dari entitas saat ini..."
TIMESTAMP=$(date +%Y_%m_%d_%H%M%S)
SNAPSHOT_FILENAME="${TIMESTAMP}_initial_schema_snapshot.yaml"
SNAPSHOT_FULL_PATH="${CHANGELOG_DIR}/${SNAPSHOT_FILENAME}"

# Jalankan diff untuk membuat file snapshot tunggal
mvn compile process-resources liquibase:diff -Dliquibase.diffChangeLogFile="$SNAPSHOT_FULL_PATH" -X
if [ $? -ne 0 ] || [ ! -f "$SNAPSHOT_FULL_PATH" ]; then
    echo "❌ Gagal membuat snapshot skema."; exit 1;
fi

# Secara otomatis include snapshot baru ke file master
INCLUDE_PATH="db/changelog/${SNAPSHOT_FILENAME}"
printf "\n  - include:\n      file: %s" "$INCLUDE_PATH" >> "$MASTER_LOG"
echo "✅ Snapshot skema baru berhasil dibuat dan didaftarkan."

# --- LANGKAH 5: Terapkan perubahan ke database (AUTO PERSIST) ---
echo "🚀 [5/6] Menerapkan perubahan ke database (liquibase:update)..."
mvn compile process-resources liquibase:update -X
if [ $? -ne 0 ]; then echo "❌ Gagal menerapkan perubahan ke database."; exit 1; fi
echo "✅ [5/6] Tabel berhasil dibuat di database."

# --- LANGKAH 6: Jalankan Seeder (jika ada flag --seed) ---
if [[ " $* " =~ " --seed " ]]; then
    echo "🌱 [6/6] Menjalankan database seeder (spring-boot:run)..."
    # Pastikan Anda punya profil 'seeding' atau mekanisme lain untuk menjalankan seeder
    mvn spring-boot:run -Dspring-boot.run.profiles=seeding
    if [ $? -ne 0 ]; then echo "❌ Gagal menjalankan seeder."; exit 1; fi
    echo "✅ Seeding selesai."
else
    echo "👍 [6/6] Selesai. Lewati seeder (tidak ada flag --seed)."
fi

echo "🎉 Proses 'migratie-fresh' selesai! Database Anda sekarang bersih dan sesuai dengan entitas terakhir."