/*
 * File:      DocumentRules.java
 * Module:    india-gst
 * Layer:     api
 * Purpose:   Holds the field checklists of GST documents as data, one list per DocumentKind.
 * Owns:      The tables derived from CGST Rules 46, 49, 53 and 55.
 * Uses:      FieldRule, DocumentField, FieldCondition.
 * Used by:   DocumentKind.
 * Not here:  Evaluation (DocumentKind and DocumentContext) and e-invoice schema validation.
 * Reuse:     Package-private data; read through DocumentKind.
 * Plan:      L2-05
 */
package io.github.someoneanoncoder.indiagst;

import static io.github.someoneanoncoder.indiagst.DocumentField.COUNTRY_OF_DESTINATION;
import static io.github.someoneanoncoder.indiagst.DocumentField.DELIVERY_ADDRESS;
import static io.github.someoneanoncoder.indiagst.DocumentField.DESCRIPTION;
import static io.github.someoneanoncoder.indiagst.DocumentField.EXPORT_ENDORSEMENT;
import static io.github.someoneanoncoder.indiagst.DocumentField.HSN_CODE;
import static io.github.someoneanoncoder.indiagst.DocumentField.IRN_QR_CODE;
import static io.github.someoneanoncoder.indiagst.DocumentField.ISSUE_DATE;
import static io.github.someoneanoncoder.indiagst.DocumentField.NATURE_OF_DOCUMENT;
import static io.github.someoneanoncoder.indiagst.DocumentField.ORIGINAL_DOCUMENT_REFERENCE;
import static io.github.someoneanoncoder.indiagst.DocumentField.PLACE_OF_SUPPLY;
import static io.github.someoneanoncoder.indiagst.DocumentField.QUANTITY_AND_UNIT;
import static io.github.someoneanoncoder.indiagst.DocumentField.RECIPIENT_ADDRESS;
import static io.github.someoneanoncoder.indiagst.DocumentField.RECIPIENT_GSTIN_OR_UIN;
import static io.github.someoneanoncoder.indiagst.DocumentField.RECIPIENT_NAME;
import static io.github.someoneanoncoder.indiagst.DocumentField.RECIPIENT_STATE_NAME_AND_CODE;
import static io.github.someoneanoncoder.indiagst.DocumentField.REVERSE_CHARGE_INDICATOR;
import static io.github.someoneanoncoder.indiagst.DocumentField.SERIAL_NUMBER;
import static io.github.someoneanoncoder.indiagst.DocumentField.SIGNATURE;
import static io.github.someoneanoncoder.indiagst.DocumentField.SUPPLIER_ADDRESS;
import static io.github.someoneanoncoder.indiagst.DocumentField.SUPPLIER_GSTIN;
import static io.github.someoneanoncoder.indiagst.DocumentField.SUPPLIER_NAME;
import static io.github.someoneanoncoder.indiagst.DocumentField.TAXABLE_VALUE;
import static io.github.someoneanoncoder.indiagst.DocumentField.TAX_AMOUNT;
import static io.github.someoneanoncoder.indiagst.DocumentField.TAX_RATE;
import static io.github.someoneanoncoder.indiagst.DocumentField.TOTAL_VALUE;
import static io.github.someoneanoncoder.indiagst.FieldCondition.ALWAYS;
import static io.github.someoneanoncoder.indiagst.FieldCondition.DELIVERY_ADDRESS_DIFFERS;
import static io.github.someoneanoncoder.indiagst.FieldCondition.EXPORT;
import static io.github.someoneanoncoder.indiagst.FieldCondition.GOODS;
import static io.github.someoneanoncoder.indiagst.FieldCondition.HSN_NOT_OMITTABLE;
import static io.github.someoneanoncoder.indiagst.FieldCondition.INTER_STATE;
import static io.github.someoneanoncoder.indiagst.FieldCondition.IRN_QR_REQUIRED;
import static io.github.someoneanoncoder.indiagst.FieldCondition.NOT_ELECTRONIC_DOCUMENT;
import static io.github.someoneanoncoder.indiagst.FieldCondition.RECIPIENT_REGISTERED;
import static io.github.someoneanoncoder.indiagst.FieldCondition.RECIPIENT_UNREGISTERED;
import static io.github.someoneanoncoder.indiagst.FieldCondition.SUPPLIER_REGISTERED;
import static io.github.someoneanoncoder.indiagst.FieldCondition.TRANSPORT_FOR_SUPPLY;
import static io.github.someoneanoncoder.indiagst.FieldCondition.UNREGISTERED_DETAILS_NEEDED;

