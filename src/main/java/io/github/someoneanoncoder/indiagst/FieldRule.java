/*
 * File:      FieldRule.java
 * Module:    india-gst
 * Layer:     api
 * Purpose:   One row of a document's Rule 46-style checklist: a field, when it is required, and its source.
 * Owns:      The data shape of a checklist row.
 * Uses:      DocumentField, FieldCondition.
 * Used by:   DocumentKind, DocumentRules.
 * Not here:  Tables themselves (DocumentRules).
 * Reuse:     Value type.
 * Plan:      L2-05
 */
package io.github.someoneanoncoder.indiagst;

import java.util.Objects;

/**
 * A single requirement: {@code field} is needed on the document when {@code condition} holds.
 *
 * @param field the particular
 * @param condition when it is required
 * @param citation the rule and clause that requires it, for example {@code "CGST Rules 2017, rule
 *     46(b)"}
 */
public record FieldRule(DocumentField field, FieldCondition condition, String citation) {

  /**
   * Validates the components.
   *
   * @param field the particular
   * @param condition when it is required
   * @param citation the rule and clause
   */
  public FieldRule {
    Objects.requireNonNull(field, "field");
    Objects.requireNonNull(condition, "condition");
    Objects.requireNonNull(citation, "citation");
  }
}
