/*
 * File:      TaxLineInput.java
 * Module:    india-gst
 * Layer:     api
 * Purpose:   One invoice line as given to the tax calculator.
 * Owns:      Validation of amount, discount and rates.
 * Uses:      Money.
 * Used by:   TaxCalculator and billing code.
 * Not here:  Computing unit price times quantity (india-money) or tax.
 * Reuse:     Value type.
 * Plan:      L2-04
 */
package io.github.someoneanoncoder.indiagst;

import io.github.someoneanoncoder.indiamoney.Money;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * A line to be taxed: its value, any discount and the GST and cess rates.
 *
 * <p>Discount: a discount recorded on the invoice before or at the time of supply reduces the value
 * of supply (CGST Act, 2017, section 15(3)(a); wording from a secondary reproduction, not
 * verified), and Rule 46(k) asks for "taxable value ... taking into account discount or abatement".
 * The calculator therefore taxes {@code amount - discount}.
 *
 * @param amount line value before discount: unit price times quantity, computed by the caller with
 *     explicit rounding; tax-exclusive or tax-inclusive depending on the {@link PricingMode}
 * @param discount discount on this line, between zero and {@code amount}
 * @param gstRate total GST percentage (CGST plus SGST, or IGST), for example {@code 18} or {@code
 *     5}; between 0 and 100
 * @param cessRate cess percentage on taxable value, between 0 and 1000; zero if none
 */
public record TaxLineInput(Money amount, Money discount, BigDecimal gstRate, BigDecimal cessRate) {

  private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
  private static final BigDecimal THOUSAND = BigDecimal.valueOf(1000);
  private static final int MAX_RATE_SCALE = 4;

  /**
   * Validates and normalises the line.
   *
   * @param amount line value before discount
   * @param discount discount on the line
   * @param gstRate total GST percentage
   * @param cessRate cess percentage
   * @throws IllegalArgumentException if an amount is negative, the discount exceeds the amount, or
   *     a rate is out of range or has more than four decimal places
   */
  public TaxLineInput {
    Objects.requireNonNull(amount, "amount");
    Objects.requireNonNull(discount, "discount");
    gstRate = checkRate(gstRate, HUNDRED, "gstRate");
    cessRate = checkRate(cessRate, THOUSAND, "cessRate");
    if (amount.isNegative() || discount.isNegative()) {
      throw new IllegalArgumentException("amount and discount must not be negative");
    }
    if (discount.compareTo(amount) > 0) {
      throw new IllegalArgumentException("discount must not exceed the amount");
    }
  }

  /**
   * Creates a line with no discount and no cess.
   *
   * @param amount line value
   * @param gstRate total GST percentage
   * @return the line
   */
  public static TaxLineInput of(Money amount, BigDecimal gstRate) {
    return new TaxLineInput(amount, Money.ZERO, gstRate, BigDecimal.ZERO);
  }

  /**
   * Returns the amount after discount.
   *
   * @return {@code amount - discount}
   */
  public Money netAmount() {
    return amount.minus(discount);
  }

  private static BigDecimal checkRate(BigDecimal rate, BigDecimal max, String name) {
    Objects.requireNonNull(rate, name);
    BigDecimal clean = rate.signum() == 0 ? BigDecimal.ZERO : rate.stripTrailingZeros();
    if (clean.signum() < 0 || clean.compareTo(max) > 0 || clean.scale() > MAX_RATE_SCALE) {
      throw new IllegalArgumentException(name + " out of range or too precise: " + rate);
    }
    return clean.scale() < 0 ? clean.setScale(0) : clean;
  }
}
