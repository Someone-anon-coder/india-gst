/*
 * File:      DocumentField.java
 * Module:    india-gst
 * Layer:     api
 * Purpose:   Names every particular that CGST Rules 46, 49, 53 and 55 can require on a document.
 * Owns:      The DocumentField enum.
 * Uses:      Nothing.
 * Used by:   DocumentKind, FieldRule, billing and print code.
 * Not here:  Which document needs which field (DocumentRules).
 * Reuse:     Enum.
 * Plan:      L2-05
 */
package io.github.someoneanoncoder.indiagst;

/** A particular printed on a GST document. */
public enum DocumentField {
  /** Name of the supplier (consigner on a delivery challan). */
  SUPPLIER_NAME,
  /** Address of the supplier (consigner on a delivery challan). */
  SUPPLIER_ADDRESS,
  /** GSTIN of the supplier (consigner on a delivery challan, if registered). */
  SUPPLIER_GSTIN,
  /** Consecutive serial number, at most sixteen characters. */
  SERIAL_NUMBER,
  /** Date of issue. */
  ISSUE_DATE,
  /** Name of the recipient (consignee on a delivery challan). */
  RECIPIENT_NAME,
  /** Address of the recipient (consignee on a delivery challan). */
  RECIPIENT_ADDRESS,
  /** GSTIN or UIN of the recipient. */
  RECIPIENT_GSTIN_OR_UIN,
  /** Address of delivery. */
  DELIVERY_ADDRESS,
  /** Name of the State and its code for the recipient. */
  RECIPIENT_STATE_NAME_AND_CODE,
  /** Name of the country of destination (exports). */
  COUNTRY_OF_DESTINATION,
  /** Export endorsement text required by the third proviso to Rule 46. */
  EXPORT_ENDORSEMENT,
  /** HSN code (HSN or SAC) of the goods or services. */
  HSN_CODE,
  /** Description of goods or services. */
  DESCRIPTION,
  /** Quantity and unit or Unique Quantity Code. */
  QUANTITY_AND_UNIT,
  /** Total value of supply. */
  TOTAL_VALUE,
  /** Taxable value after discount or abatement. */
  TAXABLE_VALUE,
  /** Rate of each tax head. */
  TAX_RATE,
  /** Amount of each tax head. */
  TAX_AMOUNT,
  /** Place of supply with the name of the State. */
  PLACE_OF_SUPPLY,
  /** Whether tax is payable on reverse charge. */
  REVERSE_CHARGE_INDICATOR,
  /** Signature or digital signature of the supplier or representative. */
  SIGNATURE,
  /** QR code embedding the Invoice Reference Number (e-invoice). */
  IRN_QR_CODE,
  /** Nature of the document (credit note or debit note). */
  NATURE_OF_DOCUMENT,
  /** Serial number and date of the corresponding tax invoice or bill of supply. */
  ORIGINAL_DOCUMENT_REFERENCE
}
