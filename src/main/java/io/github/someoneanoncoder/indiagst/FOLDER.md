# indiagst (package root)

**Path:** src/main/java/io/github/someoneanoncoder/indiagst
**Purpose:** Public API value types and rules.

## Contents
| File | Purpose | Plan |
|---|---|---|
| Gstin.java | Validated GSTIN with check character | L2-01 |
| StateCode.java | State and union territory codes | L2-02 |
| StateTaxKind.java | SGST, UTGST or none | L2-02 |
| SupplyType.java | Intra-State or inter-State | L2-03 |
| PlaceOfSupply.java | Place-of-supply resolution | L2-03 |
| PricingMode.java | Tax-exclusive or tax-inclusive | L2-04 |
| TaxLineInput.java | One line to tax | L2-04 |
| TaxLine.java | Tax per line | L2-04 |
| TaxRateSummary.java | Per-rate summary row | L2-04 |
| TaxResult.java | Invoice totals, lines and summary | L2-04 |
| TaxCalculator.java | GST calculation | L2-04 |
| DocumentKind.java | GST document kinds and checklist accessors | L2-05 |
| DocumentField.java | Fields a document can require | L2-05 |
| FieldCondition.java | When a field rule applies | L2-05 |
| FieldRule.java | One checklist row with its citation | L2-05 |
| DocumentFact.java | Yes/no facts about a document | L2-05 |
| DocumentContext.java | Facts plus supply value | L2-05 |
| DocumentRules.java | The checklist tables (package-private) | L2-05 |
| HsnRules.java | HSN digits and format | L2-06 |
| HsnEntry.java | One HSN/SAC row | L2-06 |
| HsnMaster.java | HSN/SAC lookup | L2-06 |
| InvoiceNumber.java | Rule 46(b) serial number | L2-07 |
| InvoiceNumberRegistry.java | Uniqueness lookup contract | L2-07 |
| package-info.java | Package documentation | L2 |

## Sub-folders
- internal/ — non-exported helpers (GSTIN checksum)

## Rules for adding files here
- Public API nouns only; implementation details go in internal/.
- Cite the legal source of every rule in Javadoc.
