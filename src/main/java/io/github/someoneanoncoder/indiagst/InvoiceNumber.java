/*
 * File:      InvoiceNumber.java
 * Module:    india-gst
 * Layer:     api
 * Purpose:   A validated invoice, credit note, debit note or challan serial number.
 * Owns:      Length and character rules of rule 46(b).
 * Uses:      InvoiceNumberRegistry.
 * Used by:   Billing and numbering services.
 * Not here:  Generating or storing the sequence (the product's numbering service) and GSTR-1 formats.
 * Reuse:     Value type.
 * Plan:      L2-07
 */
package io.github.someoneanoncoder.indiagst;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * A document serial number as limited by rule 46(b) of the CGST Rules, 2017: "a consecutive serial
 * number not exceeding sixteen characters, in one or multiple series, containing alphabets or
 * numerals or special characters - hyphen or dash and slash symbolised as '-' and '/' respectively,
 * and any combination thereof, unique for a financial year". Rules 49(b), 50(b), 53(1A)(c) and
 * 55(1) repeat the sixteen-character limit (text from the CBIC consolidation of 24 September 2021).
 *
 * <p>Conventions chosen here: alphabets means ASCII letters only; spaces and other symbols are
 * rejected; letters keep their case but {@link #uniquenessKey()} folds case so that {@code a/1} and
 * {@code A/1} count as the same number. "Consecutive" and "unique" cannot be checked on one value;
 * use {@link #requireUnused(String, InvoiceNumberRegistry)}.
 */
public final class InvoiceNumber {

  /** The maximum length under rule 46(b). */
  public static final int MAX_LENGTH = 16;

  private final String value;

  private InvoiceNumber(String value) {
    this.value = value;
  }

  /**
   * Validates a serial number.
   *
   * @param text the number as typed; not trimmed
   * @return the number
   * @throws IllegalArgumentException with the reason if it is null, empty, longer than 16
   *     characters or contains a character other than ASCII letters, digits, '-' and '/'
   */
  public static InvoiceNumber parse(String text) {
    String problem = problemOf(text);
    if (problem != null) {
      throw new IllegalArgumentException(problem);
    }
    return new InvoiceNumber(text);
  }

  /**
   * Validates a serial number without throwing.
   *
   * @param text the number as typed; may be null
   * @return the number, or empty if invalid
   */
  public static Optional<InvoiceNumber> tryParse(String text) {
    return problemOf(text) == null ? Optional.of(new InvoiceNumber(text)) : Optional.empty();
  }

  /**
   * Tells whether the text is a valid serial number.
   *
   * @param text the number as typed; may be null
   * @return {@code true} if valid
   */
  public static boolean isValid(String text) {
    return problemOf(text) == null;
  }

  /**
   * Joins a series prefix and a sequence number, zero-padded.
   *
   * @param prefix series prefix such as {@code "A/25-26/"}; may be empty
   * @param sequence the sequence, zero or more
   * @param minDigits minimum digits of the sequence, padded with zeros
   * @return the number
   * @throws IllegalArgumentException if the result is not valid, for example longer than 16
   */
  public static InvoiceNumber of(String prefix, long sequence, int minDigits) {
    if (sequence < 0 || minDigits < 1 || minDigits > MAX_LENGTH) {
      throw new IllegalArgumentException("sequence or width out of range");
    }
    String digits = Long.toString(sequence);
    String padded = "0".repeat(Math.max(0, minDigits - digits.length())) + digits;
    return parse(Objects.requireNonNull(prefix, "prefix") + padded);
  }

  /**
   * Fails if the number was already issued in the scope.
   *
   * @param scopeKey identifies the registered person and financial year
   * @param registry the issued-number lookup
   * @throws IllegalStateException if the number is already used
   */
  public void requireUnused(String scopeKey, InvoiceNumberRegistry registry) {
    if (registry.isUsed(scopeKey, this)) {
      throw new IllegalStateException("invoice number already used: " + value);
    }
  }

  /**
   * Returns the text as typed.
   *
   * @return the number
   */
  public String value() {
    return value;
  }

  /**
   * Returns a case-folded form for comparing numbers.
   *
   * @return upper-case text
   */
  public String uniquenessKey() {
    return value.toUpperCase(Locale.ROOT);
  }

  @Override
  public boolean equals(Object other) {
    return other instanceof InvoiceNumber n && value.equals(n.value);
  }

  @Override
  public int hashCode() {
    return value.hashCode();
  }

  @Override
  public String toString() {
    return value;
  }

  private static String problemOf(String text) {
    if (text == null || text.isEmpty()) {
      return "invoice number is empty";
    }
    if (text.length() > MAX_LENGTH) {
      return "invoice number is longer than 16 characters";
    }
    for (int i = 0; i < text.length(); i++) {
      char c = text.charAt(i);
      boolean ok =
          (c >= 'A' && c <= 'Z')
              || (c >= 'a' && c <= 'z')
              || (c >= '0' && c <= '9')
              || c == '-'
              || c == '/';
      if (!ok) {
        return "invoice number has a character other than letters, digits, '-' and '/'";
      }
    }
    return null;
  }
}
