/*
 * File:      TaxRateSummary.java
 * Module:    india-gst
 * Layer:     api
 * Purpose:   Totals of an invoice for one GST rate and cess rate.
 * Owns:      Per-rate tax summary row.
 * Uses:      Money.
 * Used by:   TaxResult, HSN/rate summary on invoices.
 * Not here:  Calculation (TaxCalculator).
 * Reuse:     Value type.
 * Plan:      L2-04
 */
package io.github.someoneanoncoder.indiagst;

import io.github.someoneanoncoder.indiamoney.Money;
import java.math.BigDecimal;

/**
 * One row of the per-rate summary: all lines with the same GST rate and cess rate added together.
 *
 * @param gstRate total GST percentage of the group
 * @param cessRate cess percentage of the group
 * @param taxableValue sum of taxable values
 * @param centralTax sum of central tax
 * @param stateTax sum of state or union territory tax
 * @param integratedTax sum of integrated tax
 * @param cess sum of cess
 */
public record TaxRateSummary(
    BigDecimal gstRate,
    BigDecimal cessRate,
    Money taxableValue,
    Money centralTax,
    Money stateTax,
    Money integratedTax,
    Money cess) {

  /**
   * Returns the sum of all tax heads in this row.
   *
   * @return central + state + integrated + cess
   */
  public Money totalTax() {
    return centralTax.plus(stateTax).plus(integratedTax).plus(cess);
  }
}
