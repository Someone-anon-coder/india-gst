/*
 * File:      HsnRules.java
 * Module:    india-gst
 * Layer:     api
 * Purpose:   Decides how many HSN digits an invoice needs and checks HSN/SAC code format.
 * Owns:      Digit requirement by aggregate turnover, omission proviso, format validation.
 * Uses:      Money (india-money).
 * Used by:   Billing and import code.
 * Not here:  Matching codes to GST rates or descriptions (HsnMaster) and any live lookup.
 * Reuse:     Static, stateless.
 * Plan:      L2-06
 */
package io.github.someoneanoncoder.indiagst;

import io.github.someoneanoncoder.indiamoney.Money;
import java.util.Optional;

/**
 * HSN (goods) and SAC (services) code rules for tax invoices.
 *
 * <p>Source: Notification No. 78/2020-Central Tax dated 15 October 2020 (G.S.R. 638(E)), issued
 * under the first proviso to rule 46 of the CGST Rules, 2017, in force from 1 April 2021: aggregate
 * turnover in the preceding financial year up to ₹5 crore requires 4 digits, more than ₹5 crore
 * requires 6 digits; a registered person with turnover up to ₹5 crore "may not mention" the digits
 * on a tax invoice for supplies to unregistered persons. Text read from the GST Council's PDF copy.
 *
 * <p>Not covered: any later notification that changes the table or the digits needed for e-invoice,
 * e-way bill or GSTR-1 reporting (not verified here), and the commentary that up to eight digits
 * may be notified. Format limits (2, 4, 6 or 8 digits) follow the Harmonized System and customs
 * tariff structure and are a library convention.
 */
public final class HsnRules {

  /** Turnover boundary: up to and including this needs 4 digits, above needs 6. */
  public static final Money FIVE_CRORE = Money.ofRupees(5_00_00_000L);

  private static final int MAX_LENGTH = 8;

  private HsnRules() {}

  /**
   * Returns the number of HSN digits required on a tax invoice.
   *
   * @param aggregateTurnover aggregate turnover in the preceding financial year
   * @return 4 if the turnover is up to ₹5 crore, otherwise 6
   */
  public static int requiredDigits(Money aggregateTurnover) {
    return aggregateTurnover.compareTo(FIVE_CRORE) <= 0 ? 4 : 6;
  }

  /**
   * Tells whether the HSN digits may be left off an invoice.
   *
   * @param aggregateTurnover aggregate turnover in the preceding financial year
   * @param recipientRegistered whether the recipient is a registered person
   * @return {@code true} only for turnover up to ₹5 crore and an unregistered recipient
   */
  public static boolean mayOmit(Money aggregateTurnover, boolean recipientRegistered) {
    return !recipientRegistered && aggregateTurnover.compareTo(FIVE_CRORE) <= 0;
  }

  /**
   * Tells whether text looks like an HSN or SAC code: ASCII digits only, 2, 4, 6 or 8 long.
   *
   * @param code the code as typed; may be null
   * @return {@code true} if well-formed
   */
  public static boolean isValidFormat(String code) {
    if (code == null || code.length() > MAX_LENGTH || code.length() % 2 != 0 || code.isEmpty()) {
      return false;
    }
    for (int i = 0; i < code.length(); i++) {
      if (code.charAt(i) < '0' || code.charAt(i) > '9') {
        return false;
      }
    }
    return true;
  }

  /**
   * Checks a code against the digits required for a turnover and recipient.
   *
   * @param code the code on the invoice line; may be null or empty if omitted
   * @param aggregateTurnover aggregate turnover in the preceding financial year
   * @param recipientRegistered whether the recipient is registered
   * @return empty if acceptable, otherwise the reason it is not
   */
  public static Optional<String> problemOf(
      String code, Money aggregateTurnover, boolean recipientRegistered) {
    boolean blank = code == null || code.isEmpty();
    if (blank) {
      return mayOmit(aggregateTurnover, recipientRegistered)
          ? Optional.empty()
          : Optional.of("HSN code is required");
    }
    if (!isValidFormat(code)) {
      return Optional.of("HSN code must be 2, 4, 6 or 8 digits");
    }
    int needed = requiredDigits(aggregateTurnover);
    return code.length() < needed
        ? Optional.of("HSN code needs at least " + needed + " digits")
        : Optional.empty();
  }
}
