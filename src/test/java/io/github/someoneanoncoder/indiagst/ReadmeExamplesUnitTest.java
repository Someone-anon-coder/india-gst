/*
 * File:      ReadmeExamplesUnitTest.java
 * Module:    india-gst
 * Layer:     test
 * Purpose:   Keeps the README examples compiling and correct.
 * Owns:      The four README usage examples.
 * Uses:      Public API
 * Used by:   Gradle test tasks.
 * Not here:  Production code.
 * Reuse:     Test only.
 * Plan:      L2
 */
package io.github.someoneanoncoder.indiagst;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.someoneanoncoder.indiagst.testing.UnitTest;
import io.github.someoneanoncoder.indiamoney.Money;
import io.github.someoneanoncoder.indiamoney.RoundOff;
import io.github.someoneanoncoder.indiamoney.RoundOffMode;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@UnitTest
class ReadmeExamplesUnitTest {

  @Test
  @DisplayName("example 1: validate a GSTIN")
  void shouldValidateGstin_whenFollowingReadmeExample1() {
    Gstin gstin = Gstin.parse("27AAPFU0939F1ZV");
    assertThat(gstin.state().displayName()).isEqualTo("Maharashtra");
    assertThat(gstin.pan()).isEqualTo("AAPFU0939F");
    assertThat(Gstin.isValid("27AAPFU0939F1ZO")).isFalse();
    assertThat(Gstin.generate("27AAPFU0939F1Z").checkCharacter()).isEqualTo('V');
  }

  @Test
  @DisplayName("example 2: tax-exclusive invoice, intra-State, two rates")
  void shouldCalculateExclusive_whenFollowingReadmeExample2() {
    PlaceOfSupply place = PlaceOfSupply.forDelivery(StateCode.MAHARASHTRA, StateCode.MAHARASHTRA);
    TaxResult result =
        TaxCalculator.calculate(
            place,
            PricingMode.EXCLUSIVE,
            List.of(
                TaxLineInput.of(Money.ofRupees(1000), new BigDecimal("18")),
                new TaxLineInput(
                    Money.ofRupees(500),
                    Money.ofRupees(50),
                    new BigDecimal("5"),
                    BigDecimal.ZERO)));
    assertThat(result.centralTaxTotal()).isEqualTo(Money.ofPaise(9_000 + 1_125));
    assertThat(result.stateTaxTotal()).isEqualTo(Money.ofPaise(9_000 + 1_125));
    assertThat(result.grandTotal()).isEqualTo(Money.ofPaise(145_000 + 20_250));
    RoundOff payable = RoundOff.of(result.grandTotal(), RoundOffMode.NEAREST_RUPEE);
    assertThat(payable.rounded()).isEqualTo(Money.ofRupees(1653));
    assertThat(payable.adjustment()).isEqualTo(Money.ofPaise(50));
  }

  @Test
  @DisplayName("example 3: MRP (tax-inclusive) line sold to another State")
  void shouldCalculateInclusive_whenFollowingReadmeExample3() {
    PlaceOfSupply place =
        PlaceOfSupply.resolve(
            StateCode.MAHARASHTRA, Optional.empty(), Optional.of(Gstin.parse("33AAACC1206D1ZN")));
    TaxResult result =
        TaxCalculator.calculate(
            place,
            PricingMode.INCLUSIVE,
            List.of(TaxLineInput.of(Money.ofRupees(118), new BigDecimal("18"))));
    assertThat(place.supplyType()).isEqualTo(SupplyType.INTER_STATE);
    assertThat(result.taxableTotal()).isEqualTo(Money.ofRupees(100));
    assertThat(result.integratedTaxTotal()).isEqualTo(Money.ofRupees(18));
  }

  @Test
  @DisplayName("example 4: field checklist, HSN digits and invoice number")
  void shouldCheckDocumentAndNumbers_whenFollowingReadmeExample4() {
    DocumentContext context =
        DocumentContext.of(Money.ofRupees(60_000), DocumentFact.GOODS, DocumentFact.INTER_STATE);
    assertThat(DocumentKind.TAX_INVOICE.requiredFields(context))
        .contains(DocumentField.PLACE_OF_SUPPLY, DocumentField.RECIPIENT_STATE_NAME_AND_CODE);
    assertThat(HsnRules.requiredDigits(Money.ofRupees(7_00_00_000L))).isEqualTo(6);
    assertThat(InvoiceNumber.of("A/25-26/", 123, 6).value()).isEqualTo("A/25-26/000123");
    assertThat(InvoiceNumber.isValid("A/25-26/0001234567")).isFalse();
  }
}
