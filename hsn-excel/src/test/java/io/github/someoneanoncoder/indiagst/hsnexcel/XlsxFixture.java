/*
 * File:      XlsxFixture.java
 * Module:    india-gst-hsn-excel
 * Layer:     test
 * Purpose:   Builds small synthetic .xlsx files for loader tests.
 * Owns:      Zip writing of shared strings and a first sheet.
 * Uses:      java.util.zip
 * Used by:   Gradle test tasks.
 * Not here:  Production code.
 * Reuse:     Test only.
 * Plan:      L2-06
 */
package io.github.someoneanoncoder.indiagst.hsnexcel;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/** A tiny xlsx writer: string cells go to shared strings, cells prefixed {@code #} are numbers. */
final class XlsxFixture {

  private XlsxFixture() {}

  static Path write(Path file, List<List<String>> rows) throws IOException {
    List<String> shared = new ArrayList<>();
    StringBuilder sheet = new StringBuilder("<worksheet><sheetData>");
    for (int r = 0; r < rows.size(); r++) {
      sheet.append("<row r=\"").append(r + 1).append("\">");
      for (int c = 0; c < rows.get(r).size(); c++) {
        String text = rows.get(r).get(c);
        String ref = (char) ('A' + c) + Integer.toString(r + 1);
        if (text.startsWith("#")) {
          sheet
              .append("<c r=\"")
              .append(ref)
              .append("\"><v>")
              .append(text.substring(1))
              .append("</v></c>");
        } else {
          shared.add(text);
          sheet
              .append("<c r=\"")
              .append(ref)
              .append("\" t=\"s\"><v>")
              .append(shared.size() - 1)
              .append("</v></c>");
        }
      }
      sheet.append("</row>");
    }
    sheet.append("</sheetData></worksheet>");
    StringBuilder strings = new StringBuilder("<sst>");
    for (String s : shared) {
      strings
          .append("<si><t>")
          .append(s.replace("&", "&amp;").replace("<", "&lt;"))
          .append("</t></si>");
    }
    strings.append("</sst>");
    return zip(
        file,
        "xl/sharedStrings.xml",
        strings.toString(),
        "xl/worksheets/sheet1.xml",
        sheet.toString());
  }

  static Path zip(Path file, String... namesAndContents) throws IOException {
    try (OutputStream out = Files.newOutputStream(file);
        ZipOutputStream zip = new ZipOutputStream(out)) {
      for (int i = 0; i < namesAndContents.length; i += 2) {
        zip.putNextEntry(new ZipEntry(namesAndContents[i]));
        zip.write(namesAndContents[i + 1].getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
      }
    }
    return file;
  }
}
