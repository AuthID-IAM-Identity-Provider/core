package io.authid.core.shared.commands.routing; // Sesuaikan dengan paket Anda

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.command.annotation.Command;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Command(command = "route", alias = "r", description = "Commands for viewing application routes.")
@Component
public class RouteCommand {

    private final RequestMappingHandlerMapping requestMappingHandlerMapping;

    @Autowired
    public RouteCommand(RequestMappingHandlerMapping requestMappingHandlerMapping) {
        this.requestMappingHandlerMapping = requestMappingHandlerMapping;
    }

    @Command(command = "list", alias = "ls", description = "Lists all registered HTTP routes in a responsive tabular format.")
    public String listRoutes() {
        StringBuilder tableBuilder = new StringBuilder();

        // 1. Definisikan header kolom
        final String HEADER_METHOD = "METHOD";
        final String HEADER_URI = "URI";
        final String HEADER_HANDLER = "HANDLER";
        final String HEADER_NAME = "NAME";

        // List untuk menyimpan data per baris
        List<List<String>> rows = new ArrayList<>();

        // 2. Ambil dan olah data terlebih dahulu untuk menghitung lebar kolom
        Map<RequestMappingInfo, org.springframework.web.method.HandlerMethod> handlerMethods =
                requestMappingHandlerMapping.getHandlerMethods();

        // Tambahkan header ke data untuk perhitungan lebar
        List<String> headerRow = List.of(HEADER_METHOD, HEADER_URI, HEADER_HANDLER, HEADER_NAME);
        rows.add(headerRow);

        // Map data ke format string yang akan ditampilkan, lalu tambahkan ke list 'rows'
        List<Map.Entry<RequestMappingInfo, org.springframework.web.method.HandlerMethod>> sortedHandlerMethods =
                handlerMethods.entrySet().stream()
                        .sorted(Comparator.comparing(entry -> {
                            Set<String> patterns = entry.getKey().getPatternValues();
                            return patterns.isEmpty() ? "" : patterns.iterator().next(); // Sortir berdasarkan URI pertama
                        }))
                        .toList();

        for (Map.Entry<RequestMappingInfo, org.springframework.web.method.HandlerMethod> entry : sortedHandlerMethods) {
            RequestMappingInfo mapping = entry.getKey();
            org.springframework.web.method.HandlerMethod handlerMethod = entry.getValue();

            Set<String> uris = mapping.getPatternValues();
            Set<org.springframework.web.bind.annotation.RequestMethod> methods = mapping.getMethodsCondition().getMethods();

            String httpMethods = methods.isEmpty() ? "ALL" :
                    methods.stream().map(Enum::name).collect(Collectors.joining(","));
            String uri = uris.isEmpty() ? "/" :
                    uris.stream().sorted().collect(Collectors.joining(", "));
            String handler = handlerMethod.getBeanType().getSimpleName() + "@" + handlerMethod.getMethod().getName();

            // Logika untuk kolom NAME:
            String name = "";
            // Coba ambil dari mapping name, ini bisa jadi @RequestMapping(name = "someName")
            if (mapping.getName() != null) {
                name = mapping.getName();
            }
            // Jika tidak ada mapping name, fallback ke nama metode handler
            if (name.isEmpty()) {
                name = handlerMethod.getMethod().getName();
            }
            // Jika nama metode juga tidak ideal, bisa juga mencoba nama bean controller
            // if (name.isEmpty() && handlerMethod.getBean() instanceof String) {
            //     name = (String) handlerMethod.getBean();
            // }


            rows.add(List.of(httpMethods, uri, handler, name));
        }

        // 3. Hitung lebar kolom maksimum secara dinamis
        int[] columnWidths = new int[headerRow.size()];
        for (List<String> row : rows) {
            for (int i = 0; i < row.size(); i++) {
                columnWidths[i] = Math.max(columnWidths[i], row.get(i).length());
            }
        }

        // Tambahkan padding ekstra untuk estetika
        int padding = 2; // Contoh padding
        for (int i = 0; i < columnWidths.length; i++) {
            columnWidths[i] += padding;
        }

        // 4. Bangun format string untuk baris (header dan data) dan separator
        StringBuilder headerAndRowFormat = new StringBuilder("|");
        StringBuilder separatorFormat = new StringBuilder("+");
        for (int width : columnWidths) {
            headerAndRowFormat.append(" %-").append(width).append("s |");
            separatorFormat.append("-".repeat(width + 1)).append("+"); // +1 for the space after content
        }
        headerAndRowFormat.append("\n");
        separatorFormat.append("\n");

        // 5. Bangun tabel utama
        // Baris separator atas
        tableBuilder.append(separatorFormat);
        // Header
        tableBuilder.append(String.format(headerAndRowFormat.toString(), (Object[]) headerRow.toArray()));
        // Baris separator di bawah header
        tableBuilder.append(separatorFormat);

        // Data rows (mulai dari indeks 1 karena indeks 0 adalah header)
        for (int i = 1; i < rows.size(); i++) {
            List<String> row = rows.get(i);
            // Potong string jika terlalu panjang saat diformat, menggunakan lebar kolom yang sudah dihitung
            List<String> truncatedRow = new ArrayList<>();
            for (int j = 0; j < row.size(); j++) {
                truncatedRow.add(truncateString(row.get(j), columnWidths[j] - padding)); // Kurangi padding untuk cek trunc
            }
            tableBuilder.append(String.format(headerAndRowFormat.toString(), (Object[]) truncatedRow.toArray()));
        }

        // Footer tabel
        tableBuilder.append(separatorFormat);

        return tableBuilder.toString();
    }

    /**
     * Helper method to truncate strings that are too long for table columns.
     * Note: maxLength here is content length, not including padding.
     */
    private String truncateString(String text, int maxLength) {
        if (text.length() > maxLength && maxLength > 3) { // Hanya potong jika ada ruang untuk "..."
            return text.substring(0, maxLength - 3) + "...";
        } else if (text.length() > maxLength) { // Jika kolom sangat sempit
            return text.substring(0, maxLength); // Potong saja tanpa "..."
        }
        return text;
    }
}