import java.util.List;

/**
 * Checklist data. Source text: CGST Rules, 2017 as consolidated by CBIC on 24 September 2021 (rules
 * 46, 46A, 49, 53, 55 including the provisos and notification references printed there).
 * <strong>Amendments after that date are not reflected</strong> (for example, secondary sources
 * report changes to rule 46 by notifications 51/2023-CT of 29 September 2023 and 20/2024-CT of
 * 2024); re-read the current rules before each release.
 */
final class DocumentRules {

  private static final String R46 = "CGST Rules 2017, rule 46";
  private static final String R49 = "CGST Rules 2017, rule 49";
  private static final String R53 = "CGST Rules 2017, rule 53(1A)";
  private static final String R55 = "CGST Rules 2017, rule 55(1)";

  static final List<FieldRule> TAX_INVOICE =
      List.of(
          rule(SUPPLIER_NAME, ALWAYS, R46 + "(a)"),
          rule(SUPPLIER_ADDRESS, ALWAYS, R46 + "(a)"),
          rule(SUPPLIER_GSTIN, ALWAYS, R46 + "(a)"),
          rule(SERIAL_NUMBER, ALWAYS, R46 + "(b)"),
          rule(ISSUE_DATE, ALWAYS, R46 + "(c)"),
          rule(RECIPIENT_NAME, RECIPIENT_REGISTERED, R46 + "(d)"),
          rule(RECIPIENT_ADDRESS, RECIPIENT_REGISTERED, R46 + "(d)"),
          rule(RECIPIENT_GSTIN_OR_UIN, RECIPIENT_REGISTERED, R46 + "(d)"),
          rule(RECIPIENT_NAME, UNREGISTERED_DETAILS_NEEDED, R46 + "(e),(f)"),
          rule(RECIPIENT_ADDRESS, UNREGISTERED_DETAILS_NEEDED, R46 + "(e),(f)"),
          rule(DELIVERY_ADDRESS, UNREGISTERED_DETAILS_NEEDED, R46 + "(e),(f)"),
          rule(RECIPIENT_STATE_NAME_AND_CODE, UNREGISTERED_DETAILS_NEEDED, R46 + "(e),(f)"),
          rule(COUNTRY_OF_DESTINATION, EXPORT, R46 + ", proviso on exports"),
          rule(EXPORT_ENDORSEMENT, EXPORT, R46 + ", proviso on exports"),
          rule(HSN_CODE, HSN_NOT_OMITTABLE, R46 + "(g); Notification 78/2020-CT"),
          rule(DESCRIPTION, ALWAYS, R46 + "(h)"),
          rule(QUANTITY_AND_UNIT, GOODS, R46 + "(i)"),
          rule(TOTAL_VALUE, ALWAYS, R46 + "(j)"),
          rule(TAXABLE_VALUE, ALWAYS, R46 + "(k)"),
          rule(TAX_RATE, ALWAYS, R46 + "(l)"),
          rule(TAX_AMOUNT, ALWAYS, R46 + "(m)"),
          rule(PLACE_OF_SUPPLY, INTER_STATE, R46 + "(n)"),
          rule(DELIVERY_ADDRESS, DELIVERY_ADDRESS_DIFFERS, R46 + "(o)"),
          rule(REVERSE_CHARGE_INDICATOR, ALWAYS, R46 + "(p)"),
          rule(SIGNATURE, NOT_ELECTRONIC_DOCUMENT, R46 + "(q) and proviso on electronic invoices"),
          rule(IRN_QR_CODE, IRN_QR_REQUIRED, R46 + "(r); rule 48(4)"));

