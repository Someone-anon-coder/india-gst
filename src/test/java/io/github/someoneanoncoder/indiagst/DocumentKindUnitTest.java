/*
 * File:      DocumentKindUnitTest.java
 * Module:    india-gst
 * Layer:     test
 * Purpose:   Unit tests for the document field checklists.
 * Owns:      Required fields per kind and condition, citations and missing-field check.
 * Uses:      DocumentKind, DocumentContext
 * Used by:   Gradle test tasks.
 * Not here:  Production code.
 * Reuse:     Test only.
 * Plan:      L2-05
 */
package io.github.someoneanoncoder.indiagst;

import static io.github.someoneanoncoder.indiagst.DocumentField.COUNTRY_OF_DESTINATION;
import static io.github.someoneanoncoder.indiagst.DocumentField.DELIVERY_ADDRESS;
import static io.github.someoneanoncoder.indiagst.DocumentField.HSN_CODE;
import static io.github.someoneanoncoder.indiagst.DocumentField.IRN_QR_CODE;
import static io.github.someoneanoncoder.indiagst.DocumentField.ORIGINAL_DOCUMENT_REFERENCE;
import static io.github.someoneanoncoder.indiagst.DocumentField.PLACE_OF_SUPPLY;
import static io.github.someoneanoncoder.indiagst.DocumentField.QUANTITY_AND_UNIT;
import static io.github.someoneanoncoder.indiagst.DocumentField.RECIPIENT_GSTIN_OR_UIN;
import static io.github.someoneanoncoder.indiagst.DocumentField.RECIPIENT_NAME;
import static io.github.someoneanoncoder.indiagst.DocumentField.RECIPIENT_STATE_NAME_AND_CODE;
import static io.github.someoneanoncoder.indiagst.DocumentField.SIGNATURE;
import static io.github.someoneanoncoder.indiagst.DocumentField.SUPPLIER_GSTIN;
import static io.github.someoneanoncoder.indiagst.DocumentField.TAX_AMOUNT;
import static io.github.someoneanoncoder.indiagst.DocumentField.TAX_RATE;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.someoneanoncoder.indiagst.testing.UnitTest;
import io.github.someoneanoncoder.indiamoney.Money;
import java.util.EnumSet;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@UnitTest
class DocumentKindUnitTest {

  private static final Money SMALL = Money.ofRupees(49_999);
  private static final Money LARGE = Money.ofRupees(50_000);

  @Test
  @DisplayName("every rule cites its source and no kind is empty")
  void shouldCiteSource_whenListingRules() {
    for (DocumentKind kind : DocumentKind.values()) {
      assertThat(kind.fieldRules()).isNotEmpty();
      kind.fieldRules()
          .forEach(rule -> assertThat(rule.citation()).startsWith("CGST Rules 2017, rule "));
    }
  }

  @Test
  @DisplayName(
      "a B2B tax invoice needs the recipient GSTIN, HSN, signature and reverse-charge flag")
  void shouldRequireRegisteredRecipientFields_whenInvoiceIsB2b() {
    Set<DocumentField> required =
        DocumentKind.TAX_INVOICE.requiredFields(
            DocumentContext.of(LARGE, DocumentFact.RECIPIENT_REGISTERED, DocumentFact.GOODS));
    assertThat(required)
        .contains(
            SUPPLIER_GSTIN,
            RECIPIENT_GSTIN_OR_UIN,
            HSN_CODE,
            QUANTITY_AND_UNIT,
            SIGNATURE,
            DocumentField.REVERSE_CHARGE_INDICATOR,
            TAX_RATE,
            TAX_AMOUNT)
        .doesNotContain(PLACE_OF_SUPPLY, IRN_QR_CODE, RECIPIENT_STATE_NAME_AND_CODE);
  }

