/*
 * File:      HsnExcelLoaderUnitTest.java
 * Module:    india-gst-hsn-excel
 * Layer:     test
 * Purpose:   Unit tests for HsnExcelLoader.
 * Owns:      Header detection, numeric and text codes, skipped rows and error cases.
 * Uses:      HsnExcelLoader, XlsxSheetReader
 * Used by:   Gradle test tasks.
 * Not here:  Production code.
 * Reuse:     Test only.
 * Plan:      L2-06
 */
package io.github.someoneanoncoder.indiagst.hsnexcel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.someoneanoncoder.indiagst.HsnEntry;
import io.github.someoneanoncoder.indiagst.hsnexcel.testing.UnitTest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@UnitTest
class HsnExcelLoaderUnitTest {

  @TempDir Path dir;

  @Test
  @DisplayName("loads text and numeric codes below a title row and header")
  void shouldLoadEntries_whenHeaderFollowsTitleRows() throws IOException {
    Path file =
        XlsxFixture.write(
            dir.resolve("hsn.xlsx"),
            List.of(
                List.of("HSN Master (synthetic)"),
                List.of("S.No", "HSN Code", "Description"),
                List.of("1", "8471", "Computers & <parts>"),
                List.of("2", "#101", "Live horses"),
                List.of("3", "#998314.0", "IT services"),
                List.of("4", "n/a", "not a code"),
                List.of("", "", ""),
                List.of("5", "84713010")));
    HsnLoadResult result = HsnExcelLoader.load(file);
    assertThat(result.master().size()).isEqualTo(4);
    assertThat(result.master().find("8471")).hasValue(new HsnEntry("8471", "Computers & <parts>"));
    assertThat(result.master().find("0101")).hasValue(new HsnEntry("0101", "Live horses"));
    assertThat(result.master().find("998314")).isPresent();
    assertThat(result.master().find("84713010").orElseThrow().description()).isEmpty();
    assertThat(result.skippedRows()).isEqualTo(1);
  }

  @Test
  @DisplayName("a SAC header and description in other columns is understood")
  void shouldFindColumns_whenOrderDiffers() throws IOException {
    Path file =
        XlsxFixture.write(
            dir.resolve("sac.xlsx"),
            List.of(List.of("Service Description", "SAC Code"), List.of("Legal services", "9982")));
    assertThat(HsnExcelLoader.load(file).master().find("9982").orElseThrow().description())
        .isEqualTo("Legal services");
  }

  @Test
  @DisplayName("a workbook without a header row is refused")
  void shouldFail_whenNoHeaderRow() throws IOException {
    Path file =
        XlsxFixture.write(dir.resolve("none.xlsx"), List.of(List.of("a", "b"), List.of("1", "2")));
    assertThatThrownBy(() -> HsnExcelLoader.load(file))
        .isInstanceOf(IOException.class)
        .hasMessageContaining("header");
  }

  @Test
  @DisplayName("a zip without a first sheet and a non-zip file are refused")
  void shouldFail_whenFileIsNotAWorkbook() throws IOException {
    Path empty = XlsxFixture.zip(dir.resolve("empty.xlsx"), "readme.txt", "x");
    assertThatThrownBy(() -> HsnExcelLoader.load(empty)).isInstanceOf(IOException.class);
    Path text = Files.writeString(dir.resolve("text.xlsx"), "not a zip");
    assertThatThrownBy(() -> HsnExcelLoader.load(text)).isInstanceOf(IOException.class);
    assertThatThrownBy(() -> HsnExcelLoader.load(dir.resolve("missing.xlsx")))
        .isInstanceOf(IOException.class);
  }

  @Test
  @DisplayName("a sheet that uses inline strings is read")
  void shouldReadInlineStrings_whenWorkbookHasNoSharedStrings() throws IOException {
    String sheet =
        "<worksheet><sheetData><row><c r=\"A1\" t=\"inlineStr\"><is><t>HSN</t></is></c>"
            + "<c r=\"B1\" t=\"inlineStr\"><is><t>Description</t></is></c></row>"
            + "<row><c r=\"A2\"><v>8501</v></c><c r=\"B2\" t=\"inlineStr\"><is><t>Motors</t></is></c></row>"
            + "</sheetData></worksheet>";
    Path file = XlsxFixture.zip(dir.resolve("inline.xlsx"), "xl/worksheets/sheet1.xml", sheet);
    assertThat(HsnExcelLoader.load(file).master().find("8501").orElseThrow().description())
        .isEqualTo("Motors");
  }
}
