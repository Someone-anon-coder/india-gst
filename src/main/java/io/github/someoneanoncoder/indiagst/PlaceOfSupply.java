/*
 * File:      PlaceOfSupply.java
 * Module:    india-gst
 * Layer:     api
 * Purpose:   Decides whether a supply is intra-State (central plus state tax) or inter-State (integrated tax).
 * Owns:      Resolution from supplier state, delivery state and buyer GSTIN.
 * Uses:      StateCode, Gstin, SupplyType, StateTaxKind.
 * Used by:   TaxCalculator and billing code.
 * Not here:  Services-specific place-of-supply rules (IGST Act sections 12 and 13), exports, imports, SEZ supplies, tax rates.
 * Reuse:     Value type; build with resolve.
 * Plan:      L2-03
 */
package io.github.someoneanoncoder.indiagst;

import java.util.Objects;
import java.util.Optional;

/**
 * The outcome of place-of-supply resolution for a supply of goods within India.
 *
 * <p>Rules applied (secondary reproductions of the IGST Act, 2017; the CBIC text could not be
 * fetched, verify before relying on it):
 *
 * <ul>
 *   <li>Section 10(1)(a): where goods move, the place of supply is where the movement ends, so an
 *       explicit delivery state wins.
 *   <li>Section 10(1)(b) and section 12(2)(a) (services to a registered person): without a delivery
 *       state the place is the state in the buyer's GSTIN.
 *   <li>Section 10(1)(ca) (inserted from 1 October 2023): for an unregistered buyer with no
 *       recorded address the place is the supplier's location, so the supply is intra-State.
 *   <li>Sections 7 and 8: supplier location and place of supply in the same State or union
 *       territory make a supply intra-State; otherwise it is inter-State. Code 97 (Other Territory)
 *       is always treated as inter-State (not confirmed against a primary clause).
 * </ul>
 *
 * <p>Daman and Diu legacy code 25 is treated as code 26 because the two union territories merged in
 * January 2020. Exports, imports, SEZ supplies and most services rules are out of scope.
 *
 * @param supplierState the supplier's location (the state of its GSTIN)
 * @param placeOfSupplyState the resolved place of supply
 * @param supplyType intra-State or inter-State
 */
public record PlaceOfSupply(
    StateCode supplierState, StateCode placeOfSupplyState, SupplyType supplyType) {

  /**
   * Validates the components.
   *
   * @param supplierState the supplier's location
   * @param placeOfSupplyState the place of supply
   * @param supplyType the classification
   */
  public PlaceOfSupply {
    Objects.requireNonNull(supplierState, "supplierState");
    Objects.requireNonNull(placeOfSupplyState, "placeOfSupplyState");
    Objects.requireNonNull(supplyType, "supplyType");
  }

  /**
   * Resolves the place of supply.
   *
   * @param supplierState the supplier's location
   * @param deliveryState the state where the goods are delivered, if known; takes precedence
   * @param buyerGstin the buyer's GSTIN, if the buyer is registered
   * @return the resolution
   */
  public static PlaceOfSupply resolve(
      StateCode supplierState, Optional<StateCode> deliveryState, Optional<Gstin> buyerGstin) {
    Objects.requireNonNull(supplierState, "supplierState");
    StateCode place =
        deliveryState.orElseGet(() -> buyerGstin.map(Gstin::state).orElse(supplierState));
    boolean sameState =
        canonical(supplierState) == canonical(place) && place != StateCode.OTHER_TERRITORY;
    return new PlaceOfSupply(
        supplierState, place, sameState ? SupplyType.INTRA_STATE : SupplyType.INTER_STATE);
  }

  /**
   * Resolves the place of supply for a registered buyer with no separate delivery state.
   *
   * @param supplierState the supplier's location
   * @param buyerGstin the buyer's GSTIN
   * @return the resolution
   */
  public static PlaceOfSupply forRegisteredBuyer(StateCode supplierState, Gstin buyerGstin) {
    return resolve(supplierState, Optional.empty(), Optional.of(buyerGstin));
  }

  /**
   * Resolves the place of supply for goods delivered to a given state.
   *
   * @param supplierState the supplier's location
   * @param deliveryState where the movement of goods ends
   * @return the resolution
   */
  public static PlaceOfSupply forDelivery(StateCode supplierState, StateCode deliveryState) {
    return resolve(supplierState, Optional.of(deliveryState), Optional.empty());
  }

  /**
   * Returns the state-level tax head that pairs with central tax on this supply.
   *
   * @return SGST or UTGST for intra-State supplies; {@link StateTaxKind#NOT_APPLICABLE} otherwise
   */
  public StateTaxKind stateTaxKind() {
    return supplyType == SupplyType.INTRA_STATE
        ? placeOfSupplyState.stateTaxKind()
        : StateTaxKind.NOT_APPLICABLE;
  }

  private static StateCode canonical(StateCode state) {
    return state == StateCode.DAMAN_AND_DIU_LEGACY
        ? StateCode.DADRA_AND_NAGAR_HAVELI_AND_DAMAN_AND_DIU
        : state;
  }
}
