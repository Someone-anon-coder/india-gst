/*
 * File:      HsnMaster.java
 * Module:    india-gst
 * Layer:     api
 * Purpose:   An immutable lookup of HSN/SAC codes to descriptions.
 * Owns:      Entries and lookup by code.
 * Uses:      HsnEntry.
 * Used by:   Billing screens and the hsn-excel loader.
 * Not here:  Loading files (hsn-excel module).
 * Reuse:     Value type.
 * Plan:      L2-06
 */
package io.github.someoneanoncoder.indiagst;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Lookup table of HSN and SAC codes. Build it by hand or load it with the {@code hsn-excel} module.
 */
public final class HsnMaster {

  private final Map<String, HsnEntry> entries;

  private HsnMaster(Map<String, HsnEntry> entries) {
    this.entries = entries;
  }

  /**
   * Builds a master from entries; a later entry with the same code replaces an earlier one.
   *
   * @param list the entries
   * @return the master
   */
  public static HsnMaster of(Collection<HsnEntry> list) {
    Map<String, HsnEntry> map = new LinkedHashMap<>();
    for (HsnEntry entry : list) {
      map.put(entry.code(), entry);
    }
    return new HsnMaster(Map.copyOf(map));
  }

  /**
   * Finds an entry.
   *
   * @param code the HSN or SAC digits
   * @return the entry, or empty
   */
  public Optional<HsnEntry> find(String code) {
    return Optional.ofNullable(code == null ? null : entries.get(code));
  }

  /**
   * Returns the number of codes.
   *
   * @return entry count
   */
  public int size() {
    return entries.size();
  }
}
