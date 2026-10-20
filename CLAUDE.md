# CLAUDE.md — india-gst

Public MIT library (derived from the ShopSuite monorepo CLAUDE.md §12 template). Scope: `Gstin`, `StateCode`, `PlaceOfSupply`, `TaxCalculator`, `DocumentKind` field rules, `HsnRules`, `InvoiceNumber`, plus the optional `hsn-excel` loader module. Anything else (numbering storage, invoice persistence, GSTR return logic, rate tables) is out of scope and stays in the private product.

## Rules

1. Runtime dependency: `india-money` only (`com.github.Someone-anon-coder:india-money:v0.1.1` via JitPack, `requires transitive io.github.someoneanoncoder.indiamoney`). Decision recorded 2026-10-20 in the ShopSuite decisions log (§10.4): `TaxCalculator` needs `Money`, and PLAN makes L2 depend on L1. No other runtime dependency; `hsn-excel` uses the JDK only. Test/build tools must be permissively licensed.
2. Money and quantity use india-money types; no `double`/`float` anywhere in main code (ArchUnit enforced).
3. Every rule that comes from law cites its source (Act section, rule, notification number and date) in Javadoc and the README. Say when only a secondary source was read. Never state a rule from memory as fact; list unverified items in the README.
4. README keeps the line "Not tax advice; rules as of <date>; verify against CBIC notifications" and the date is updated whenever a rule is re-checked.
5. Every public type/method has full Javadoc; every source file has the header (checkstyle enforced); every folder has `FOLDER.md`.
6. Files at most 400 lines, methods at most 40.
7. Value types use natural names. The subject-stem method rule of the monorepo (§5.3) applies to layered code, not to this public API.
8. Tests: `<Class><Category>Test`; categories unit, property, golden, arch, security. Line coverage at least 90%, PIT at least 70%. GSTIN, invoice-number and spreadsheet input are untrusted: security tests required.
9. The 50 golden invoices (`golden/Invoices/invoices.txt`) are worked by hand and never generated from the implementation; the owner reviews them before the DoD is marked done.
10. Never delete branches. Ask the owner before pushing, opening PRs, tagging or publishing releases.
11. Releases only from tags `v*` via CI. Breaking changes only in a minor bump before 1.0.0, listed in CHANGELOG.
12. Version comes from `-PreleaseVersion` or env `VERSION`; never hard-code it. jitpack.yml installs Temurin 25 via SDKMAN (`25.0.4.1+1-tem`).

## Tracking

Plan IDs L2-01..L2-08 live in the ShopSuite repo `docs/PLAN.md`. Status of this library is tracked in that repo's CLAUDE.md §10.
