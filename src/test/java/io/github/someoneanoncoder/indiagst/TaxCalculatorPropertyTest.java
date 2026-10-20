/*
 * File:      TaxCalculatorPropertyTest.java
 * Module:    india-gst
 * Layer:     test
 * Purpose:   Property tests for TaxCalculator invariants.
 * Owns:      Inclusive lines total the price, heads add up, summary matches lines.
 * Uses:      TaxCalculator
 * Used by:   Gradle test tasks.
 * Not here:  Production code.
 * Reuse:     Test only.
 * Plan:      L2-04
 */
package io.github.someoneanoncoder.indiagst;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.someoneanoncoder.indiagst.testing.PropertyTest;
import io.github.someoneanoncoder.indiamoney.Money;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;

@PropertyTest
class TaxCalculatorPropertyTest {

  private static final PlaceOfSupply INTRA =
      PlaceOfSupply.forDelivery(StateCode.MAHARASHTRA, StateCode.MAHARASHTRA);
  private static final PlaceOfSupply INTER =
      PlaceOfSupply.forDelivery(StateCode.MAHARASHTRA, StateCode.KARNATAKA);

  @Provide
  Arbitrary<TaxLineInput> lines() {
    Arbitrary<Long> amount = Arbitraries.longs().between(0, 100_000_000L);
    Arbitrary<BigDecimal> gst =
        Arbitraries.of("0", "0.25", "3", "5", "12", "18", "28", "40").map(BigDecimal::new);
    Arbitrary<BigDecimal> cess = Arbitraries.of("0", "0", "1", "5", "12").map(BigDecimal::new);
    Arbitrary<Integer> discountPercent = Arbitraries.integers().between(0, 100);
    return Combinators.combine(amount, gst, cess, discountPercent)
        .as(
            (a, g, c, pct) ->
                new TaxLineInput(Money.ofPaise(a), Money.ofPaise(a * pct / 100), g, c));
  }

  @Provide
  Arbitrary<List<TaxLineInput>> lineLists() {
    return lines().list().ofMinSize(0).ofMaxSize(15);
  }

  @Property
  void shouldTotalNetAmount_whenPricingIsInclusive(
      @ForAll("lineLists") List<TaxLineInput> inputs, @ForAll boolean inter) {
    TaxResult result =
        TaxCalculator.calculate(inter ? INTER : INTRA, PricingMode.INCLUSIVE, inputs);
    for (int i = 0; i < inputs.size(); i++) {
      TaxLine line = result.lines().get(i);
      assertThat(line.lineTotal()).isEqualTo(inputs.get(i).netAmount());
      assertThat(line.taxableValue().paise()).isBetween(0L, inputs.get(i).netAmount().paise());
      assertThat(line.cess().isNegative()).isFalse();
      assertThat(line.totalTax().isNegative()).isFalse();
    }
  }

  @Property
  void shouldKeepHeadsApart_whenIntraOrInter(
      @ForAll("lineLists") List<TaxLineInput> inputs, @ForAll boolean inclusive) {
    PricingMode mode = inclusive ? PricingMode.INCLUSIVE : PricingMode.EXCLUSIVE;
    TaxResult intra = TaxCalculator.calculate(INTRA, mode, inputs);
    TaxResult inter = TaxCalculator.calculate(INTER, mode, inputs);
    assertThat(intra.integratedTaxTotal()).isEqualTo(Money.ZERO);
    assertThat(inter.centralTaxTotal().plus(inter.stateTaxTotal())).isEqualTo(Money.ZERO);
    assertThat(intra.cessTotal()).isEqualTo(inter.cessTotal());
    if (inclusive) {
      assertThat(intra.grandTotal()).isEqualTo(inter.grandTotal());
      assertThat(Math.abs(intra.taxableTotal().paise() - inter.taxableTotal().paise()))
          .isLessThanOrEqualTo(inputs.size());
    } else {
      assertThat(intra.taxableTotal()).isEqualTo(inter.taxableTotal());
    }
  }

  @Property
  void shouldKeepHalvesEqual_whenExclusiveIntra(@ForAll("lineLists") List<TaxLineInput> inputs) {
    TaxResult result = TaxCalculator.calculate(INTRA, PricingMode.EXCLUSIVE, inputs);
    for (TaxLine line : result.lines()) {
      assertThat(line.centralTax()).isEqualTo(line.stateTax());
    }
  }

  @Property
  void shouldKeepHalvesEqual_whenInclusiveIntra(@ForAll("lineLists") List<TaxLineInput> inputs) {
    TaxResult result = TaxCalculator.calculate(INTRA, PricingMode.INCLUSIVE, inputs);
    for (TaxLine line : result.lines()) {
      assertThat(line.centralTax()).isEqualTo(line.stateTax());
    }
  }

  @Property
  void shouldFollowRule35_whenInclusive(@ForAll("lines") TaxLineInput input) {
    TaxResult result = TaxCalculator.calculate(INTER, PricingMode.INCLUSIVE, List.of(input));
    BigDecimal divisor = BigDecimal.valueOf(100).add(input.gstRate()).add(input.cessRate());
    long expected =
        BigDecimal.valueOf(input.netAmount().paise())
            .multiply(input.gstRate())
            .divide(divisor, 0, RoundingMode.HALF_UP)
            .longValueExact();
    assertThat(result.integratedTaxTotal().paise()).isEqualTo(expected);
  }

  @Property
  void shouldAddUp_whenSummarisingLines(
      @ForAll("lineLists") List<TaxLineInput> inputs, @ForAll boolean inclusive) {
    PricingMode mode = inclusive ? PricingMode.INCLUSIVE : PricingMode.EXCLUSIVE;
    TaxResult result = TaxCalculator.calculate(INTRA, mode, inputs);
    List<Money> taxable = new ArrayList<>();
    List<Money> tax = new ArrayList<>();
    result.summary().forEach(r -> taxable.add(r.taxableValue()));
    result.summary().forEach(r -> tax.add(r.totalTax()));
    assertThat(taxable.stream().reduce(Money.ZERO, Money::plus)).isEqualTo(result.taxableTotal());
    assertThat(tax.stream().reduce(Money.ZERO, Money::plus)).isEqualTo(result.taxTotal());
    assertThat(result.grandTotal()).isEqualTo(result.taxableTotal().plus(result.taxTotal()));
  }

  @Property
  void shouldChargeRateOfNetAmount_whenExclusiveInterState(@ForAll("lines") TaxLineInput input) {
    TaxResult result = TaxCalculator.calculate(INTER, PricingMode.EXCLUSIVE, List.of(input));
    assertThat(result.integratedTaxTotal())
        .isEqualTo(input.netAmount().percent(input.gstRate(), RoundingMode.HALF_UP));
    assertThat(result.taxableTotal()).isEqualTo(input.netAmount());
  }
}
