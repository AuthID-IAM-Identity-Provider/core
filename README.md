# AuthID Core Engine

![Build Status](https://img.shields.io/badge/build-passing-brightgreen)
![Java Version](https://img.shields.io/badge/java-21-blue)
![Spring Boot Version](https://img.shields.io/badge/Spring%20Boot-3.3.0-green)

AuthID Core Engine adalah fondasi backend yang kokoh dibangun dengan Spring Boot, dirancang untuk menyediakan layanan esensial untuk aplikasi modern, termasuk sistem Role-Based Access Control (RBAC) yang kuat, migrasi database otomatis, dan alur kerja ramah pengembang yang terinspirasi oleh kerangka kerja Laravel.

## ✨ Fitur Unggulan

* **Role-Based Access Control (RBAC):** Sistem izin yang fleksibel dan diperkaya terinspirasi oleh `spatie/laravel-permission`, memungkinkan untuk peran, izin, dan penetapan langsung ke model.
* **Manajemen Migrasi Database:** Menggunakan Liquibase untuk mengelola perubahan skema database secara terkontrol dan konsisten.
* **Alur Kerja Terinspirasi Laravel:**
    * **Factories:** Membuat data dummy untuk entitas Anda dengan mudah untuk pengujian dan seeding.
    * **Seeders:** Mengisi database Anda dengan data awal atau data uji secara terstruktur.
    * **Skrip Kustom:** Termasuk `fresh.sh` dan `migrate.sh` untuk mengotomatiskan dan menyederhanakan proses migrasi dan seeding.
* **Audit Otomatis:** Melacak `createdAt`, `updatedAt`, `createdBy`, dan `updatedBy` secara otomatis untuk semua entitas.
* **Live Reload:** Terintegrasi dengan Spring Boot DevTools untuk restart aplikasi otomatis dan live reload browser selama pengembangan.

## 📋 Persyaratan

* Java JDK 21
* Apache Maven 3.8+
* Instans Database Oracle
* Lingkungan shell (seperti Bash atau Zsh) untuk menjalankan skrip bantuan.

## 🚀 Instalasi & Pengaturan

1.  **Clone repository:**
    ```bash
    git clone <your-repository-url>
    cd utils
    ```

2.  **Konfigurasi Koneksi Database:**
    Buka `src/main/resources/application.yaml` dan perbarui bagian `datasource` dengan kredensial database Oracle Anda:
    ```yaml
    spring:
      datasource:
        url: jdbc:oracle:thin:@//<host>:<port>/<service_name>
        username: <your_username>
        password: <your_password>
    ```

3.  **Instal Dependensi:**
    Jalankan perintah Maven berikut untuk mengunduh semua library yang diperlukan.
    ```bash
    mvn clean install
    ```

## 💻 Alur Kerja Pengembangan

Untuk mempercepat pengembangan lokal, proyek ini menyertakan skrip bantuan kustom yang meniru kenyamanan kerangka kerja Laravel.

### Mereset Database Anda (`fresh.sh`)

Gunakan skrip `fresh.sh` saat Anda berada di tahap awal pengembangan atau ingin memulai dengan basis data yang bersih. Skrip ini akan menghapus semua tabel, membuat satu file migrasi "snapshot" yang bersih dari entitas Anda saat ini, dan menerapkannya.

**Cara penggunaan:**
```bash
# Jadikan skrip dapat dieksekusi (hanya perlu dilakukan sekali)
chmod +x fresh.sh

# Jalankan skrip
./fresh.sh
````

Untuk menjalankan seeder setelah mereset, gunakan flag `--seed`:

```bash
./fresh.sh --seed
```

### Membuat Perubahan Inkremental (`migrate.sh`)

Setelah skema awal Anda stabil, gunakan `migrate.sh` untuk perubahan sehari-hari. Ini akan membuat file migrasi baru yang berisi perubahan spesifik Anda.

**Cara penggunaan:**

```bash
# Jadikan skrip dapat dieksekusi (hanya perlu dilakukan sekali)
chmod +x migrate.sh

# Jalankan skrip dengan nama deskriptif untuk perubahan tersebut
./migrate.sh "add_description_to_roles_table"
```

## 🏛️ Konsep Inti

### Factories

Factories adalah kelas yang dirancang untuk menghasilkan data palsu untuk satu entitas. Mereka sangat penting untuk seeding dan pengujian.

### Seeders

Seeders menggunakan factories untuk mengisi database dengan data. `DatabaseSeeder` utama akan mengatur semua seeder lainnya. Untuk menjalankan semua seeder, gunakan profil Spring `seeding`.

**Cara menjalankan Seeder:**

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=seeding
```

## ▶️ Menjalankan Aplikasi

Untuk menjalankan server aplikasi utama:

```bash
mvn spring-boot:run
```

Aplikasi akan tersedia di `http://localhost:8080`. Berkat Spring Boot DevTools, setiap perubahan yang Anda buat pada kode akan memicu restart otomatis.

## 🤝 Berkontribusi

Kontribusi sangat kami harapkan\! Silakan ajukan pull request.

1.  Fork repository.
2.  Buat branch fitur Anda (`git checkout -b feature/AmazingFeature`).
3.  Commit perubahan Anda (`git commit -m 'Add some AmazingFeature'`).
4.  Push ke branch (`git push origin feature/AmazingFeature`).
5.  Buka Pull Request.

## 📄 Lisensi

Proyek ini dilisensikan di bawah [Lisensi MIT](https://www.google.com/search?q=LICENSE).## 🛠️ Analisis Teknis

AuthID Core Engine adalah aplikasi Spring Boot yang dirancang untuk menyediakan fondasi backend yang kuat dengan fokus pada kontrol akses berbasis peran (RBAC), manajemen database, dan alur kerja pengembangan yang efisien.

### Arsitektur dan Teknologi

*   **Spring Boot:** Kerangka kerja utama yang digunakan untuk membangun aplikasi, menyediakan konfigurasi otomatis dan ekosistem yang kaya.
*   **Spring Security:** Digunakan untuk mengamankan aplikasi, termasuk otentikasi (OAuth2 Client, Resource Server, Authorization Server) dan otorisasi (RBAC).
*   **Spring Data JPA:** Memfasilitasi interaksi dengan database relasional menggunakan Hibernate sebagai implementasi JPA.
*   **Oracle Database:** Database relasional yang digunakan untuk penyimpanan data. Driver JDBC Oracle (UCP, ojdbc11, oraclepki) disertakan.
*   **Liquibase:** Digunakan untuk manajemen skema database, memungkinkan migrasi database yang terkontrol dan versioning. Integrasi dengan Hibernate (liquibase-hibernate6) memungkinkan pembuatan changelog berdasarkan entitas JPA.
*   **MapStruct:** Pustaka pemetaan kode-ke-kode yang digunakan untuk menghasilkan mappers yang cepat dan aman tipe antara objek DTO dan entitas JPA.
*   **Lombok:** Mengurangi boilerplate code dengan anotasi seperti `@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`, dll.
*   **Spring Shell:** Digunakan untuk membangun aplikasi command-line, kemungkinan untuk alat administrasi atau utilitas pengembangan.
*   **Spring Integration:** Digunakan untuk integrasi aplikasi, khususnya dengan HTTP dan JPA, menunjukkan kemampuan untuk membangun alur kerja yang kompleks.
*   **Apache POI:** Pustaka untuk membaca dan menulis format file Microsoft Office, menunjukkan potensi untuk fitur impor/ekspor data.
*   **Logstash Logback Encoder & Janino:** Digunakan untuk logging terstruktur, memfasilitasi analisis log terpusat.
*   **Spring Batch:** Digunakan untuk pemrosesan batch, menunjukkan kemampuan untuk menangani operasi data skala besar.
*   **Datafaker:** Pustaka untuk menghasilkan data palsu, sangat berguna untuk pengujian dan seeding database.
*   **ClassGraph:** Pustaka pemindaian classpath yang cepat, mungkin digunakan untuk penemuan komponen dinamis atau analisis runtime.

### Struktur Proyek

*   **`pom.xml`:** File konfigurasi Maven