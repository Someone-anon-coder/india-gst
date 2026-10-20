/*
 * File:      TaxCalculator.java
 * Module:    india-gst
 * Layer:     api
 * Purpose:   Computes GST per line and per rate for tax-exclusive and tax-inclusive (MRP) pricing.
 * Owns:      Taxable value, rate split into central/state or integrated tax, cess, line rounding.
 * Uses:      Money (india-money), PlaceOfSupply, TaxLineInput, TaxLine, TaxRateSummary, TaxResult.
 * Used by:   Billing and quotation code in any product.
 * Not here:  Choosing rates (HSN to rate tables change by notification), unit price times quantity, round-off of the payable total.
 * Reuse:     Static, stateless.
 * Plan:      L2-04
 */
package io.github.someoneanoncoder.indiagst;

import io.github.someoneanoncoder.indiamoney.Money;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * GST calculation for an invoice.
 *
 * <p>Rules and conventions (CGST Act, 2017 section 9 levies central tax and the SGST/UTGST Acts the
 * state tax at equal rates on intra-State supplies; the IGST Act, 2017 section 5 levies integrated
 * tax on inter-State supplies; these summaries come from secondary sources):
 *
 * <ul>
 *   <li><b>Tax-exclusive:</b> taxable value is {@code amount - discount}. Intra-State: central tax
 *       and state tax are each {@code taxable x (rate / 2)} rounded half-up to paise, so the two
 *       halves are always equal. Inter-State: integrated tax is {@code taxable x rate}. Cess is
 *       {@code taxable x cessRate}.
 *   <li><b>Tax-inclusive (MRP):</b> the discounted amount is the final price. Taxable value is
 *       {@code price x 100 / (100 + rate + cessRate)} rounded half-up; cess is its percentage
 *       (rounded half-up, never above the tax part); the remainder is GST, so the line always
 *       totals exactly the discounted price. On an intra-State line an odd paisa of GST goes to
 *       central tax.
 *   <li><b>Rounding:</b> each line is rounded to whole paise on its own and the invoice totals are
 *       the sums of those lines. No CGST Rule prescribes line-level or invoice-level rounding; this
 *       is the common billing-software practice and is flagged as a convention, not law.
 * </ul>
 */
public final class TaxCalculator {

  private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
  private static final BigDecimal TWO = BigDecimal.valueOf(2);
  private static final int MAX_LINES = 100_000;

  private TaxCalculator() {}

  /**
   * Taxes every line of an invoice.
   *
   * @param place the resolved place of supply
   * @param mode whether the line amounts include tax
   * @param inputs the lines; at most 100,000
   * @return per-line results, a per-rate summary and totals
   * @throws IllegalArgumentException if there are too many lines
   * @throws ArithmeticException if an amount overflows
   */
  public static TaxResult calculate(
      PlaceOfSupply place, PricingMode mode, List<TaxLineInput> inputs) {
    Objects.requireNonNull(place, "place");
    Objects.requireNonNull(mode, "mode");
    if (inputs.size() > MAX_LINES) {
      throw new IllegalArgumentException("too many lines: " + inputs.size());
    }
    List<TaxLine> lines = new ArrayList<>(inputs.size());
    for (TaxLineInput input : inputs) {
      lines.add(mode == PricingMode.EXCLUSIVE ? exclusive(place, input) : inclusive(place, input));
    }
    return new TaxResult(place, mode, lines, summarise(lines));
  }

  private static TaxLine exclusive(PlaceOfSupply place, TaxLineInput input) {
    Money taxable = input.netAmount();
    Money cess = taxable.percent(input.cessRate(), RoundingMode.HALF_UP);
    if (place.supplyType() == SupplyType.INTER_STATE) {
      Money igst = taxable.percent(input.gstRate(), RoundingMode.HALF_UP);
      return new TaxLine(input, taxable, Money.ZERO, Money.ZERO, igst, cess);
    }
    BigDecimal half = input.gstRate().divide(TWO);
    Money each = taxable.percent(half, RoundingMode.HALF_UP);
    return new TaxLine(input, taxable, each, each, Money.ZERO, cess);
  }

  private static TaxLine inclusive(PlaceOfSupply place, TaxLineInput input) {
    Money price = input.netAmount();
    BigDecimal divisor = HUNDRED.add(input.gstRate()).add(input.cessRate());
    long taxablePaise =
        BigDecimal.valueOf(price.paise())
            .multiply(HUNDRED)
            .divide(divisor, 0, RoundingMode.HALF_UP)
            .longValueExact();
    Money taxable = Money.ofPaise(taxablePaise);
    Money taxPart = price.minus(taxable);
    Money cess = taxable.percent(input.cessRate(), RoundingMode.HALF_UP);
    if (cess.compareTo(taxPart) > 0) {
      cess = taxPart;
    }
    Money gst = taxPart.minus(cess);
    if (place.supplyType() == SupplyType.INTER_STATE) {
      return new TaxLine(input, taxable, Money.ZERO, Money.ZERO, gst, cess);
    }
    List<Money> halves = gst.allocate(1, 1);
    return new TaxLine(input, taxable, halves.get(0), halves.get(1), Money.ZERO, cess);
  }

  private static List<TaxRateSummary> summarise(List<TaxLine> lines) {
    Map<List<BigDecimal>, TaxRateSummary> rows = new TreeMap<>(TaxCalculator::compareRates);
    for (TaxLine line : lines) {
      List<BigDecimal> key = List.of(line.input().gstRate(), line.input().cessRate());
      TaxRateSummary old = rows.get(key);
      rows.put(key, old == null ? firstRow(line) : addLine(old, line));
    }
    return new ArrayList<>(rows.values());
  }

  private static TaxRateSummary firstRow(TaxLine line) {
    return new TaxRateSummary(
        line.input().gstRate(),
        line.input().cessRate(),
        line.taxableValue(),
        line.centralTax(),
        line.stateTax(),
        line.integratedTax(),
        line.cess());
  }

  private static TaxRateSummary addLine(TaxRateSummary row, TaxLine line) {
    return new TaxRateSummary(
        row.gstRate(),
        row.cessRate(),
        row.taxableValue().plus(line.taxableValue()),
        row.centralTax().plus(line.centralTax()),
        row.stateTax().plus(line.stateTax()),
        row.integratedTax().plus(line.integratedTax()),
        row.cess().plus(line.cess()));
  }

  private static int compareRates(List<BigDecimal> a, List<BigDecimal> b) {
    int byGst = a.get(0).compareTo(b.get(0));
    return byGst != 0 ? byGst : a.get(1).compareTo(b.get(1));
  }
}
