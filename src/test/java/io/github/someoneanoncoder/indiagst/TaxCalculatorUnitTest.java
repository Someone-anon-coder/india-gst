/*
 * File:      TaxCalculatorUnitTest.java
 * Module:    india-gst
 * Layer:     test
 * Purpose:   Unit tests for TaxCalculator input validation, heads and summaries.
 * Owns:      Validation errors, totals and summary ordering.
 * Uses:      TaxCalculator, TaxLineInput, TaxResult
 * Used by:   Gradle test tasks.
 * Not here:  Production code.
 * Reuse:     Test only.
 * Plan:      L2-04
 */
package io.github.someoneanoncoder.indiagst;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.someoneanoncoder.indiagst.testing.UnitTest;
import io.github.someoneanoncoder.indiamoney.Money;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@UnitTest
class TaxCalculatorUnitTest {

  private static final PlaceOfSupply INTRA =
      PlaceOfSupply.forDelivery(StateCode.MAHARASHTRA, StateCode.MAHARASHTRA);
  private static final PlaceOfSupply INTER =
      PlaceOfSupply.forDelivery(StateCode.MAHARASHTRA, StateCode.KARNATAKA);

  private static TaxLineInput line(long rupees, String rate) {
    return TaxLineInput.of(Money.ofRupees(rupees), new BigDecimal(rate));
  }

  @Test
  @DisplayName("an intra-State exclusive line splits tax into equal central and state halves")
  void shouldSplitHalves_whenIntraStateExclusive() {
    TaxResult result =
        TaxCalculator.calculate(INTRA, PricingMode.EXCLUSIVE, List.of(line(1000, "18")));
    assertThat(result.centralTaxTotal()).isEqualTo(Money.ofRupees(90));
    assertThat(result.stateTaxTotal()).isEqualTo(Money.ofRupees(90));
    assertThat(result.integratedTaxTotal()).isEqualTo(Money.ZERO);
    assertThat(result.grandTotal()).isEqualTo(Money.ofRupees(1180));
    assertThat(result.placeOfSupply()).isEqualTo(INTRA);
    assertThat(result.pricingMode()).isEqualTo(PricingMode.EXCLUSIVE);
  }

  @Test
  @DisplayName("an inter-State line carries integrated tax only")
  void shouldChargeIntegratedTax_whenInterState() {
    TaxResult result =
        TaxCalculator.calculate(INTER, PricingMode.EXCLUSIVE, List.of(line(1000, "18")));
    assertThat(result.integratedTaxTotal()).isEqualTo(Money.ofRupees(180));
    assertThat(result.centralTaxTotal()).isEqualTo(Money.ZERO);
    assertThat(result.taxTotal()).isEqualTo(Money.ofRupees(180));
    assertThat(result.lines().get(0).lineTotal()).isEqualTo(Money.ofRupees(1180));
  }

  @Test
  @DisplayName("summary rows are ordered by GST rate then cess rate and rates compare numerically")
  void shouldOrderSummary_whenRatesDiffer() {
    TaxLineInput a =
        new TaxLineInput(Money.ofRupees(100), Money.ZERO, new BigDecimal("18.0"), BigDecimal.ONE);
    TaxLineInput b =
        new TaxLineInput(Money.ofRupees(100), Money.ZERO, new BigDecimal("18"), BigDecimal.ZERO);
    TaxLineInput c = line(100, "5");
    TaxResult result = TaxCalculator.calculate(INTRA, PricingMode.EXCLUSIVE, List.of(a, b, c, b));
    assertThat(result.summary()).hasSize(3);
    assertThat(result.summary().get(0).gstRate()).isEqualByComparingTo("5");
    assertThat(result.summary().get(1).cessRate()).isEqualByComparingTo("0");
    assertThat(result.summary().get(1).taxableValue()).isEqualTo(Money.ofRupees(200));
    assertThat(result.summary().get(2).cessRate()).isEqualByComparingTo("1");
    assertThat(result.summary().get(2).totalTax()).isEqualTo(Money.ofRupees(19));
  }

