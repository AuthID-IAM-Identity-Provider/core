#!/bin/bash

# ==============================================================================
# Skrip ini membuat "snapshot" atau backup dari STRUKTUR database Anda saat ini
# menggunakan Liquibase. Ini akan menghasilkan satu file changelog YAML
# yang bisa digunakan untuk membuat ulang skema yang sama persis di tempat lain.
# Ini TIDAK mem-backup DATA, hanya skema (tabel, kolom, index, dll.).
# ==============================================================================

# Konfigurasi
BACKUP_DIR="backups/db_structure"

# --- LANGKAH 1: Persiapan ---
echo "🚀 [1/3] Memulai proses backup struktur database..."

# Buat direktori backup jika belum ada
mkdir -p "$BACKUP_DIR"

# Buat nama file yang unik berdasarkan timestamp
TIMESTAMP=$(date +%Y_%m_%d_%H%M%S)
BACKUP_FILENAME="backup_structure_${TIMESTAMP}.yaml"
BACKUP_FULL_PATH="${BACKUP_DIR}/${BACKUP_FILENAME}"

echo "📝 File backup akan disimpan di: ${BACKUP_FULL_PATH}"

# --- LANGKAH 2: Jalankan Liquibase untuk Membuat Changelog ---
echo "🔍 [2/3] Menganalisis database dan membuat changelog..."

# Perintah inti: generateChangeLog akan memeriksa database yang terhubung
# dan membuat file changelog yang merepresentasikan skemanya.
mvn liquibase:generateChangeLog -Dliquibase.changeLogFile="$BACKUP_FULL_PATH"

# Periksa apakah perintah sebelumnya berhasil
if [ $? -ne 0 ]; then
    echo "❌ Gagal saat menjalankan 'liquibase:generateChangeLog'."
    echo "Pastikan konfigurasi database Anda di pom.xml sudah benar."
    exit 1
fi

# --- LANGKAH 3: Verifikasi dan Selesai ---
# Periksa apakah file backup benar-benar dibuat
if [ ! -f "$BACKUP_FULL_PATH" ]; then
    echo "❌ Gagal membuat file backup. File tidak ditemukan di lokasi yang diharapkan."
    exit 1
fi

echo "✅ [3/3] File backup struktur berhasil dibuat."
echo "🎉 Proses selesai! Snapshot skema Anda ada di: ${BACKUP_FULL_PATH}"
