/*
 * File:      StateTaxKind.java
 * Module:    india-gst
 * Layer:     api
 * Purpose:   Names the state-level tax head (SGST or UTGST) that pairs with central tax.
 * Owns:      The StateTaxKind enum.
 * Uses:      Nothing.
 * Used by:   StateCode, PlaceOfSupply, TaxCalculator.
 * Not here:  Rates or amounts (TaxLine).
 * Reuse:     Enum.
 * Plan:      L2-02
 */
package io.github.someoneanoncoder.indiagst;

/** The state-level GST head that is charged next to central tax on an intra-State supply. */
public enum StateTaxKind {
  /**
   * State tax (CGST Act, 2017 pairs it with central tax; SGST Acts of States and legislature UTs).
   */
  SGST,
  /** Union territory tax under the UTGST Act, 2017 (union territories without a legislature). */
  UTGST,
  /** No state-level head applies (inter-State supply or Other Territory). */
  NOT_APPLICABLE
}
