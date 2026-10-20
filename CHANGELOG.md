# Changelog

All notable changes are listed here. Format: [Keep a Changelog](https://keepachangelog.com/); versioning: [SemVer](https://semver.org/) (breaking changes only in minor bumps before 1.0.0).

## [Unreleased]

### Added
- `Gstin` with Luhn mod-36 check character, PAN and state extraction, generation [L2-01].
- `StateCode` (all States and union territories, 97 Other Territory, legacy 25 and 28), `StateTaxKind` [L2-02].
- `PlaceOfSupply` and `SupplyType` [L2-03].
- `TaxCalculator`, `TaxLineInput`, `TaxLine`, `TaxRateSummary`, `TaxResult`, `PricingMode`: exclusive and inclusive pricing, discount, cess, per-rate summary [L2-04].
- `DocumentKind`, `DocumentField`, `FieldRule`, `FieldCondition`, `DocumentFact`, `DocumentContext`: Rule 46, 49, 53, 55 checklists as data [L2-05].
- `HsnRules`, `HsnEntry`, `HsnMaster`; optional `hsn-excel` module with a JDK-only `.xlsx` loader [L2-06].
- `InvoiceNumber`, `InvoiceNumberRegistry` [L2-07].
- 50 worked invoices as golden tests (draft, awaiting owner review) [L2-08].
