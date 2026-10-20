/*
 * File:      TaxLine.java
 * Module:    india-gst
 * Layer:     api
 * Purpose:   The tax computed for one invoice line.
 * Owns:      Taxable value and each tax head for a line.
 * Uses:      Money, TaxLineInput.
 * Used by:   TaxResult, invoice rendering code.
 * Not here:  Calculation (TaxCalculator).
 * Reuse:     Value type.
 * Plan:      L2-04
 */
package io.github.someoneanoncoder.indiagst;

import io.github.someoneanoncoder.indiamoney.Money;

/**
 * The result of taxing one line. Intra-State lines carry central and state tax; inter-State lines
 * carry integrated tax; the unused heads are zero.
 *
 * @param input the line as supplied
 * @param taxableValue value on which tax is charged (Rule 46(k))
 * @param centralTax central tax (CGST)
 * @param stateTax state tax or union territory tax (SGST or UTGST)
 * @param integratedTax integrated tax (IGST)
 * @param cess compensation cess
 */
public record TaxLine(
    TaxLineInput input,
    Money taxableValue,
    Money centralTax,
    Money stateTax,
    Money integratedTax,
    Money cess) {

  /**
   * Returns the sum of all tax heads.
   *
   * @return central + state + integrated + cess
   */
  public Money totalTax() {
    return centralTax.plus(stateTax).plus(integratedTax).plus(cess);
  }

  /**
   * Returns the line total payable.
   *
   * @return taxable value plus total tax
   */
  public Money lineTotal() {
    return taxableValue.plus(totalTax());
  }
}
