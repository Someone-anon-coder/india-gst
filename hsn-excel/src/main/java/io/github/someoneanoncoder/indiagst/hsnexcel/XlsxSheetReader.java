/*
 * File:      XlsxSheetReader.java
 * Module:    india-gst-hsn-excel
 * Layer:     io
 * Purpose:   Reads the cell text of the first worksheet of an .xlsx file with the JDK only.
 * Owns:      Zip and XML parsing with size limits and XXE protection.
 * Uses:      java.util.zip, javax.xml.stream.
 * Used by:   HsnExcelLoader.
 * Not here:  HSN meaning of columns (HsnExcelLoader); .xls (legacy) files; formulas and styles.
 * Reuse:     Package-private.
 * Plan:      L2-06
 */
package io.github.someoneanoncoder.indiagst.hsnexcel;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/** Minimal xlsx reader: shared strings, inline strings and numbers of the first sheet. */
final class XlsxSheetReader {

  private static final long MAX_ENTRY_BYTES = 128L * 1024 * 1024;
  private static final int MAX_ROWS = 1_000_000;
  private static final int MAX_COLUMNS = 256;

  private XlsxSheetReader() {}

  /**
   * Reads all rows of {@code xl/worksheets/sheet1.xml}.
   *
   * @param file the .xlsx file
   * @return rows of cell text; missing cells are empty strings
   * @throws IOException if the file is not a readable xlsx, has no first sheet, or exceeds limits
   */
  static List<List<String>> rowsRead(Path file) throws IOException {
    try (ZipFile zip = new ZipFile(file.toFile())) {
      List<String> shared = sharedStringsRead(zip);
      ZipEntry sheet = zip.getEntry("xl/worksheets/sheet1.xml");
      if (sheet == null) {
        throw new IOException("xlsx has no xl/worksheets/sheet1.xml");
      }
      return sheetRead(zip, sheet, shared);
    } catch (XMLStreamException e) {
      throw new IOException("xlsx contains invalid XML", e);
    }
  }

  private static XMLStreamReader open(ZipFile zip, ZipEntry entry)
      throws IOException, XMLStreamException {
    if (entry.getSize() > MAX_ENTRY_BYTES) {
      throw new IOException("xlsx entry too large: " + entry.getName());
    }
    XMLInputFactory factory = XMLInputFactory.newInstance();
    factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
    factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
    InputStream in = new LimitedInputStream(zip.getInputStream(entry), MAX_ENTRY_BYTES);
    return factory.createXMLStreamReader(in);
  }

  private static List<String> sharedStringsRead(ZipFile zip)
      throws IOException, XMLStreamException {
    ZipEntry entry = zip.getEntry("xl/sharedStrings.xml");
    List<String> strings = new ArrayList<>();
    if (entry == null) {
      return strings;
    }
    XMLStreamReader xml = open(zip, entry);
    StringBuilder current = null;
    while (xml.hasNext()) {
      int event = xml.next();
      if (event == XMLStreamConstants.START_ELEMENT && "si".equals(xml.getLocalName())) {
        current = new StringBuilder();
      } else if (event == XMLStreamConstants.START_ELEMENT
          && "t".equals(xml.getLocalName())
          && current != null) {
        current.append(xml.getElementText());
      } else if (event == XMLStreamConstants.END_ELEMENT && "si".equals(xml.getLocalName())) {
        strings.add(current == null ? "" : current.toString());
        current = null;
      }
    }
    return strings;
  }

  private static List<List<String>> sheetRead(ZipFile zip, ZipEntry sheet, List<String> shared)
      throws IOException, XMLStreamException {
    XMLStreamReader xml = open(zip, sheet);
    List<List<String>> rows = new ArrayList<>();
    List<String> row = null;
    while (xml.hasNext()) {
      int event = xml.next();
      if (event == XMLStreamConstants.START_ELEMENT) {
        String name = xml.getLocalName();
        if ("row".equals(name)) {
          row = new ArrayList<>();
        } else if ("c".equals(name) && row != null) {
          cellRead(xml, row, shared);
        }
      } else if (event == XMLStreamConstants.END_ELEMENT && "row".equals(xml.getLocalName())) {
        rows.add(row);
        row = null;
        if (rows.size() > MAX_ROWS) {
          throw new IOException("xlsx has too many rows");
        }
      }
    }
    return rows;
  }

  private static void cellRead(XMLStreamReader xml, List<String> row, List<String> shared)
      throws XMLStreamException, IOException {
    String type = xml.getAttributeValue(null, "t");
    int column = columnOf(xml.getAttributeValue(null, "r"));
    String text = "";
    while (xml.hasNext()) {
      int event = xml.next();
      if (event == XMLStreamConstants.START_ELEMENT
          && ("v".equals(xml.getLocalName()) || "t".equals(xml.getLocalName()))) {
        text += xml.getElementText();
      } else if (event == XMLStreamConstants.END_ELEMENT && "c".equals(xml.getLocalName())) {
        break;
      }
    }
    while (row.size() < column) {
      row.add("");
    }
    row.add("s".equals(type) ? sharedText(shared, text) : text);
  }

  private static String sharedText(List<String> shared, String index) throws IOException {
    try {
      return shared.get(Integer.parseInt(index.strip()));
    } catch (NumberFormatException | IndexOutOfBoundsException e) {
      throw new IOException("xlsx shared string index is invalid", e);
    }
  }

  private static int columnOf(String reference) throws IOException {
    if (reference == null) {
      throw new IOException("xlsx cell without reference");
    }
    int column = 0;
    int i = 0;
    while (i < reference.length() && Character.isLetter(reference.charAt(i))) {
      column = column * 26 + (Character.toUpperCase(reference.charAt(i)) - 'A' + 1);
      i++;
    }
    if (column < 1 || column > MAX_COLUMNS) {
      throw new IOException("xlsx column out of range: " + reference);
    }
    return column - 1;
  }
}
