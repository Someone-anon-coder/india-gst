/*
 * File:      DocumentContext.java
 * Module:    india-gst
 * Layer:     api
 * Purpose:   Carries the facts needed to decide which conditional fields apply.
 * Owns:      The set of facts and the supply value.
 * Uses:      Money, DocumentFact, FieldCondition.
 * Used by:   DocumentKind.
 * Not here:  The field tables (DocumentRules).
 * Reuse:     Value type.
 * Plan:      L2-05
 */
package io.github.someoneanoncoder.indiagst;

import io.github.someoneanoncoder.indiamoney.Money;
import java.util.EnumSet;
import java.util.Set;

/**
 * What is known about a document when its field checklist is evaluated.
 *
 * @param facts the true facts; copied
 * @param supplyValue value of the taxable supply, used for the ₹50,000 threshold in Rule 46(e)
 */
public record DocumentContext(Set<DocumentFact> facts, Money supplyValue) {

  private static final Money FIFTY_THOUSAND = Money.ofRupees(50_000);

  /**
   * Copies the facts into an immutable set.
   *
   * @param facts the true facts
   * @param supplyValue value of the taxable supply
   */
  public DocumentContext {
    facts = facts.isEmpty() ? Set.of() : Set.copyOf(EnumSet.copyOf(facts));
  }

  /**
   * Creates a context.
   *
   * @param supplyValue value of the taxable supply
   * @param facts the facts that are true
   * @return the context
   */
  public static DocumentContext of(Money supplyValue, DocumentFact... facts) {
    Set<DocumentFact> set = EnumSet.noneOf(DocumentFact.class);
    set.addAll(java.util.List.of(facts));
    return new DocumentContext(set, supplyValue);
  }

  /**
   * Tells whether a field condition holds.
   *
   * @param condition the condition from a field rule
   * @return {@code true} if it holds for this document
   */
  public boolean holds(FieldCondition condition) {
    boolean registered = facts.contains(DocumentFact.RECIPIENT_REGISTERED);
    return switch (condition) {
      case ALWAYS -> true;
      case RECIPIENT_REGISTERED -> registered;
      case RECIPIENT_UNREGISTERED -> !registered;
      case UNREGISTERED_DETAILS_NEEDED ->
          !registered
              && (supplyValue.compareTo(FIFTY_THOUSAND) >= 0
                  || facts.contains(DocumentFact.RECIPIENT_REQUESTED_DETAILS));
      case INTER_STATE -> facts.contains(DocumentFact.INTER_STATE);
      case DELIVERY_ADDRESS_DIFFERS -> facts.contains(DocumentFact.DELIVERY_ADDRESS_DIFFERS);
      case HSN_NOT_OMITTABLE -> !facts.contains(DocumentFact.HSN_MAY_BE_OMITTED);
      case NOT_ELECTRONIC_DOCUMENT -> !facts.contains(DocumentFact.ELECTRONIC_DOCUMENT);
      case IRN_QR_REQUIRED -> facts.contains(DocumentFact.IRN_QR_REQUIRED);
      case EXPORT -> facts.contains(DocumentFact.EXPORT);
      case GOODS -> facts.contains(DocumentFact.GOODS);
      case SUPPLIER_REGISTERED -> facts.contains(DocumentFact.SUPPLIER_REGISTERED);
      case TRANSPORT_FOR_SUPPLY -> facts.contains(DocumentFact.TRANSPORT_FOR_SUPPLY);
    };
  }
}
