/*
 * File:      FieldCondition.java
 * Module:    india-gst
 * Layer:     api
 * Purpose:   Says when a field rule applies.
 * Owns:      The FieldCondition enum.
 * Uses:      DocumentContext.
 * Used by:   FieldRule, DocumentContext.
 * Not here:  Per-document tables (DocumentRules).
 * Reuse:     Enum.
 * Plan:      L2-05
 */
package io.github.someoneanoncoder.indiagst;

/** The circumstance under which a {@link FieldRule} requires its field. */
public enum FieldCondition {
  /** Always required. */
  ALWAYS,
  /** The recipient is registered. */
  RECIPIENT_REGISTERED,
  /** The recipient is unregistered. */
  RECIPIENT_UNREGISTERED,
  /**
   * The recipient is unregistered and either the taxable supply is ₹50,000 or more (Rule 46(e)) or
   * the recipient asks for the details (Rule 46(f)).
   */
  UNREGISTERED_DETAILS_NEEDED,
  /** The supply is inter-State. */
  INTER_STATE,
  /** The delivery address differs from the place of supply. */
  DELIVERY_ADDRESS_DIFFERS,
  /** The HSN code is not omittable under the notified proviso (see {@code HsnRules}). */
  HSN_NOT_OMITTABLE,
  /** The document is not an electronic document signed under the Information Technology Act. */
  NOT_ELECTRONIC_DOCUMENT,
  /** The invoice is an e-invoice carrying an Invoice Reference Number. */
  IRN_QR_REQUIRED,
  /** The supply is an export or a supply to an SEZ. */
  EXPORT,
  /** The supply is of goods (not services). */
  GOODS,
  /** The supplier or consigner is registered. */
  SUPPLIER_REGISTERED,
  /** A delivery challan accompanies goods transported for supply to the consignee. */
  TRANSPORT_FOR_SUPPLY
}