  @Test
  @DisplayName("an empty invoice has zero totals")
  void shouldReturnZeroTotals_whenNoLines() {
    TaxResult result = TaxCalculator.calculate(INTRA, PricingMode.INCLUSIVE, List.of());
    assertThat(result.grandTotal()).isEqualTo(Money.ZERO);
    assertThat(result.summary()).isEmpty();
  }

  @Test
  @DisplayName("line input is validated")
  void shouldRejectInvalidInput_whenBuildingLines() {
    Money hundred = Money.ofRupees(100);
    assertThatThrownBy(() -> TaxLineInput.of(Money.ofRupees(-1), BigDecimal.TEN))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(
            () -> new TaxLineInput(hundred, Money.ofRupees(-1), BigDecimal.TEN, BigDecimal.ZERO))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(
            () -> new TaxLineInput(hundred, Money.ofRupees(101), BigDecimal.TEN, BigDecimal.ZERO))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> TaxLineInput.of(hundred, new BigDecimal("-1")))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> TaxLineInput.of(hundred, new BigDecimal("100.01")))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> TaxLineInput.of(hundred, new BigDecimal("18.00001")))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(
            () -> new TaxLineInput(hundred, Money.ZERO, BigDecimal.TEN, new BigDecimal("1000.5")))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(
            () -> new TaxLineInput(hundred, Money.ZERO, BigDecimal.TEN, new BigDecimal("-1")))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> TaxLineInput.of(null, BigDecimal.TEN))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  @DisplayName("rates are normalised so 18, 18.0 and 1.8E+1 are equal")
  void shouldNormaliseRates_whenScalesDiffer() {
    assertThat(line(1, "18.00").gstRate()).isEqualTo(line(1, "18").gstRate());
    assertThat(line(1, "1.8E+1").gstRate()).isEqualTo(new BigDecimal("18"));
    assertThat(line(1, "0.00").gstRate()).isEqualTo(BigDecimal.ZERO);
    assertThat(line(1, "100").gstRate()).isEqualTo(new BigDecimal("100"));
  }

  @Test
  @DisplayName("net amount subtracts the discount")
  void shouldSubtractDiscount_whenComputingNetAmount() {
    TaxLineInput input =
        new TaxLineInput(Money.ofRupees(100), Money.ofRupees(30), BigDecimal.TEN, BigDecimal.ZERO);
    assertThat(input.netAmount()).isEqualTo(Money.ofRupees(70));
  }

  @Test
  @DisplayName("an absurd number of lines is refused")
  void shouldRejectTooManyLines_whenListIsHuge() {
    List<TaxLineInput> many = java.util.Collections.nCopies(100_001, line(1, "5"));
    assertThatThrownBy(() -> TaxCalculator.calculate(INTRA, PricingMode.EXCLUSIVE, many))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("overflowing amounts throw ArithmeticException instead of wrapping")
  void shouldThrow_whenTotalOverflows() {
    TaxLineInput big = TaxLineInput.of(Money.ofPaise(Long.MAX_VALUE / 2 + 1), BigDecimal.ZERO);
    assertThatThrownBy(
            () ->
                TaxCalculator.calculate(INTRA, PricingMode.EXCLUSIVE, List.of(big, big))
                    .grandTotal())
        .isInstanceOf(ArithmeticException.class);
  }

  @Test
  @DisplayName("result lists are immutable copies")
  void shouldCopyLists_whenBuildingResult() {
    TaxResult result = TaxCalculator.calculate(INTRA, PricingMode.EXCLUSIVE, List.of(line(1, "5")));
    assertThatThrownBy(() -> result.lines().add(result.lines().get(0)))
        .isInstanceOf(UnsupportedOperationException.class);
  }
}
