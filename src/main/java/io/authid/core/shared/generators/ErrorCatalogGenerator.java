// File: src/main/java/io/authid/core/generators/ErrorCatalogGenerator.java
package io.authid.core.shared.generators;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ErrorCatalogGenerator {

    private static final String MASTER_EXCEL_FILENAME = "ErrorsCatalogs.xlsx";
    private static final String MASTER_EXCEL_DIR = "src/main/resources/masters/";

    private static final String OUTPUT_JAVA_ROOT_DIR_RELATIVE = "src/main/java/";

    private static final Pattern ENUM_ENTRY_PATTERN = Pattern.compile(
        "\\s*(\\w+)\\((\".*?\"), (\"(?<category>.*?)\"), (\"(?<module>.*?)\"), (\"(?<baseKey>.*?)\"), (HttpStatus\\.\\w+), (\"(?<visibility>.*?)\")\\)");

    private static final String ENUM_TEMPLATE_HEADER = """
        // GENERATED FILE - DO NOT MODIFY MANUALLY
        // Generated from Excel Master Data: {0}
        package {1};
        
        import org.springframework.http.HttpStatus;
        import java.util.Arrays;
        import java.util.Collections;
        import java.util.List;
        import java.util.Set;
        import java.util.stream.Collectors;
        import io.authid.core.shared.components.i18n.extractors.I18n;
        
        public enum {2} {
        """;
    private static final String ENUM_TEMPLATE_FOOTER = """
            ; // End of enum entries
        
            private final String code;
            private final String category;
            private final String module;
            private final String baseMessageKey;
            private final HttpStatus httpStatus;
            private final List<String> additionalInfoVisibility;
            private final String titleKey;
            private final String debugDescriptionKey;
            private final String causeKey;
            private final String actionKey;
        
            private static final Set<String> VALID_PROFILES_FOR_GENERATION_CHECK = new HashSet<>(Arrays.asList("DEV", "SIT", "UAT", "BET", "PROD", "ALL"));
        
            {2}(String code, String category, String module, String baseMessageKey, HttpStatus httpStatus, String additionalInfoVisibilityString) {
                this.code = code;
                this.category = category;
                this.module = module;
                this.baseMessageKey = baseMessageKey;
                this.httpStatus = httpStatus;
                this.additionalInfoVisibility = parseVisibility(additionalInfoVisibilityString);
        
                I18n.setSourceClass({2}.class); 
                this.titleKey = I18n.extract(baseMessageKey + ".title");
                this.debugDescriptionKey = I18n.extract(baseMessageKey + ".debug");
                this.causeKey = I18n.extract(baseMessageKey + ".cause");
                this.actionKey = I18n.extract(baseMessageKey + ".action");
            }
        
            public String getCode() { return code; }
            public String getCategory() { return category; }
            public String getModule() { return module; }
            public String getBaseMessageKey() { return baseMessageKey; }
            public HttpStatus getHttpStatus() { return httpStatus; }
            public List<String> getAdditionalInfoVisibility() { return additionalInfoVisibility; }
            public String getMessageKey() { return baseMessageKey; }
            public String getTitleKey() { return titleKey; }
            public String getDebugDescriptionKey() { return debugDescriptionKey; }
            public String getCauseKey() { return causeKey; }
            public String getActionKey() { return actionKey; }
        
            private List<String> parseVisibility(String visibilityStr) {
                if ("ALL".equalsIgnoreCase(visibilityStr)) {
                    return List.of("DEV", "SIT", "UAT", "BET", "PROD");
                } else if (visibilityStr != null && !visibilityStr.isEmpty()) {
                    List<String> parsedProfiles = Arrays.asList(visibilityStr.toUpperCase().split(";")).stream().map(String::trim).collect(Collectors.toList());
                    return parsedProfiles;
                } else {
                    return Collections.emptyList();
                }
            }
        }
        """;


    public void generate() throws IOException, IllegalArgumentException {
        System.out.println("Starting Error Catalog generation from master Excel: " + MASTER_EXCEL_DIR + MASTER_EXCEL_FILENAME);

        String projectRoot = System.getProperty("user.dir") + File.separator;
        String outputJavaRoot = projectRoot + OUTPUT_JAVA_ROOT_DIR_RELATIVE;

        new File(outputJavaRoot).mkdirs();

        File masterExcelFile = new File(MASTER_EXCEL_DIR + MASTER_EXCEL_FILENAME);
        if (!masterExcelFile.exists()) {
            throw new IllegalArgumentException("Master Excel file '" + MASTER_EXCEL_FILENAME + "' not found in " + MASTER_EXCEL_DIR + ".");
        }

        Map<String, String> dummyCategoriesMap = Collections.emptyMap();
        Map<String, String> dummyModulesMap = Collections.emptyMap();
        Set<String> dummyValidEnvironments = Collections.emptySet();


        Map<String, Map<String, List<String>>> groupedEnumEntries = new LinkedHashMap<>();
        Set<String> processedGlobalErrorCodes = new HashSet<>();

        System.out.println("Reading and validating error definitions from Excel sheets...");

        processAllErrorDefinitionSheets(masterExcelFile, groupedEnumEntries, processedGlobalErrorCodes, dummyCategoriesMap, dummyModulesMap, dummyValidEnvironments);

        System.out.println("Finished reading Excel sheets. Found " + processedGlobalErrorCodes.size() + " unique error codes.");
        System.out.println("Detected " + groupedEnumEntries.size() + " enum classes to generate.");

        writeGroupedErrorCatalogs(masterExcelFile, outputJavaRoot, groupedEnumEntries);

        System.out.println("All enum catalogs generated successfully!");
    }

    // --- UTILITIES UNTUK MEMBACA EXCEL ---

    // Metode ini sekarang menerima FormulaEvaluator
    private String getCellValue(Cell cell, FormulaEvaluator evaluator) {
        if (cell == null) {
            return "";
        }
        DataFormatter formatter = new DataFormatter();

        if (cell.getCellType() == CellType.FORMULA) {
            if (evaluator == null) {
                System.err.println("ERROR: Attempted to evaluate formula in cell " + cell.getAddress() + " without a FormulaEvaluator. Returning formula string.");
                return cell.getCellFormula().trim();
            }
            try {
                // Evaluasi rumus, lalu gunakan resultType untuk format
                CellValue cellValue = evaluator.evaluate(cell);

                // --- WORKAROUND DI SINI ---
                // Hindari formatCellValue(CellValue) jika tidak ditemukan
                // Format CellValue secara manual atau konversikan ke tipe dasar
                switch (cellValue.getCellType()) {
                    case NUMERIC:
                        return formatter.formatCellValue(cell).trim(); // Coba format Cell asli
                    // atau String.valueOf(cellValue.getNumberValue());
                    case STRING:
                        return cellValue.getStringValue().trim();
                    case BOOLEAN:
                        return String.valueOf(cellValue.getBooleanValue()).trim();
                    case ERROR:
                        return FormulaError.forInt(cellValue.getErrorValue()).getString().trim();
                    case BLANK:
                        return "";
                    default:
                        return cellValue.formatAsString().trim(); // Fallback untuk format as string
                }
            } catch (Exception e) {
                System.err.println("ERROR: Failed to evaluate formula in cell " + cell.getAddress() + ". Returning formula string. Error: " + e.getMessage());
                return cell.getCellFormula().trim();
            }
        } else {
            return formatter.formatCellValue(cell).trim();
        }
    }

    // readLookupSheet dimodifikasi untuk membuat dan meneruskan FormulaEvaluator
    private Map<String, String> readLookupSheet(File excelFile, String sheetName, String keyColumnLetter, String valueColumnLetter) throws IOException {
        Map<String, String> lookupMap = new HashMap<>();
        try (FileInputStream inputStream = new FileInputStream(excelFile);
             Workbook workbook = new XSSFWorkbook(inputStream)) {
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator(); // Buat evaluator
            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                System.err.println("WARNING: Lookup sheet '" + sheetName + "' not found in " + excelFile.getName() + ". This might lead to validation errors.");
                return lookupMap;
            }
            int keyColIdx = ColumnLetterToIndex(keyColumnLetter);
            int valueColIdx = ColumnLetterToIndex(valueColumnLetter);
            for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
                Row row = sheet.getRow(rowNum);
                if (row == null || isRowEmpty(row)) continue;
                String key = getCellValue(row.getCell(keyColIdx), evaluator); // Teruskan evaluator
                String value = getCellValue(row.getCell(valueColIdx), evaluator); // Teruskan evaluator
                if (!key.isEmpty() && !value.isEmpty()) {
                    lookupMap.put(key, value);
                }
            }
        }
        return lookupMap;
    }

    // readLookupEnvironments dimodifikasi untuk membuat dan meneruskan FormulaEvaluator
    private Set<String> readLookupEnvironments(File excelFile) throws IOException {
        Set<String> environments = new HashSet<>();
        try (FileInputStream inputStream = new FileInputStream(excelFile);
             Workbook workbook = new XSSFWorkbook(inputStream)) {
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator(); // Buat evaluator
            Sheet sheet = workbook.getSheet("Lookup_Environments");
            if (sheet == null) {
                System.err.println("WARNING: Lookup_Environments sheet not found in " + excelFile.getName() + ". Environment validation might be inaccurate.");
                return environments;
            }
            for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
                Row row = sheet.getRow(rowNum);
                if (row == null || isRowEmpty(row)) continue;
                String envName = getCellValue(row.getCell(0), evaluator); // Teruskan evaluator
                if (!envName.isEmpty()) {
                    environments.add(envName.toUpperCase());
                }
            }
        }
        return environments;
    }

    private void processAllErrorDefinitionSheets(File masterExcelFile, Map<String, Map<String, List<String>>> groupedEnumEntries, Set<String> processedGlobalErrorCodes, Map<String, String> categoriesMap, Map<String, String> modulesMap, Set<String> validEnvironments) throws IOException {
        try (FileInputStream inputStream = new FileInputStream(masterExcelFile);
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator(); // Buat evaluator sekali per workbook

            int totalSheets = workbook.getNumberOfSheets();
            int sheetsProcessed = 0;

            for (int i = 0; i < totalSheets; i++) {
                Sheet sheet = workbook.getSheetAt(i);
                String sheetName = sheet.getSheetName();

                if ("ErrorMessages".equalsIgnoreCase(sheetName) || sheetName.startsWith("Lookup_")) {
                    continue; // Skip lookup and messages sheets
                }
                sheetsProcessed++;
                System.out.printf("  Processing sheet %d/%d: %s%n", sheetsProcessed, totalSheets, sheetName);

                for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
                    Row row = sheet.getRow(rowNum);
                    if (row == null || isRowEmpty(row)) continue;

                    // --- BACA KOLOM SESUAI INDEKS EXCEL ANDA (MULAI DARI 0) ---
                    // Setiap panggilan getCellValue harus meneruskan evaluator
                    String enumName = getCellValue(row.getCell(0), evaluator);                     // Kolom DomainEvent
                    String categoryFullName = getCellValue(row.getCell(1), evaluator);             // Kolom B
                    String moduleFullName = getCellValue(row.getCell(3), evaluator);               // Kolom D
                    String jsonCode = getCellValue(row.getCell(6), evaluator);                     // Kolom G (hasil rumus Excel)
                    String httpStatus = getCellValue(row.getCell(7), evaluator);                   // Kolom H
                    String baseMessageKey = getCellValue(row.getCell(8), evaluator);               // Kolom I
                    String additionalInfoVisibility = getCellValue(row.getCell(11), evaluator);    // Kolom L
                    String targetModulePackage = getCellValue(row.getCell(12), evaluator);         // Kolom M (Target Module Package)
                    String targetEnumClassName = getCellValue(row.getCell(13), evaluator);         // Kolom N (Target Enum Name)

                    // ... (validasi lainnya tetap sama) ...

                    if (enumName.isEmpty() || categoryFullName.isEmpty() || moduleFullName.isEmpty() ||
                        jsonCode.isEmpty() || baseMessageKey.isEmpty() || httpStatus.isEmpty() ||
                        targetModulePackage.isEmpty() || targetEnumClassName.isEmpty()) {
                        System.err.printf("    WARNING: Skipping row %d in sheet %s due to missing essential data.%n", rowNum + 1, sheetName);
                        continue;
                    }

                    // --- Perbaikan Utama: Konversi rawHttpStatus ke nama Enum HttpStatus ---
                    String httpStatusEnumName;
                    try {
                        // Integer.parseInt(rawHttpStatus) akan mengubah "404" menjadi 404
                        // HttpStatus.valueOf(int code) akan mencari enum konstanta yang sesuai (misal: NOT_FOUND)
                        httpStatusEnumName = HttpStatus.valueOf(Integer.parseInt(httpStatus)).name(); // Mendapatkan "NOT_FOUND"
                    } catch (IllegalArgumentException e) {
                        System.err.printf("    ERROR: Invalid HTTP Status '%s' for enum '%s' in sheet %s. Must be a valid HTTP status code (e.g., 200, 404). Skipping.%n", httpStatus, enumName, sheetName);
                        continue; // Lewati jika HTTP Status tidak valid
                    }

                    if (processedGlobalErrorCodes.contains(jsonCode)) {
                        System.err.printf("    ERROR: Duplicate JSON Error Code '%s' found for enum '%s' in sheet %s. This code is already used. Skipping.%n", jsonCode, enumName, sheetName);
                        continue;
                    }
                    processedGlobalErrorCodes.add(jsonCode);

                    String enumEntryString = String.format(
                        "%s(\"%s\", \"%s\", \"%s\", \"%s\", HttpStatus.%s, \"%s\")",
                        enumName, jsonCode, categoryFullName, moduleFullName, baseMessageKey, httpStatusEnumName, additionalInfoVisibility
                    );

                    String groupKey = targetModulePackage + "." + targetEnumClassName;
                    groupedEnumEntries.computeIfAbsent(groupKey, k -> new LinkedHashMap<>())
                        .computeIfAbsent(targetEnumClassName, k -> new ArrayList<>())
                        .add(enumEntryString);
                }
            }
        }
    }

    private void writeGroupedErrorCatalogs(File masterExcelFile, String outputRootPath, Map<String, Map<String, List<String>>> groupedEnumEntries) throws IOException {
        System.out.println("Starting to write generated enum files...");
        int enumsGenerated = 0;
        int totalEnums = groupedEnumEntries.size();

        for (Map.Entry<String, Map<String, List<String>>> packageEntry : groupedEnumEntries.entrySet()) {
            String fullEnumKey = packageEntry.getKey();
            String packageName = fullEnumKey.substring(0, fullEnumKey.lastIndexOf("."));
            String enumClassName = fullEnumKey.substring(fullEnumKey.lastIndexOf(".") + 1);

            String outputDirForPackage = outputRootPath + packageName.replace(".", File.separator) + File.separator;
            new File(outputDirForPackage).mkdirs();

            String filePath = outputDirForPackage + enumClassName + ".java";

            List<String> currentEnumEntries = packageEntry.getValue().get(enumClassName);

            if (currentEnumEntries == null || currentEnumEntries.isEmpty()) {
                System.out.printf("WARNING: No entries found for enum %s. Skipping generation.%n", enumClassName);
                continue;
            }
            enumsGenerated++;
            System.out.printf("  Generating enum %d/%d: %s.java%n", enumsGenerated, totalEnums, enumClassName);

            String allEnumEntries = String.join(",\n    ", currentEnumEntries);

            String header = String.format(ENUM_TEMPLATE_HEADER, masterExcelFile.getName(), packageName, enumClassName);
            String footer = String.format(ENUM_TEMPLATE_FOOTER, enumClassName);

            try (FileWriter writer = new FileWriter(filePath)) {
                writer.write(header);
                writer.write(allEnumEntries);
                writer.write(footer);
            }

            // Read existing entries for warnings about removed enums (optional, can be uncommented)
            // Map<String, String> existingEntriesForThisEnum = readExistingEnumEntries(filePath);
            // Set<String> newEnumNamesForThisFile = new HashSet<>();
            // for (String entryString : currentEnumEntries) {
            //      Matcher matcher = ENUM_ENTRY_PATTERN.matcher(entryString);
            //      if (matcher.find()) {
            //          newEnumNamesForThisFile.add(matcher.group(1));
            //      }
            // }
            // for (String existingEnumName : existingEntriesForThisEnum.keySet()) {
            //     if (!newEnumNamesForThisFile.contains(existingEnumName)) {
            //         System.out.println(String.format("    WARNING: Enum entry '%s' from existing %s.java is not found in the new Excel data. It will be removed in the generated file.", existingEnumName, enumClassName));
            //     }
            // }
        }
    }


    // --- HELPER METHODS ---
    // Pastikan semua helper method ini hanya didefinisikan SATU KALI di dalam kelas.

    private boolean isRowEmpty(Row row) {
        if (row == null) {
            return true;
        }
        for (int cellNum = row.getFirstCellNum(); cellNum < Math.min(row.getLastCellNum(), 50); cellNum++) {
            Cell cell = row.getCell(cellNum);
            if (cell != null && cell.getCellType() != CellType.BLANK && !getCellValue(cell, null).isEmpty()) { // Use getCellValue with null evaluator here
                return false;
            }
        }
        return true;
    }

    private int ColumnLetterToIndex(String letter) {
        int index = 0;
        for (char c : letter.toUpperCase().toCharArray()) {
            index = index * 26 + (c - 'A' + 1);
        }
        return index - 1;
    }

    // readExistingEnumEntries tetap ada (untuk dipanggil oleh writeGroupedErrorCatalogs jika perlu)
    private Map<String, String> readExistingEnumEntries(String filePath) throws IOException {
        Map<String, String> entries = new HashMap<>();
        File file = new File(filePath);
        if (!file.exists()) return entries;

        List<String> lines = Files.readAllLines(Paths.get(filePath));
        boolean inEnumBlock = false;
        Pattern enumClassPattern = Pattern.compile("public\\s+enum\\s+(\\w+)\\s*\\{");

        for (String line : lines) {
            Matcher classMatcher = enumClassPattern.matcher(line);
            if (classMatcher.find()) {
                inEnumBlock = true;
                continue;
            }

            if (inEnumBlock) {
                if (line.trim().equals(";") || line.trim().equals("};")) {
                    inEnumBlock = false;
                    break;
                }
                Matcher entryMatcher = ENUM_ENTRY_PATTERN.matcher(line.trim());
                if (entryMatcher.find()) {
                    entries.put(entryMatcher.group(1), line.trim());
                }
            }
        }
        return entries;
    }
}