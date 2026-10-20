/*
 * File:      StateCode.java
 * Module:    india-gst
 * Layer:     api
 * Purpose:   GST state and union territory codes as printed in a GSTIN and on invoices.
 * Owns:      The code table, names, tax-head kind and special-category flag.
 * Uses:      Nothing.
 * Used by:   Gstin, PlaceOfSupply, TaxCalculator and any billing code that prints a state code.
 * Not here:  Place-of-supply logic (PlaceOfSupply) and GSTIN parsing (Gstin).
 * Reuse:     Enum; look up with fromCode. Never renumber values.
 * Plan:      L2-02
 */
package io.github.someoneanoncoder.indiagst;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Two-digit GST state and union-territory codes (the first two characters of a GSTIN).
 *
 * <p>Source: the code list printed by the GST portal and CBIC and reproduced widely (Rule 46(e),
 * (f) and (n) of the CGST Rules, 2017 require "the name of the State and its code" on invoices).
 * <strong>Verification status:</strong> the official table could not be fetched when this class was
 * written; the list was assembled from several consistent secondary sources and checked against the
 * 2019-2020 reorganisations (Ladakh code 38 and the merger into Dadra and Nagar Haveli and Daman
 * and Diu under code 26). Re-check against the GST portal before relying on it.
 *
 * <p>State tax head: a State or a union territory with a legislature (Delhi, Puducherry, Jammu and
 * Kashmir) levies SGST; a union territory without a legislature (Andaman and Nicobar Islands,
 * Chandigarh, Dadra and Nagar Haveli and Daman and Diu, Ladakh, Lakshadweep) levies UTGST (UTGST
 * Act, 2017, section 2(8) lists the territories; CGST Act, 2017, section 2(103) includes a union
 * territory with legislature in "State"). The Jammu and Kashmir treatment is taken from the CGST
 * Act definition and was not confirmed against a primary source here.
 *
 * <p>Special category: {@link #isSpecialCategory()} marks the States named in the Explanation to
 * section 22 of the CGST Act, 2017 for the lower registration threshold (Manipur, Mizoram,
 * Nagaland, Tripura). This list comes from secondary sources and is <em>not</em> the wider Article
 * 279A(4)(g) list of eleven States.
 */
public enum StateCode {
  /** Jammu and Kashmir (union territory with legislature). */
  JAMMU_AND_KASHMIR("01", "Jammu and Kashmir", Kind.UT_WITH_LEGISLATURE),
  /** Himachal Pradesh. */
  HIMACHAL_PRADESH("02", "Himachal Pradesh", Kind.STATE),
  /** Punjab. */
  PUNJAB("03", "Punjab", Kind.STATE),
  /** Chandigarh (union territory without legislature). */
  CHANDIGARH("04", "Chandigarh", Kind.UT_WITHOUT_LEGISLATURE),
  /** Uttarakhand. */
  UTTARAKHAND("05", "Uttarakhand", Kind.STATE),
  /** Haryana. */
  HARYANA("06", "Haryana", Kind.STATE),
  /** Delhi (union territory with legislature). */
  DELHI("07", "Delhi", Kind.UT_WITH_LEGISLATURE),
  /** Rajasthan. */
  RAJASTHAN("08", "Rajasthan", Kind.STATE),
  /** Uttar Pradesh. */
  UTTAR_PRADESH("09", "Uttar Pradesh", Kind.STATE),
  /** Bihar. */
  BIHAR("10", "Bihar", Kind.STATE),
  /** Sikkim. */
  SIKKIM("11", "Sikkim", Kind.STATE),
  /** Arunachal Pradesh. */
  ARUNACHAL_PRADESH("12", "Arunachal Pradesh", Kind.STATE),
  /** Nagaland (special category). */
  NAGALAND("13", "Nagaland", Kind.STATE, true, false),
  /** Manipur (special category). */
  MANIPUR("14", "Manipur", Kind.STATE, true, false),
  /** Mizoram (special category). */
  MIZORAM("15", "Mizoram", Kind.STATE, true, false),
  /** Tripura (special category). */
  TRIPURA("16", "Tripura", Kind.STATE, true, false),
  /** Meghalaya. */
  MEGHALAYA("17", "Meghalaya", Kind.STATE),
  /** Assam. */
  ASSAM("18", "Assam", Kind.STATE),
  /** West Bengal. */
  WEST_BENGAL("19", "West Bengal", Kind.STATE),
  /** Jharkhand. */
  JHARKHAND("20", "Jharkhand", Kind.STATE),
  /** Odisha. */
  ODISHA("21", "Odisha", Kind.STATE),
  /** Chhattisgarh. */
  CHHATTISGARH("22", "Chhattisgarh", Kind.STATE),
  /** Madhya Pradesh. */
  MADHYA_PRADESH("23", "Madhya Pradesh", Kind.STATE),
  /** Gujarat. */
  GUJARAT("24", "Gujarat", Kind.STATE),
  /** Daman and Diu: code used before the January 2020 merger; legacy. */
  DAMAN_AND_DIU_LEGACY("25", "Daman and Diu", Kind.UT_WITHOUT_LEGISLATURE, false, true),
  /** Dadra and Nagar Haveli and Daman and Diu (union territory without legislature). */
  DADRA_AND_NAGAR_HAVELI_AND_DAMAN_AND_DIU(
      "26", "Dadra and Nagar Haveli and Daman and Diu", Kind.UT_WITHOUT_LEGISLATURE),
  /** Maharashtra. */
  MAHARASHTRA("27", "Maharashtra", Kind.STATE),
  /** Andhra Pradesh as it was before the 2014 bifurcation; legacy, current code is 37. */
  ANDHRA_PRADESH_LEGACY("28", "Andhra Pradesh (before division)", Kind.STATE, false, true),
  /** Karnataka. */
  KARNATAKA("29", "Karnataka", Kind.STATE),
  /** Goa. */
  GOA("30", "Goa", Kind.STATE),
  /** Lakshadweep (union territory without legislature). */
  LAKSHADWEEP("31", "Lakshadweep", Kind.UT_WITHOUT_LEGISLATURE),
  /** Kerala. */
  KERALA("32", "Kerala", Kind.STATE),
  /** Tamil Nadu. */
  TAMIL_NADU("33", "Tamil Nadu", Kind.STATE),
  /** Puducherry (union territory with legislature). */
  PUDUCHERRY("34", "Puducherry", Kind.UT_WITH_LEGISLATURE),
  /** Andaman and Nicobar Islands (union territory without legislature). */
  ANDAMAN_AND_NICOBAR_ISLANDS("35", "Andaman and Nicobar Islands", Kind.UT_WITHOUT_LEGISLATURE),
  /** Telangana. */
  TELANGANA("36", "Telangana", Kind.STATE),
  /** Andhra Pradesh (current code). */
  ANDHRA_PRADESH("37", "Andhra Pradesh", Kind.STATE),
  /** Ladakh (union territory without legislature). */
  LADAKH("38", "Ladakh", Kind.UT_WITHOUT_LEGISLATURE),
  /** Other Territory: used for places outside the States and union territories listed above. */
  OTHER_TERRITORY("97", "Other Territory", Kind.OTHER_TERRITORY);

  /** Constitutional category of a code, which decides the state-level tax head. */
  public enum Kind {
    /** A State: levies SGST. */
    STATE,
    /** A union territory with a legislature: levies SGST. */
    UT_WITH_LEGISLATURE,
    /** A union territory without a legislature: levies UTGST. */
    UT_WITHOUT_LEGISLATURE,
    /** Not a State or union territory (code 97): no state-level tax head. */
    OTHER_TERRITORY
  }

  private static final Map<String, StateCode> BY_CODE = new HashMap<>();

  static {
    for (StateCode state : values()) {
      BY_CODE.put(state.code, state);
    }
  }

  private final String code;
  private final String displayName;
  private final Kind kind;
  private final boolean specialCategory;
  private final boolean legacy;

  StateCode(String code, String displayName, Kind kind) {
    this(code, displayName, kind, false, false);
  }

  StateCode(String code, String displayName, Kind kind, boolean specialCategory, boolean legacy) {
    this.code = code;
    this.displayName = displayName;
    this.kind = kind;
    this.specialCategory = specialCategory;
    this.legacy = legacy;
  }

  /**
   * Finds the state for a two-digit code.
   *
   * @param code two ASCII digits such as {@code "27"}
   * @return the state, or empty if the code is unknown (including 96, 99 and 00)
   */
  public static Optional<StateCode> fromCode(String code) {
    return Optional.ofNullable(code == null ? null : BY_CODE.get(code));
  }

  /**
   * Returns the two-digit code.
   *
   * @return code with a leading zero where needed, for example {@code "07"}
   */
  public String code() {
    return code;
  }

  /**
   * Returns the English name as used on invoices.
   *
   * @return the display name
   */
  public String displayName() {
    return displayName;
  }

  /**
   * Returns the constitutional category.
   *
   * @return the kind
   */
  public Kind kind() {
    return kind;
  }

  /**
   * Tells whether this is a special-category State for the section 22 registration threshold.
   *
   * @return {@code true} for Manipur, Mizoram, Nagaland and Tripura (unverified list, see class
   *     documentation)
   */
  public boolean isSpecialCategory() {
    return specialCategory;
  }

  /**
   * Tells whether this code was replaced by a newer one and should not be issued to new
   * registrations, although old documents may still carry it.
   *
   * @return {@code true} for 25 (Daman and Diu) and 28 (Andhra Pradesh before division)
   */
  public boolean isLegacy() {
    return legacy;
  }

  /**
   * Tells whether this is a union territory (with or without a legislature).
   *
   * @return {@code true} for union territories
   */
  public boolean isUnionTerritory() {
    return kind == Kind.UT_WITH_LEGISLATURE || kind == Kind.UT_WITHOUT_LEGISLATURE;
  }

  /**
   * Returns the state-level tax head levied on intra-State supplies in this place.
   *
   * @return {@link StateTaxKind#SGST}, {@link StateTaxKind#UTGST}, or {@link
   *     StateTaxKind#NOT_APPLICABLE} for Other Territory
   */
  public StateTaxKind stateTaxKind() {
    return switch (kind) {
      case STATE, UT_WITH_LEGISLATURE -> StateTaxKind.SGST;
      case UT_WITHOUT_LEGISLATURE -> StateTaxKind.UTGST;
      case OTHER_TERRITORY -> StateTaxKind.NOT_APPLICABLE;
    };
  }
}
