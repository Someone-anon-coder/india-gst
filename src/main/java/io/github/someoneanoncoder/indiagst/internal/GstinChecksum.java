/*
 * File:      GstinChecksum.java
 * Module:    india-gst
 * Layer:     internal
 * Purpose:   Computes the GSTIN check character (Luhn mod 36).
 * Owns:      The check-character algorithm.
 * Uses:      Nothing.
 * Used by:   Gstin.
 * Not here:  Format checks or state lookup (Gstin).
 * Reuse:     Package-private; not exported.
 * Plan:      L2-01
 */
package io.github.someoneanoncoder.indiagst.internal;

/**
 * Luhn mod-36 check character for GSTINs.
 *
 * <p>Source: the algorithm is <em>not</em> published in the CGST Act or Rules. It is the generic
 * "Luhn mod N" scheme (N = 36, alphabet {@code 0-9A-Z}) that GSTIN validators and python-stdnum
 * describe. It was verified against these registered-looking GSTINs: {@code 27AAPFU0939F1ZV} and
 * {@code 33AAACC1206D1ZN}. Treat a mismatch as "probably mistyped", not as legal proof.
 */
public final class GstinChecksum {

  private static final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
  private static final int BASE = 36;

  private GstinChecksum() {}

  /**
   * Computes the check character for the first fourteen characters of a GSTIN.
   *
   * @param payload fourteen characters from {@code 0-9A-Z}
   * @return the fifteenth character
   * @throws IllegalArgumentException if {@code payload} has a character outside {@code 0-9A-Z}
   */
  public static char checkCharacterOf(CharSequence payload) {
    int factor = 2;
    int sum = 0;
    for (int i = payload.length() - 1; i >= 0; i--) {
      int value = ALPHABET.indexOf(payload.charAt(i));
      if (value < 0) {
        throw new IllegalArgumentException("not a GSTIN character at index " + i);
      }
      int product = value * factor;
      sum += product / BASE + product % BASE;
      factor = factor == 2 ? 1 : 2;
    }
    return ALPHABET.charAt((BASE - sum % BASE) % BASE);
  }
}
