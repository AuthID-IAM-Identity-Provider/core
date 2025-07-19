#!/bin/bash

# ==============================================================================
# Skrip untuk migrasi inkremental (Satu perubahan, satu file)
# Membuat file changelog, mendaftarkannya, dan menerapkannya ke DB.
# ==============================================================================

# 1. Cek argumen
if [ -z "$1" ]; then
  echo "❌ Error: Tolong berikan deskripsi untuk migrasi."
  echo "Contoh: ./migrate.sh \"add description to role\""
  exit 1
fi

# 2. Siapkan nama dan path
TIMESTAMP=$(date +%Y_%m_%d_%H%M%S)
DESCRIPTION=$(echo "$1" | tr ' ' '_')
CHANGELOG_DIR="src/main/resources/db/changelog"
FILENAME="${TIMESTAMP}_${DESCRIPTION}.yaml"
FULL_PATH="${CHANGELOG_DIR}/${FILENAME}"
MASTER_LOG="${CHANGELOG_DIR}/db.changelog-master.yaml"

# 3. Jalankan perintah Maven untuk membuat file changeset
echo "✍️  [1/4] Membuat file changelog..."
# Kita hanya butuh compile di sini karena diff perlu file .class
mvn compile liquibase:diff -Dliquibase.diffChangeLogFile="$FULL_PATH"

# Periksa apakah perintah Maven berhasil dan file baru telah dibuat
# shellcheck disable=SC2181
if [ $? -ne 0 ] || [ ! -f "$FULL_PATH" ]; then
    echo "❌ Error: Gagal membuat file changelog. Proses dibatalkan."
    exit 1
fi
echo "✅ File changelog berhasil dibuat: $FILENAME"

# 4. Tambahkan entri 'include' ke file master
echo "🔄 [2/4] Mendaftarkan changelog ke file master..."
INCLUDE_PATH="db/changelog/${FILENAME}"
printf "\n  - include:\n      file: %s" "$INCLUDE_PATH" >> "$MASTER_LOG"
echo "✅ Master changelog berhasil diperbarui."

# 5. Terapkan perubahan ke database
echo "🚀 [3/4] Menerapkan perubahan ke database..."
# Gunakan 'process-resources' agar file YAML baru disalin ke target/classes
mvn compile process-resources liquibase:update
# shellcheck disable=SC2181
if [ $? -ne 0 ]; then
    echo "❌ Error: Gagal menerapkan perubahan ke database."
    exit 1
fi
echo "✅ Perubahan berhasil diterapkan ke database."

# 6. Pesan Selesai
echo "🎉 [4/4] Proses migrasi inkremental selesai!"