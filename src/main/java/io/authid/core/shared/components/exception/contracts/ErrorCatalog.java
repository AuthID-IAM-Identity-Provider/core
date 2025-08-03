package io.authid.core.shared.components.exception.contracts;

import org.springframework.http.HttpStatus;

/**
 * Mendefinisikan kontrak standar untuk semua enum katalog error.
 * Dengan mengimplementasikan interface ini, setiap enum error dari domain yang berbeda
 * dapat digunakan secara polimorfik oleh sistem penanganan error utama.
 */
public interface ErrorCatalog {

    /**
     * Mengembalikan kode error unik yang dapat dibaca oleh mesin.
     * <p>
     * Contoh: "RES-USR-0001"
     *
     * @return String kode error.
     */
    String getCode();

    /**
     * Mengembalikan kunci dasar (base key) untuk proses terjemahan (i18n).
     * <p>
     * Contoh: "error.user.not.found"
     *
     * @return String kunci pesan.
     */
    String getBaseMessageKey();

    /**
     * Mengembalikan status HTTP yang sesuai untuk error ini.
     *
     * @return HttpStatus enum dari Spring Framework.
     */
    HttpStatus getHttpStatus();

    /**
     * Mengembalikan kategori umum dari error.
     * <p>
     * Contoh: "Resource", "Business", "Validation"
     *
     * @return String nama kategori.
     */
    String getCategory();

    String getModule();
}