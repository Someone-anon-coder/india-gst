/*
 * File:      HsnEntry.java
 * Module:    india-gst
 * Layer:     api
 * Purpose:   One row of the HSN/SAC master.
 * Owns:      Code and description.
 * Uses:      Nothing.
 * Used by:   HsnMaster, hsn-excel loader.
 * Not here:  Rates (they change by notification).
 * Reuse:     Value type.
 * Plan:      L2-06
 */
package io.github.someoneanoncoder.indiagst;

import java.util.Objects;

/**
 * A code and its description from an HSN/SAC master.
 *
 * @param code the HSN or SAC digits (valid per {@link HsnRules#isValidFormat(String)})
 * @param description the official description
 */
public record HsnEntry(String code, String description) {

  /**
   * Validates the entry.
   *
   * @param code the HSN or SAC digits
   * @param description the description
   * @throws IllegalArgumentException if the code is malformed
   */
  public HsnEntry {
    Objects.requireNonNull(description, "description");
    if (!HsnRules.isValidFormat(code)) {
      throw new IllegalArgumentException("malformed HSN code");
    }
  }
}
