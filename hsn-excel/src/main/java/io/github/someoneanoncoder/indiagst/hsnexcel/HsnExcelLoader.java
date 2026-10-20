/*
 * File:      HsnExcelLoader.java
 * Module:    india-gst-hsn-excel
 * Layer:     io
 * Purpose:   Loads an HsnMaster from an HSN/SAC master spreadsheet (.xlsx).
 * Owns:      Header detection, code normalisation and skipped-row counting.
 * Uses:      XlsxSheetReader, HsnMaster, HsnEntry, HsnRules.
 * Used by:   Applications importing the official master.
 * Not here:  Downloading the file, legacy .xls files, GST rates.
 * Reuse:     Static.
 * Plan:      L2-06
 */
package io.github.someoneanoncoder.indiagst.hsnexcel;

import io.github.someoneanoncoder.indiagst.HsnEntry;
import io.github.someoneanoncoder.indiagst.HsnMaster;
import io.github.someoneanoncoder.indiagst.HsnRules;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Reads the first worksheet of an {@code .xlsx} file. The header row is the first row that has a
 * cell mentioning "hsn" or "sac" (the code column) and a cell mentioning "description".
 *
 * <p><strong>Not verified against the official file:</strong> the layout of the GST portal's
 * HSN/SAC master could not be downloaded when this was written; tests use a synthetic workbook.
 * Numeric code cells lose leading zeros, so an odd-length numeric code gets one zero in front. Rows
 * whose code is not a well-formed HSN/SAC code are skipped and counted.
 */
public final class HsnExcelLoader {

  private HsnExcelLoader() {}

  /**
   * Loads a master from a file.
   *
   * @param file path of an .xlsx file chosen by the user
   * @return the entries and the number of skipped rows
   * @throws IOException if the file cannot be read, is not xlsx, is too large, or has no header row
   */
  public static HsnLoadResult load(Path file) throws IOException {
    List<List<String>> rows = XlsxSheetReader.rowsRead(file);
    int headerAt = -1;
    int codeColumn = -1;
    int descriptionColumn = -1;
    for (int r = 0; r < rows.size() && headerAt < 0; r++) {
      codeColumn = columnMentioning(rows.get(r), "hsn", "sac");
      descriptionColumn = columnMentioning(rows.get(r), "description");
      if (codeColumn >= 0 && descriptionColumn >= 0) {
        headerAt = r;
      }
    }
    if (headerAt < 0) {
      throw new IOException("no header row with an HSN/SAC column and a description column");
    }
    return entriesRead(rows, headerAt, codeColumn, descriptionColumn);
  }

  private static HsnLoadResult entriesRead(
      List<List<String>> rows, int headerAt, int codeColumn, int descriptionColumn) {
    List<HsnEntry> entries = new ArrayList<>();
    int skipped = 0;
    for (List<String> row : rows.subList(headerAt + 1, rows.size())) {
      String code = cell(row, codeColumn).strip();
      if (code.endsWith(".0")) {
        code = code.substring(0, code.length() - 2);
      }
      if (code.length() % 2 == 1) {
        code = "0" + code;
      }
      if (HsnRules.isValidFormat(code)) {
        entries.add(new HsnEntry(code, cell(row, descriptionColumn).strip()));
      } else if (!row.stream().allMatch(String::isBlank)) {
        skipped++;
      }
    }
    return new HsnLoadResult(HsnMaster.of(entries), skipped);
  }

  private static int columnMentioning(List<String> row, String... words) {
    for (int i = 0; i < row.size(); i++) {
      String text = row.get(i).toLowerCase(Locale.ROOT);
      for (String word : words) {
        if (text.contains(word)) {
          return i;
        }
      }
    }
    return -1;
  }

  private static String cell(List<String> row, int column) {
    return column < row.size() ? row.get(column) : "";
  }
}
