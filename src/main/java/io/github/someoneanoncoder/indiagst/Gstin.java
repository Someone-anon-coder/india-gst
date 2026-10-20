/*
 * File:      Gstin.java
 * Module:    india-gst
 * Layer:     api
 * Purpose:   Immutable, validated 15-character GST Identification Number.
 * Owns:      Format check, state extraction, embedded PAN, entity number and check character.
 * Uses:      StateCode, GstinChecksum (internal).
 * Used by:   PlaceOfSupply, DocumentChecklist, billing code holding a party GSTIN.
 * Not here:  Live registration status (needs the GST portal), UIN/TDS/TCS identifiers, PAN validation.
 * Reuse:     Value type; construct with parse or tryParse.
 * Plan:      L2-01
 */
package io.github.someoneanoncoder.indiagst;

import io.github.someoneanoncoder.indiagst.internal.GstinChecksum;
import java.util.Objects;
import java.util.Optional;

/**
 * A GST Identification Number of a normal taxpayer: 2-digit state code, 10-character PAN, entity
 * number (1-9 then A-Z), the fixed letter {@code Z}, and a check character.
 *
 * <p>Source for the layout: GST registration documentation and the published GSTIN structure (state
 * code, PAN, registration count per PAN in the state, {@code Z}, check character). The layout is
 * <em>not</em> spelled out in the CGST Rules; Rule 46(a) only requires the GSTIN on the invoice.
 * The check character uses {@code GstinChecksum} (Luhn mod 36, unofficial but widely verified). A
 * valid GSTIN is only well-formed: it may be cancelled or never issued. Identifiers of other kinds
 * (UIN, TDS/TCS deductors, non-resident taxpayers) are not accepted.
 *
 * <p>Input is untrusted: {@link #parse(String)} rejects anything longer than 15 characters before
 * looking at content and never throws anything but {@link IllegalArgumentException}.
 */
public final class Gstin {

  private static final int LENGTH = 15;

  private final String value;
  private final StateCode state;

  private Gstin(String value, StateCode state) {
    this.value = value;
    this.state = state;
  }

  /**
   * Parses and validates a GSTIN. Surrounding spaces are removed and letters are upper-cased.
   *
   * @param text the GSTIN as typed
   * @return the validated GSTIN
   * @throws IllegalArgumentException with the reason if {@code text} is null, has the wrong length
   *     or layout, has an unknown state code, or fails the check character
   */
  public static Gstin parse(String text) {
    String problem = problemOf(text);
    if (problem != null) {
      throw new IllegalArgumentException(problem);
    }
    String clean = text.strip().toUpperCase(java.util.Locale.ROOT);
    return new Gstin(clean, StateCode.fromCode(clean.substring(0, 2)).orElseThrow());
  }

  /**
   * Parses a GSTIN without throwing.
   *
   * @param text the GSTIN as typed; may be null
   * @return the GSTIN, or empty if invalid
   */
  public static Optional<Gstin> tryParse(String text) {
    return problemOf(text) == null ? Optional.of(parse(text)) : Optional.empty();
  }

  /**
   * Tells whether the text is a valid GSTIN.
   *
   * @param text the GSTIN as typed; may be null
   * @return {@code true} if {@link #parse(String)} would succeed
   */
  public static boolean isValid(String text) {
    return problemOf(text) == null;
  }

  /**
   * Builds a GSTIN by appending the correct check character to a 14-character prefix.
   *
   * @param prefix the first fourteen characters: state code, PAN, entity number and {@code Z}
   * @return the completed, validated GSTIN
   * @throws IllegalArgumentException if the prefix is not fourteen valid characters
   */
  public static Gstin generate(String prefix) {
    if (prefix == null || prefix.length() != LENGTH - 1) {
      throw new IllegalArgumentException("GSTIN prefix must have 14 characters");
    }
    String upper = prefix.toUpperCase(java.util.Locale.ROOT);
    if (!isLayoutValid(upper + "0")) {
      throw new IllegalArgumentException("GSTIN prefix has an invalid layout");
    }
    return parse(upper + GstinChecksum.checkCharacterOf(upper));
  }

  private static String problemOf(String text) {
    if (text == null) {
      return "GSTIN is null";
    }
    String clean = text.strip();
    if (clean.length() != LENGTH) {
      return "GSTIN must have 15 characters";
    }
    String upper = clean.toUpperCase(java.util.Locale.ROOT);
    if (!isLayoutValid(upper)) {
      return "GSTIN layout is invalid";
    }
    if (StateCode.fromCode(upper.substring(0, 2)).isEmpty()
        || StateCode.fromCode(upper.substring(0, 2)).orElseThrow() == StateCode.OTHER_TERRITORY) {
      return "GSTIN state code is unknown";
    }
    if (GstinChecksum.checkCharacterOf(upper.substring(0, LENGTH - 1))
        != upper.charAt(LENGTH - 1)) {
      return "GSTIN check character does not match";
    }
    return null;
  }

  private static boolean isLayoutValid(String upper) {
    return isDigits(upper, 0, 2)
        && isLetters(upper, 2, 7)
        && isDigits(upper, 7, 11)
        && isLetters(upper, 11, 12)
        && isEntityNumber(upper.charAt(12))
        && upper.charAt(13) == 'Z'
        && isAlphanumeric(upper.charAt(14));
  }

  private static boolean isDigits(String s, int from, int to) {
    for (int i = from; i < to; i++) {
      if (s.charAt(i) < '0' || s.charAt(i) > '9') {
        return false;
      }
    }
    return true;
  }

  private static boolean isLetters(String s, int from, int to) {
    for (int i = from; i < to; i++) {
      if (s.charAt(i) < 'A' || s.charAt(i) > 'Z') {
        return false;
      }
    }
    return true;
  }

  private static boolean isEntityNumber(char c) {
    return (c >= '1' && c <= '9') || (c >= 'A' && c <= 'Z');
  }

  private static boolean isAlphanumeric(char c) {
    return (c >= '0' && c <= '9') || (c >= 'A' && c <= 'Z');
  }

  /**
   * Returns the state encoded in the first two characters.
   *
   * @return the state or union territory
   */
  public StateCode state() {
    return state;
  }

  /**
   * Returns the PAN embedded in characters 3 to 12.
   *
   * @return the ten-character PAN (format only; not checked against the Income Tax Department)
   */
  public String pan() {
    return value.substring(2, 12);
  }

  /**
   * Returns the entity number: how many registrations the PAN holder has in the state.
   *
   * @return the thirteenth character, {@code 1-9} then {@code A-Z}
   */
  public char entityNumber() {
    return value.charAt(12);
  }

  /**
   * Returns the check character.
   *
   * @return the fifteenth character
   */
  public char checkCharacter() {
    return value.charAt(14);
  }

  /**
   * Returns the canonical text.
   *
   * @return fifteen upper-case characters
   */
  public String value() {
    return value;
  }

  @Override
  public boolean equals(Object other) {
    return other instanceof Gstin g && value.equals(g.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }

  @Override
  public String toString() {
    return value;
  }
}
