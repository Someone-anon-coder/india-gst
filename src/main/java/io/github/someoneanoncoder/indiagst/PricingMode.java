/*
 * File:      PricingMode.java
 * Module:    india-gst
 * Layer:     api
 * Purpose:   Tells TaxCalculator whether line amounts include GST.
 * Owns:      The PricingMode enum.
 * Uses:      Nothing.
 * Used by:   TaxCalculator.
 * Not here:  Calculation (TaxCalculator).
 * Reuse:     Enum.
 * Plan:      L2-04
 */
package io.github.someoneanoncoder.indiagst;

/** How the amounts given to the tax calculator relate to tax. */
public enum PricingMode {
  /** Amounts are before tax; tax is added on top. */
  EXCLUSIVE,
  /** Amounts are MRP-style and already contain GST and cess; tax is backed out. */
  INCLUSIVE
}
