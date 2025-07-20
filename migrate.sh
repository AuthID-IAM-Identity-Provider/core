#!/bin/bash

# ==============================================================================
# Skrip untuk migrasi inkremental (Satu perubahan, satu file)
# ==============================================================================

# Logging ke file + console
LOG_FILE="logs/migrate_$(date +%Y%m%d_%H%M%S).log"
mkdir -p logs
exec > >(tee -a "$LOG_FILE") 2>&1

# 1. Cek argumen
if [ -z "$1" ]; then
  echo "[ERROR] $(date '+%Y-%m-%d %H:%M:%S') - Deskripsi migrasi belum diberikan"
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

# 3. Generate changelog
echo "[INFO] $(date '+%Y-%m-%d %H:%M:%S') - [1/4] Membuat file changelog..."
mvn compile liquibase:diff -Dliquibase.diffChangeLogFile="$FULL_PATH"
if [ $? -ne 0 ] || [ ! -f "$FULL_PATH" ]; then
  echo "[ERROR] $(date '+%Y-%m-%d %H:%M:%S') - Gagal membuat changelog: $FILENAME"
  exit 1
fi
echo "[INFO] $(date '+%Y-%m-%d %H:%M:%S') - File changelog berhasil dibuat: $FILENAME"

# 4. Tambahkan ke master
echo "[INFO] $(date '+%Y-%m-%d %H:%M:%S') - [2/4] Mendaftarkan changelog..."
INCLUDE_PATH="db/changelog/${FILENAME}"
printf "\n  - include:\n      file: %s" "$INCLUDE_PATH" >> "$MASTER_LOG"
echo "[INFO] $(date '+%Y-%m-%d %H:%M:%S') - Master changelog diperbarui."

# 5. Jalankan migrasi
echo "[INFO] $(date '+%Y-%m-%d %H:%M:%S') - [3/4] Menerapkan ke database..."
mvn compile process-resources liquibase:update
if [ $? -ne 0 ]; then
  echo "[ERROR] $(date '+%Y-%m-%d %H:%M:%S') - Gagal menerapkan ke DB."
  exit 1
fi
echo "[INFO] $(date '+%Y-%m-%d %H:%M:%S') - Perubahan berhasil diterapkan."

# 6. Selesai
echo "[DONE] $(date '+%Y-%m-%d %H:%M:%S') - [4/4] Migrasi selesai."