  @Test
  @DisplayName("an unregistered recipient needs details at 50,000 or more, or on request")
  void shouldRequireUnregisteredDetails_whenValueOrRequestApplies() {
    assertThat(DocumentKind.TAX_INVOICE.requiredFields(DocumentContext.of(LARGE)))
        .contains(RECIPIENT_NAME, DELIVERY_ADDRESS, RECIPIENT_STATE_NAME_AND_CODE);
    assertThat(DocumentKind.TAX_INVOICE.requiredFields(DocumentContext.of(SMALL)))
        .doesNotContain(RECIPIENT_NAME, DELIVERY_ADDRESS, RECIPIENT_STATE_NAME_AND_CODE);
    assertThat(
            DocumentKind.TAX_INVOICE.requiredFields(
                DocumentContext.of(SMALL, DocumentFact.RECIPIENT_REQUESTED_DETAILS)))
        .contains(RECIPIENT_NAME, RECIPIENT_STATE_NAME_AND_CODE);
  }

  @Test
  @DisplayName("inter-State, export, e-invoice, electronic and omittable-HSN facts switch fields")
  void shouldSwitchFields_whenFactsApply() {
    Set<DocumentField> required =
        DocumentKind.TAX_INVOICE.requiredFields(
            DocumentContext.of(
                SMALL,
                DocumentFact.INTER_STATE,
                DocumentFact.EXPORT,
                DocumentFact.IRN_QR_REQUIRED,
                DocumentFact.ELECTRONIC_DOCUMENT,
                DocumentFact.HSN_MAY_BE_OMITTED,
                DocumentFact.DELIVERY_ADDRESS_DIFFERS));
    assertThat(required)
        .contains(PLACE_OF_SUPPLY, COUNTRY_OF_DESTINATION, IRN_QR_CODE, DELIVERY_ADDRESS)
        .doesNotContain(SIGNATURE, HSN_CODE);
  }

  @Test
  @DisplayName("bill of supply has no tax fields")
  void shouldHaveNoTaxFields_whenBillOfSupply() {
    assertThat(DocumentKind.BILL_OF_SUPPLY.requiredFields(DocumentContext.of(LARGE)))
        .doesNotContain(TAX_RATE, TAX_AMOUNT, PLACE_OF_SUPPLY);
  }

  @Test
  @DisplayName("credit and debit notes share a checklist that references the original invoice")
  void shouldReferenceOriginal_whenCreditOrDebitNote() {
    assertThat(DocumentKind.CREDIT_NOTE.fieldRules())
        .isEqualTo(DocumentKind.DEBIT_NOTE.fieldRules());
    assertThat(DocumentKind.CREDIT_NOTE.requiredFields(DocumentContext.of(SMALL)))
        .contains(ORIGINAL_DOCUMENT_REFERENCE, RECIPIENT_STATE_NAME_AND_CODE, SIGNATURE)
        .contains(DocumentField.NATURE_OF_DOCUMENT);
  }

  @Test
  @DisplayName("delivery challan needs tax figures only when goods move for supply")
  void shouldRequireTax_whenChallanIsForSupply() {
    Set<DocumentField> other =
        DocumentKind.DELIVERY_CHALLAN.requiredFields(DocumentContext.of(SMALL));
    Set<DocumentField> forSupply =
        DocumentKind.DELIVERY_CHALLAN.requiredFields(
            DocumentContext.of(
                SMALL, DocumentFact.TRANSPORT_FOR_SUPPLY, DocumentFact.SUPPLIER_REGISTERED));
    assertThat(other)
        .doesNotContain(TAX_RATE, SUPPLIER_GSTIN)
        .contains(QUANTITY_AND_UNIT, HSN_CODE);
    assertThat(forSupply).contains(TAX_RATE, TAX_AMOUNT, SUPPLIER_GSTIN);
  }

  @Test
  @DisplayName("missingFields reports required fields that are absent and ignores extras")
  void shouldReportMissing_whenFieldsAreAbsent() {
    DocumentContext context = DocumentContext.of(SMALL, DocumentFact.RECIPIENT_REGISTERED);
    Set<DocumentField> all = DocumentKind.BILL_OF_SUPPLY.requiredFields(context);
    assertThat(DocumentKind.BILL_OF_SUPPLY.missingFields(all, context)).isEmpty();
    Set<DocumentField> partial = EnumSet.copyOf(all);
    partial.remove(SIGNATURE);
    partial.add(IRN_QR_CODE);
    assertThat(DocumentKind.BILL_OF_SUPPLY.missingFields(partial, context))
        .containsExactly(SIGNATURE);
    assertThat(context.facts()).containsExactly(DocumentFact.RECIPIENT_REGISTERED);
  }
}
