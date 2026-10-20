/*
 * File:      SupplyType.java
 * Module:    india-gst
 * Layer:     api
 * Purpose:   Classifies a supply as intra-State or inter-State.
 * Owns:      The SupplyType enum.
 * Uses:      Nothing.
 * Used by:   PlaceOfSupply, TaxCalculator.
 * Not here:  Resolution logic (PlaceOfSupply).
 * Reuse:     Enum.
 * Plan:      L2-03
 */
package io.github.someoneanoncoder.indiagst;

/** Whether a supply attracts central plus state tax, or integrated tax. */
public enum SupplyType {
  /**
   * Supplier location and place of supply are in the same State or union territory (IGST Act, 2017,
   * section 8).
   */
  INTRA_STATE,
  /**
   * Supplier location and place of supply are in different States or union territories (IGST Act,
   * 2017, section 7).
   */
  INTER_STATE
}
