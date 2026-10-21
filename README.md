# india-gst

GST rules for Indian billing software in plain Java: validate a **GSTIN** (including the check character), look up **state codes**, decide **intra- or inter-State** supply, calculate **CGST/SGST/UTGST/IGST and cess** for tax-exclusive and tax-inclusive (MRP) prices, check which **fields a GST document needs**, apply the **HSN digit rule**, and validate **invoice numbers**. No portal access. Java 25, JPMS module `io.github.someoneanoncoder.indiagst`, MIT licence.

> **Not tax advice; rules as of 20 October 2026; verify against CBIC notifications.**
> GST law changes by notification. This library encodes rules read from the sources listed below; several could only be checked against secondary reproductions (see "What is and is not verified"). Check every rule against the current CBIC text before relying on it for filing or compliance, and ask your chartered accountant about edge cases.

Status: `0.1.0`. Depends on [`india-money`](https://github.com/Someone-anon-coder/india-money) for `Money` (exposed transitively); no other runtime dependency.

## Install

JitPack (first release). This is a multi-module build, so JitPack publishes under the group `com.github.Someone-anon-coder.india-gst`:

```kotlin
repositories { maven("https://jitpack.io") }
dependencies {
    implementation("com.github.Someone-anon-coder.india-gst:india-gst:v0.1.0")
    // optional: load the official HSN/SAC master from .xlsx (JDK only, no third-party jars)
    // implementation("com.github.Someone-anon-coder.india-gst:hsn-excel:v0.1.0")
}
```

Do not use `com.github.Someone-anon-coder:india-gst`: that root coordinate is an aggregate that also pulls in `hsn-excel`. If you restrict the JitPack repository by group, allow both `com.github.Someone-anon-coder` (for `india-money`, a transitive dependency) and `com.github.Someone-anon-coder.india-gst`.

Maven Central coordinates `io.github.someone-anon-coder:india-gst` follow before v1.0.0.

## Examples

### 1. GSTIN and state codes

```java
Gstin gstin = Gstin.parse("27AAPFU0939F1ZV");
gstin.state().displayName();             // Maharashtra
gstin.pan();                             // AAPFU0939F
Gstin.isValid("27AAPFU0939F1ZO");        // false: wrong check character
Gstin.generate("27AAPFU0939F1Z");        // 27AAPFU0939F1ZV (appends the check character)
```

### 2. Tax-exclusive invoice, same State, two rates, round-off

```java
PlaceOfSupply place = PlaceOfSupply.forDelivery(StateCode.MAHARASHTRA, StateCode.MAHARASHTRA);
TaxResult result = TaxCalculator.calculate(place, PricingMode.EXCLUSIVE, List.of(
    TaxLineInput.of(Money.ofRupees(1000), new BigDecimal("18")),
    new TaxLineInput(Money.ofRupees(500), Money.ofRupees(50), new BigDecimal("5"), BigDecimal.ZERO)));
result.centralTaxTotal();                // INR 101.25   (state tax is the same)
result.grandTotal();                     // INR 1652.50
RoundOff.of(result.grandTotal(), RoundOffMode.NEAREST_RUPEE).rounded();  // INR 1653.00 (india-money)
```

### 3. MRP (tax-inclusive) price sold to another State

```java
PlaceOfSupply place = PlaceOfSupply.resolve(StateCode.MAHARASHTRA, Optional.empty(),
    Optional.of(Gstin.parse("33AAACC1206D1ZN")));        // buyer's GSTIN decides the place
TaxResult result = TaxCalculator.calculate(place, PricingMode.INCLUSIVE,
    List.of(TaxLineInput.of(Money.ofRupees(118), new BigDecimal("18"))));
result.taxableTotal();                   // INR 100.00
result.integratedTaxTotal();             // INR 18.00 (IGST); the line always totals the MRP
```

### 4. Document checklist, HSN digits, invoice number

```java
DocumentContext context = DocumentContext.of(Money.ofRupees(60_000), DocumentFact.GOODS, DocumentFact.INTER_STATE);
DocumentKind.TAX_INVOICE.requiredFields(context);       // includes PLACE_OF_SUPPLY, RECIPIENT_STATE_NAME_AND_CODE ...
DocumentKind.TAX_INVOICE.missingFields(printedFields, context);
HsnRules.requiredDigits(Money.ofRupees(7_00_00_000L));  // 6 (turnover above five crore)
InvoiceNumber.of("A/25-26/", 123, 6);                   // A/25-26/000123
InvoiceNumber.isValid("A/25-26/0001234567");            // false: longer than 16 characters
```

All four examples run as tests (`ReadmeExamplesUnitTest`).

## Sources

| Rule | Source | Where it is used |
|---|---|---|
| Tax invoice particulars, 16-character serial number | CGST Rules, 2017, rule 46 (CBIC consolidation of 24 Sep 2021, [cbic-gst.gov.in](https://cbic-gst.gov.in/pdf/24092021-CGST-Rules-2017-Part-A-Rules.pdf)) | `DocumentKind.TAX_INVOICE`, `InvoiceNumber` |
| Value of supply inclusive of tax (MRP): tax amount formula | CGST Rules, rule 35 (same consolidation) | `TaxCalculator` inclusive mode |
| Bill of supply | CGST Rules, rule 49 (same consolidation) | `DocumentKind.BILL_OF_SUPPLY` |
| Credit and debit note | CGST Rules, rule 53(1A) (same consolidation) | `DocumentKind.CREDIT_NOTE`, `DEBIT_NOTE` |
| Delivery challan | CGST Rules, rule 55(1) (same consolidation) | `DocumentKind.DELIVERY_CHALLAN` |
| HSN digits: 4 up to ₹5 crore, 6 above; omission for unregistered recipients | Notification 78/2020-Central Tax, 15 Oct 2020, effective 1 Apr 2021 ([GST Council copy](https://www.gstcouncil.gov.in/sites/default/files/2024-05/notfctn-78-central-tax-english-2020.pdf)) | `HsnRules` |
| Intra/inter-State, place of supply of goods | IGST Act, 2017, sections 7, 8, 10 (secondary reproductions) | `PlaceOfSupply` |
| SGST versus UTGST | UTGST Act, 2017, section 2(8); CGST Act, 2017, section 2(103) (secondary) | `StateCode.stateTaxKind` |
| Discount reduces value of supply | CGST Act, 2017, section 15(3)(a) (secondary), rule 46(k) | `TaxLineInput` |
| GSTIN check character | Luhn mod 36; not in the Act or Rules | `Gstin` |

## What is and is not verified

Verified against a primary source text read while building this version: rules 46, 46A, 49, 53, 55 (as of the 24 Sep 2021 consolidation) and notification 78/2020-CT. Rule 35 (the tax-inclusive formula) was read in the same consolidation. The GSTIN check algorithm reproduces two public sample GSTINs (`27AAPFU0939F1ZV`, `33AAACC1206D1ZN`) and rejects a mistyped one.

**Not verified against a primary source (treat as open):**

- The official state code table (only secondary lists were reachable; the list used agrees across them). Codes 96 (Other Country) and 99 (Centre Jurisdiction) are not modelled.
- IGST Act sections 7, 8, 10, 12, 13 and UTGST section 2(8): the CBIC pages could not be fetched (certificate error) so wording comes from secondary sites. Section 10(1)(ca) (unregistered recipients, from 1 Oct 2023) is taken from one of them.
- That Jammu and Kashmir levies SGST (taken from the "State includes a union territory with legislature" definition).
- Bill-to ship-to: `PlaceOfSupply.resolve` treats a delivery state as the place (section 10(1)(a)); delivery on the buyer's direction to a third person is section 10(1)(b) and needs `forDirectedDelivery`. The caller decides which applies.
- That supplies to code 97 (Other Territory) are inter-State.
- The special-category list in `StateCode.isSpecialCategory` (section 22 Explanation: Manipur, Mizoram, Nagaland, Tripura).
- Amendments to rules 46, 49, 53, 55 after 24 Sep 2021 (secondary sources mention changes by notifications 51/2023-CT and 20/2024-CT); later notifications on HSN digits for e-invoice, e-way bill or GSTR-1 reporting.
- The layout of the official HSN/SAC Excel master (the loader is tested on synthetic workbooks only).

**Conventions, not law:** rounding each tax head per line to paise (half-up) and summing. For MRP lines each head follows rule 35 (`price x head rate / (100 + sum of rates)`), so central and state tax are always equal and the taxable value is the remainder of the price. See the 50 worked invoices in `src/test/resources/.../golden/Invoices/invoices.txt`; cases marked AMBIGUOUS show where the law does not choose.

## Behaviour you should know

- `Gstin.parse` trims and upper-cases, rejects wrong length before examining content, and throws only `IllegalArgumentException`. A valid GSTIN is only well-formed: it may be cancelled or never issued. UIN, TDS/TCS and non-resident identifiers are not accepted.
- `TaxCalculator` takes the **total GST rate** (for example 18) and halves it for intra-State supplies. It does not know rate slabs or HSN-to-rate mappings: those change by notification and belong in your data.
- Line amounts (`unit price x quantity`) are computed by you with `india-money` and an explicit rounding mode. Credit notes: calculate on positive amounts and print them as a credit note.
- `InvoiceNumber` allows ASCII letters, digits, `-` and `/`, at most 16 characters. Uniqueness within a financial year is a contract you implement through `InvoiceNumberRegistry` (make the check and insert atomic with a unique index).
- `PlaceOfSupply` covers goods and registered/unregistered buyers inside India. Exports, imports, SEZ supplies, reverse charge and the services place-of-supply rules (IGST sections 12 and 13) are out of scope.

## Optional module: HSN master loader

`hsn-excel` reads an `.xlsx` HSN/SAC master with the JDK only (zip + StAX; DTDs and external entities disabled; decompression limited) and returns an `HsnMaster`:

```java
HsnLoadResult result = HsnExcelLoader.load(Path.of("hsn-master.xlsx"));
result.master().find("8471");            // Optional<HsnEntry>
result.skippedRows();                    // rows without a valid code
```

Legacy `.xls` is not supported. The loader finds the header row by looking for a column mentioning "hsn", "sac" or "tariff" and one mentioning "description".

## Development

```
./gradlew check javadoc    # format, checkstyle, tests, coverage gate (90% lines), javadoc, both modules
./gradlew testUnit testProperty testGolden testArch testSecurity
./gradlew pitest           # mutation testing, threshold 70%
```

Requires JDK 25. See `CLAUDE.md` for conventions and `CONTRIBUTING.md`.
