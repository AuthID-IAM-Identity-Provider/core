#!/bin/bash

# ==============================================================================
# Script untuk menjalankan Promtail
# Script ini secara otomatis menemukan file konfigurasi berdasarkan lokasinya.
# ==============================================================================

echo "🚀 Starting Promtail..."

# Mendapatkan direktori tempat script ini berada
# Ini membuat script bisa dijalankan dari mana saja
SCRIPT_DIR=$(dirname "$0")

# Membuat path lengkap ke file konfigurasi
CONFIG_FILE="$SCRIPT_DIR/promtail-config.yml"

# Memeriksa apakah file konfigurasi benar-benar ada sebelum melanjutkan
if [ ! -f "$CONFIG_FILE" ]; then
    echo "❌ Error: Config file not found at $CONFIG_FILE"
    exit 1
fi

echo "📖 Using configuration: $CONFIG_FILE"
echo "----------------------------------------------------"

# Menjalankan Promtail dengan file konfigurasi yang sudah ditentukan
# Pastikan 'promtail' sudah terinstal di /usr/local/bin
promtail -config.file="$CONFIG_FILE"