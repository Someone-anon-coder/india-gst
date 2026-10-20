/*
 * File:      DocumentKind.java
 * Module:    india-gst
 * Layer:     api
 * Purpose:   Names GST document types and answers which fields each one needs.
 * Owns:      The kind list, the checklist accessors and the missing-field check.
 * Uses:      DocumentRules (data), DocumentContext, FieldRule.
 * Used by:   Billing code, print templates, validators.
 * Not here:  The tables (DocumentRules), numbering, tax computation.
 * Reuse:     Enum.
 * Plan:      L2-05
 */
package io.github.someoneanoncoder.indiagst;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * The GST documents a shop issues, each with its field checklist as data.
 *
 * <p>Sources: tax invoice, CGST Rules 2017 rule 46 (and 46A for an invoice-cum-bill of supply,
 * which this library does not model separately); bill of supply, rule 49; credit and debit note,
 * rule 53(1A) (section 34 of the CGST Act, 2017); delivery challan, rule 55(1). The tables follow
 * the CBIC consolidation of 24 September 2021 and ignore later amendments; see {@code
 * DocumentRules}.
 *
 * <p>Interpretation flagged for review: rule 46(d) reads "name, address and GSTIN or UIN, if
 * registered, of the recipient". It is modelled as all three being required for a registered
 * recipient, and rule 46(e)/(f) governing unregistered recipients.
 */
public enum DocumentKind {
  /** Tax invoice under section 31(1) and rule 46. */
  TAX_INVOICE(DocumentRules.TAX_INVOICE),
  /** Bill of supply under section 31(3)(c) and rule 49. */
  BILL_OF_SUPPLY(DocumentRules.BILL_OF_SUPPLY),
  /** Credit note under section 34(1) and rule 53(1A). */
  CREDIT_NOTE(DocumentRules.CREDIT_OR_DEBIT_NOTE),
  /** Debit note under section 34(3) and rule 53(1A). */
  DEBIT_NOTE(DocumentRules.CREDIT_OR_DEBIT_NOTE),
  /** Delivery challan under rule 55. */
  DELIVERY_CHALLAN(DocumentRules.DELIVERY_CHALLAN);

  private final List<FieldRule> rules;

  DocumentKind(List<FieldRule> rules) {
    this.rules = rules;
  }

  /**
   * Returns the full checklist for this kind.
   *
   * @return immutable list of rules, in the order of the rule text
   */
  public List<FieldRule> fieldRules() {
    return rules;
  }

  /**
   * Returns the fields required in the given circumstances.
   *
   * @param context the known facts about the document
   * @return the required fields
   */
  public Set<DocumentField> requiredFields(DocumentContext context) {
    Set<DocumentField> required = EnumSet.noneOf(DocumentField.class);
    for (FieldRule rule : rules) {
      if (context.holds(rule.condition())) {
        required.add(rule.field());
      }
    }
    return required;
  }

  /**
   * Returns the required fields that are not present.
   *
   * @param present the fields the document actually carries
   * @param context the known facts about the document
   * @return fields required but absent; empty if the document is complete
   */
  public Set<DocumentField> missingFields(Set<DocumentField> present, DocumentContext context) {
    Set<DocumentField> missing = requiredFields(context);
    missing.removeAll(present);
    return missing;
  }
}
