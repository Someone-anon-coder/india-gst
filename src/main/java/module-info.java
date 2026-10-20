/*
 * File:      module-info.java
 * Module:    india-gst
 * Layer:     api
 * Purpose:   JPMS descriptor exporting only the public API package.
 * Owns:      The list of exported packages and the transitive india-money requirement.
 * Uses:      india-money (Money appears in the public API) and the JDK.
 * Used by:   Every consumer on the module path.
 * Not here:  Implementation classes (the internal package is not exported).
 * Reuse:     Keep exports minimal.
 * Plan:      L2
 */

/**
 * GSTIN, state codes, place of supply, GST calculation, document rules, HSN and invoice numbers.
 */
module io.github.someoneanoncoder.indiagst {
  requires transitive io.github.someoneanoncoder.indiamoney;

  exports io.github.someoneanoncoder.indiagst;
}
