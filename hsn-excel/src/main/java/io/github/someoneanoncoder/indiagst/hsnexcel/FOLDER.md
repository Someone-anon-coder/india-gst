# hsnexcel

**Path:** hsn-excel/src/main/java/io/github/someoneanoncoder/indiagst/hsnexcel
**Purpose:** Read an HSN/SAC master spreadsheet into an HsnMaster.

## Contents
| File | Purpose | Plan |
|---|---|---|
| HsnExcelLoader.java | Header detection and entry extraction | L2-06 |
| HsnLoadResult.java | Master plus skipped-row count | L2-06 |
| XlsxSheetReader.java | Minimal xlsx reader (package-private) | L2-06 |
| LimitedInputStream.java | Decompression size guard (package-private) | L2-06 |
| package-info.java | Package documentation | L2-06 |

## Rules for adding files here
- No third-party dependencies; every input is untrusted.
