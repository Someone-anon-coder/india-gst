/*
 * File:      DocumentFact.java
 * Module:    india-gst
 * Layer:     api
 * Purpose:   Names the yes/no facts about a document that decide which fields are required.
 * Owns:      The DocumentFact enum.
 * Uses:      Nothing.
 * Used by:   DocumentContext.
 * Not here:  Rule data (DocumentRules).
 * Reuse:     Enum.
 * Plan:      L2-05
 */
package io.github.someoneanoncoder.indiagst;

/** A fact about a document that the caller knows and the checklist needs. */
public enum DocumentFact {
  /** The recipient holds a GSTIN or UIN. */
  RECIPIENT_REGISTERED,
  /** An unregistered recipient asked for their details to be recorded (Rule 46(f)). */
  RECIPIENT_REQUESTED_DETAILS,
  /** The supply is inter-State. */
  INTER_STATE,
  /** Delivery address differs from the place of supply. */
  DELIVERY_ADDRESS_DIFFERS,
  /** The HSN digits may be omitted (turnover up to five crore and unregistered recipient). */
  HSN_MAY_BE_OMITTED,
  /** The document is electronic and signature is not required (proviso to Rule 46, Rule 49). */
  ELECTRONIC_DOCUMENT,
  /** The invoice is issued as an e-invoice under Rule 48(4). */
  IRN_QR_REQUIRED,
  /** The supply is an export or to an SEZ. */
  EXPORT,
  /** The supply is of goods (not services). */
  GOODS,
  /** The supplier or consigner is registered. */
  SUPPLIER_REGISTERED,
  /** Goods move for supply to the consignee (delivery challan). */
  TRANSPORT_FOR_SUPPLY
}
