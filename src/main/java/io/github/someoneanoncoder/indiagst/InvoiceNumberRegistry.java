/*
 * File:      InvoiceNumberRegistry.java
 * Module:    india-gst
 * Layer:     api
 * Purpose:   Contract a caller implements to prove an invoice number is not yet used.
 * Owns:      The uniqueness lookup interface.
 * Uses:      InvoiceNumber.
 * Used by:   InvoiceNumber.requireUnused and billing storage.
 * Not here:  Storage (the caller's database).
 * Reuse:     Functional interface.
 * Plan:      L2-07
 */
package io.github.someoneanoncoder.indiagst;

/**
 * Looks up issued invoice numbers.
 *
 * <p>Contract: rule 46(b) of the CGST Rules, 2017 requires the serial number to be "unique for a
 * financial year", across all series of one registered person. The implementor chooses the scope
 * key (for example GSTIN plus financial year) and must make {@code isUsed} and the later insert
 * atomic, for example with a unique index; this library cannot enforce atomicity.
 */
@FunctionalInterface
public interface InvoiceNumberRegistry {

  /**
   * Tells whether the number was already issued in the scope.
   *
   * @param scopeKey identifies the registered person and financial year
   * @param number the candidate number
   * @return {@code true} if already issued
   */
  boolean isUsed(String scopeKey, InvoiceNumber number);
}
