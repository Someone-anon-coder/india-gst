/*
 * File:      TaxResult.java
 * Module:    india-gst
 * Layer:     api
 * Purpose:   The complete tax computation for an invoice.
 * Owns:      Lines, per-rate summary and invoice totals.
 * Uses:      TaxLine, TaxRateSummary, PlaceOfSupply.
 * Used by:   Billing and printing code.
 * Not here:  Calculation (TaxCalculator) and round-off (india-money RoundOff).
 * Reuse:     Value type.
 * Plan:      L2-04
 */
package io.github.someoneanoncoder.indiagst;

import io.github.someoneanoncoder.indiamoney.Money;
import java.util.List;
import java.util.function.Function;

/**
 * Tax computed for a whole invoice. Totals are plain sums of the line results; no further rounding
 * is applied, so the figures always add up. Apply {@code RoundOff} from india-money to {@link
 * #grandTotal()} for the rounded payable amount.
 *
 * @param placeOfSupply the supply classification used
 * @param pricingMode whether amounts were tax-inclusive
 * @param lines one result per input line, in input order
 * @param summary one row per distinct (GST rate, cess rate), ascending
 */
public record TaxResult(
    PlaceOfSupply placeOfSupply,
    PricingMode pricingMode,
    List<TaxLine> lines,
    List<TaxRateSummary> summary) {

  /**
   * Copies the lists so the result is immutable.
   *
   * @param placeOfSupply the supply classification
   * @param pricingMode the pricing mode
   * @param lines the line results
   * @param summary the per-rate rows
   */
  public TaxResult {
    lines = List.copyOf(lines);
    summary = List.copyOf(summary);
  }

  /**
   * Returns the total taxable value.
   *
   * @return sum over lines
   */
  public Money taxableTotal() {
    return sum(TaxLine::taxableValue);
  }

  /**
   * Returns the total central tax.
   *
   * @return sum over lines
   */
  public Money centralTaxTotal() {
    return sum(TaxLine::centralTax);
  }

  /**
   * Returns the total state or union territory tax.
   *
   * @return sum over lines
   */
  public Money stateTaxTotal() {
    return sum(TaxLine::stateTax);
  }

  /**
   * Returns the total integrated tax.
   *
   * @return sum over lines
   */
  public Money integratedTaxTotal() {
    return sum(TaxLine::integratedTax);
  }

  /**
   * Returns the total cess.
   *
   * @return sum over lines
   */
  public Money cessTotal() {
    return sum(TaxLine::cess);
  }

  /**
   * Returns the total of all tax heads.
   *
   * @return sum over lines
   */
  public Money taxTotal() {
    return sum(TaxLine::totalTax);
  }

  /**
   * Returns the amount payable before any round-off.
   *
   * @return taxable total plus tax total
   */
  public Money grandTotal() {
    return taxableTotal().plus(taxTotal());
  }

  private Money sum(Function<TaxLine, Money> part) {
    Money total = Money.ZERO;
    for (TaxLine line : lines) {
      total = total.plus(part.apply(line));
    }
    return total;
  }
}