  static final List<FieldRule> BILL_OF_SUPPLY =
      List.of(
          rule(SUPPLIER_NAME, ALWAYS, R49 + "(a)"),
          rule(SUPPLIER_ADDRESS, ALWAYS, R49 + "(a)"),
          rule(SUPPLIER_GSTIN, ALWAYS, R49 + "(a)"),
          rule(SERIAL_NUMBER, ALWAYS, R49 + "(b)"),
          rule(ISSUE_DATE, ALWAYS, R49 + "(c)"),
          rule(RECIPIENT_NAME, RECIPIENT_REGISTERED, R49 + "(d)"),
          rule(RECIPIENT_ADDRESS, RECIPIENT_REGISTERED, R49 + "(d)"),
          rule(RECIPIENT_GSTIN_OR_UIN, RECIPIENT_REGISTERED, R49 + "(d)"),
          rule(HSN_CODE, HSN_NOT_OMITTABLE, R49 + "(e); first proviso applies rule 46 provisos"),
          rule(DESCRIPTION, ALWAYS, R49 + "(f)"),
          rule(TOTAL_VALUE, ALWAYS, R49 + "(g)"),
          rule(SIGNATURE, NOT_ELECTRONIC_DOCUMENT, R49 + "(h) and proviso on electronic bills"));

  static final List<FieldRule> CREDIT_OR_DEBIT_NOTE =
      List.of(
          rule(SUPPLIER_NAME, ALWAYS, R53 + "(a)"),
          rule(SUPPLIER_ADDRESS, ALWAYS, R53 + "(a)"),
          rule(SUPPLIER_GSTIN, ALWAYS, R53 + "(a)"),
          rule(NATURE_OF_DOCUMENT, ALWAYS, R53 + "(b)"),
          rule(SERIAL_NUMBER, ALWAYS, R53 + "(c)"),
          rule(ISSUE_DATE, ALWAYS, R53 + "(d)"),
          rule(RECIPIENT_NAME, RECIPIENT_REGISTERED, R53 + "(e)"),
          rule(RECIPIENT_ADDRESS, RECIPIENT_REGISTERED, R53 + "(e)"),
          rule(RECIPIENT_GSTIN_OR_UIN, RECIPIENT_REGISTERED, R53 + "(e)"),
          rule(RECIPIENT_NAME, RECIPIENT_UNREGISTERED, R53 + "(f)"),
          rule(RECIPIENT_ADDRESS, RECIPIENT_UNREGISTERED, R53 + "(f)"),
          rule(DELIVERY_ADDRESS, RECIPIENT_UNREGISTERED, R53 + "(f)"),
          rule(RECIPIENT_STATE_NAME_AND_CODE, RECIPIENT_UNREGISTERED, R53 + "(f)"),
          rule(ORIGINAL_DOCUMENT_REFERENCE, ALWAYS, R53 + "(g)"),
          rule(TAXABLE_VALUE, ALWAYS, R53 + "(h)"),
          rule(TAX_RATE, ALWAYS, R53 + "(h)"),
          rule(TAX_AMOUNT, ALWAYS, R53 + "(h)"),
          rule(SIGNATURE, ALWAYS, R53 + "(i)"));

  static final List<FieldRule> DELIVERY_CHALLAN =
      List.of(
          rule(ISSUE_DATE, ALWAYS, R55 + "(i)"),
          rule(SERIAL_NUMBER, ALWAYS, R55 + "(i)"),
          rule(SUPPLIER_NAME, ALWAYS, R55 + "(ii)"),
          rule(SUPPLIER_ADDRESS, ALWAYS, R55 + "(ii)"),
          rule(SUPPLIER_GSTIN, SUPPLIER_REGISTERED, R55 + "(ii)"),
          rule(RECIPIENT_NAME, ALWAYS, R55 + "(iii)"),
          rule(RECIPIENT_ADDRESS, ALWAYS, R55 + "(iii)"),
          rule(RECIPIENT_GSTIN_OR_UIN, RECIPIENT_REGISTERED, R55 + "(iii)"),
          rule(HSN_CODE, ALWAYS, R55 + "(iv)"),
          rule(DESCRIPTION, ALWAYS, R55 + "(iv)"),
          rule(QUANTITY_AND_UNIT, ALWAYS, R55 + "(v)"),
          rule(TAXABLE_VALUE, ALWAYS, R55 + "(vi)"),
          rule(TAX_RATE, TRANSPORT_FOR_SUPPLY, R55 + "(vii)"),
          rule(TAX_AMOUNT, TRANSPORT_FOR_SUPPLY, R55 + "(vii)"),
          rule(PLACE_OF_SUPPLY, INTER_STATE, R55 + "(viii)"),
          rule(SIGNATURE, ALWAYS, R55 + "(ix)"));

  private DocumentRules() {}

  private static FieldRule rule(DocumentField field, FieldCondition condition, String citation) {
    return new FieldRule(field, condition, citation);
  }
}
