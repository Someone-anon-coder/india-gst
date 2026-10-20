/*
 * File:      HsnExcelLoaderSecurityTest.java
 * Module:    india-gst-hsn-excel
 * Layer:     test
 * Purpose:   Security tests: spreadsheets are untrusted files.
 * Owns:      XXE, entity expansion, bad indices, huge columns and decompression limits.
 * Uses:      HsnExcelLoader
 * Used by:   Gradle test tasks.
 * Not here:  Production code.
 * Reuse:     Test only.
 * Plan:      L2-06
 */
package io.github.someoneanoncoder.indiagst.hsnexcel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.someoneanoncoder.indiagst.hsnexcel.testing.SecurityTest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;

@SecurityTest
class HsnExcelLoaderSecurityTest {

  @TempDir Path dir;

  private static String sheet(String cells) {
    return "<worksheet><sheetData><row>" + cells + "</row></sheetData></worksheet>";
  }

  @Test
  @DisplayName("an external entity pointing at a local file is not resolved")
  void shouldNotResolveExternalEntities_whenSheetDeclaresDoctype() throws IOException {
    Path secret = Files.writeString(dir.resolve("secret.txt"), "TOP-SECRET");
    String xml =
        "<?xml version=\"1.0\"?><!DOCTYPE x [<!ENTITY e SYSTEM \""
            + secret.toUri()
            + "\">]>"
            + "<worksheet><sheetData><row><c r=\"A1\" t=\"inlineStr\"><is><t>&e;</t></is></c></row></sheetData></worksheet>";
    Path file = XlsxFixture.zip(dir.resolve("xxe.xlsx"), "xl/worksheets/sheet1.xml", xml);
    assertThatThrownBy(() -> HsnExcelLoader.load(file))
        .isInstanceOf(IOException.class)
        .satisfies(
            e ->
                assertThat(String.valueOf(e.getMessage()) + e.getCause())
                    .doesNotContain("TOP-SECRET"));
  }

  @Test
  @Timeout(10)
  @DisplayName("an entity expansion bomb does not exhaust memory")
  void shouldRefuse_whenEntitiesExpandExponentially() throws IOException {
    String xml =
        "<?xml version=\"1.0\"?><!DOCTYPE lolz [<!ENTITY a \"aaaaaaaaaa\"><!ENTITY b \"&a;&a;&a;&a;&a;&a;&a;&a;&a;&a;\">"
            + "<!ENTITY c \"&b;&b;&b;&b;&b;&b;&b;&b;&b;&b;\"><!ENTITY d \"&c;&c;&c;&c;&c;&c;&c;&c;&c;&c;\">]>"
            + "<worksheet><sheetData><row><c r=\"A1\" t=\"inlineStr\"><is><t>&d;</t></is></c></row></sheetData></worksheet>";
    Path file = XlsxFixture.zip(dir.resolve("lolz.xlsx"), "xl/worksheets/sheet1.xml", xml);
    assertThatThrownBy(() -> HsnExcelLoader.load(file)).isInstanceOf(IOException.class);
  }

  @Test
  @DisplayName("a shared string index outside the table is an IOException")
  void shouldFail_whenSharedStringIndexIsInvalid() throws IOException {
    Path file =
        XlsxFixture.zip(
            dir.resolve("index.xlsx"),
            "xl/sharedStrings.xml",
            "<sst><si><t>HSN</t></si></sst>",
            "xl/worksheets/sheet1.xml",
            sheet("<c r=\"A1\" t=\"s\"><v>99</v></c>"));
    assertThatThrownBy(() -> HsnExcelLoader.load(file)).isInstanceOf(IOException.class);
    Path negative =
        XlsxFixture.zip(
            dir.resolve("neg.xlsx"),
            "xl/worksheets/sheet1.xml",
            sheet("<c r=\"A1\" t=\"s\"><v>abc</v></c>"));
    assertThatThrownBy(() -> HsnExcelLoader.load(negative)).isInstanceOf(IOException.class);
  }

  @Test
  @DisplayName("absurd column references and missing references are refused")
  void shouldFail_whenCellReferenceIsAbsurd() throws IOException {
    Path far =
        XlsxFixture.zip(
            dir.resolve("far.xlsx"),
            "xl/worksheets/sheet1.xml",
            sheet("<c r=\"ZZZZZZ1\"><v>1</v></c>"));
    assertThatThrownBy(() -> HsnExcelLoader.load(far)).isInstanceOf(IOException.class);
    Path none =
        XlsxFixture.zip(
            dir.resolve("none.xlsx"), "xl/worksheets/sheet1.xml", sheet("<c><v>1</v></c>"));
    assertThatThrownBy(() -> HsnExcelLoader.load(none)).isInstanceOf(IOException.class);
  }

  @Test
  @DisplayName("malformed XML is an IOException, not a runtime exception")
  void shouldFail_whenXmlIsBroken() throws IOException {
    Path file =
        XlsxFixture.zip(
            dir.resolve("broken.xlsx"), "xl/worksheets/sheet1.xml", "<worksheet><sheetData><row>");
    assertThatThrownBy(() -> HsnExcelLoader.load(file)).isInstanceOf(IOException.class);
  }

  @Test
  @DisplayName("the decompressed-size guard stops a stream that exceeds its limit")
  void shouldStop_whenLimitedStreamIsOverRead() throws IOException {
    try (java.io.InputStream in =
        new LimitedInputStream(new java.io.ByteArrayInputStream(new byte[100]), 10)) {
      assertThatThrownBy(() -> in.readAllBytes()).isInstanceOf(IOException.class);
    }
    try (java.io.InputStream in =
        new LimitedInputStream(new java.io.ByteArrayInputStream(new byte[100]), 10)) {
      assertThatThrownBy(
              () -> {
                for (int i = 0; i < 11; i++) {
                  in.read();
                }
              })
          .isInstanceOf(IOException.class);
    }
    try (java.io.InputStream in =
        new LimitedInputStream(new java.io.ByteArrayInputStream(new byte[5]), 10)) {
      assertThat(in.readAllBytes()).hasSize(5);
    }
  }
}
