/*
 * File:      PlaceOfSupplyUnitTest.java
 * Module:    india-gst
 * Layer:     test
 * Purpose:   Unit tests for PlaceOfSupply resolution.
 * Owns:      Intra and inter-State decisions and precedence of delivery state.
 * Uses:      PlaceOfSupply, Gstin, StateCode
 * Used by:   Gradle test tasks.
 * Not here:  Production code.
 * Reuse:     Test only.
 * Plan:      L2-03
 */
package io.github.someoneanoncoder.indiagst;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.someoneanoncoder.indiagst.testing.UnitTest;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@UnitTest
class PlaceOfSupplyUnitTest {

  private static final Gstin MH_BUYER = Gstin.parse("27AAPFU0939F1ZV");
  private static final Gstin TN_BUYER = Gstin.parse("33AAACC1206D1ZN");

  @Test
  @DisplayName("same state delivery is intra-State with SGST")
  void shouldBeIntra_whenDeliveryStateEqualsSupplierState() {
    PlaceOfSupply place = PlaceOfSupply.forDelivery(StateCode.MAHARASHTRA, StateCode.MAHARASHTRA);
    assertThat(place.supplyType()).isEqualTo(SupplyType.INTRA_STATE);
    assertThat(place.stateTaxKind()).isEqualTo(StateTaxKind.SGST);
  }

  @Test
  @DisplayName("different state delivery is inter-State with no state tax head")
  void shouldBeInter_whenDeliveryStateDiffers() {
    PlaceOfSupply place = PlaceOfSupply.forDelivery(StateCode.MAHARASHTRA, StateCode.KARNATAKA);
    assertThat(place.supplyType()).isEqualTo(SupplyType.INTER_STATE);
    assertThat(place.stateTaxKind()).isEqualTo(StateTaxKind.NOT_APPLICABLE);
    assertThat(place.placeOfSupplyState()).isEqualTo(StateCode.KARNATAKA);
    assertThat(place.supplierState()).isEqualTo(StateCode.MAHARASHTRA);
  }

  @Test
  @DisplayName("registered buyer decides the place when no delivery state is given")
  void shouldUseBuyerState_whenNoDeliveryState() {
    assertThat(PlaceOfSupply.forRegisteredBuyer(StateCode.MAHARASHTRA, MH_BUYER).supplyType())
        .isEqualTo(SupplyType.INTRA_STATE);
    assertThat(PlaceOfSupply.forRegisteredBuyer(StateCode.MAHARASHTRA, TN_BUYER).supplyType())
        .isEqualTo(SupplyType.INTER_STATE);
  }

  @Test
  @DisplayName("an explicit delivery state wins over the buyer GSTIN state")
  void shouldPreferDelivery_whenBothGiven() {
    PlaceOfSupply place =
        PlaceOfSupply.resolve(
            StateCode.MAHARASHTRA, Optional.of(StateCode.MAHARASHTRA), Optional.of(TN_BUYER));
    assertThat(place.supplyType()).isEqualTo(SupplyType.INTRA_STATE);
  }

  @Test
  @DisplayName("bill-to ship-to: the directing buyer's state is the place of supply")
  void shouldUseBuyerState_whenBuyerDirectsDelivery() {
    PlaceOfSupply place = PlaceOfSupply.forDirectedDelivery(StateCode.MAHARASHTRA, TN_BUYER);
    assertThat(place.placeOfSupplyState()).isEqualTo(StateCode.TAMIL_NADU);
    assertThat(place.supplyType()).isEqualTo(SupplyType.INTER_STATE);
  }

  @Test
  @DisplayName("an unregistered buyer without an address is treated as local")
  void shouldBeIntra_whenNothingElseKnown() {
    PlaceOfSupply place = PlaceOfSupply.resolve(StateCode.GOA, Optional.empty(), Optional.empty());
    assertThat(place.placeOfSupplyState()).isEqualTo(StateCode.GOA);
    assertThat(place.supplyType()).isEqualTo(SupplyType.INTRA_STATE);
  }

  @Test
  @DisplayName("union territory without legislature gives UTGST")
  void shouldGiveUtgst_whenIntraUnionTerritory() {
    PlaceOfSupply place = PlaceOfSupply.forDelivery(StateCode.CHANDIGARH, StateCode.CHANDIGARH);
    assertThat(place.stateTaxKind()).isEqualTo(StateTaxKind.UTGST);
  }

  @Test
  @DisplayName("Other Territory is always inter-State, even for a supplier coded 97")
  void shouldBeInter_whenOtherTerritory() {
    assertThat(PlaceOfSupply.forDelivery(StateCode.DELHI, StateCode.OTHER_TERRITORY).supplyType())
        .isEqualTo(SupplyType.INTER_STATE);
    assertThat(
            PlaceOfSupply.forDelivery(StateCode.OTHER_TERRITORY, StateCode.OTHER_TERRITORY)
                .supplyType())
        .isEqualTo(SupplyType.INTER_STATE);
  }

  @Test
  @DisplayName("legacy Daman and Diu counts as the merged union territory")
  void shouldBeIntra_whenLegacyDamanAndDiuMeetsMergedTerritory() {
    PlaceOfSupply place =
        PlaceOfSupply.forDelivery(
            StateCode.DAMAN_AND_DIU_LEGACY, StateCode.DADRA_AND_NAGAR_HAVELI_AND_DAMAN_AND_DIU);
    assertThat(place.supplyType()).isEqualTo(SupplyType.INTRA_STATE);
  }

  @Test
  @DisplayName("null components are rejected")
  void shouldRejectNull_whenBuildingRecord() {
    assertThatThrownBy(() -> new PlaceOfSupply(null, StateCode.GOA, SupplyType.INTRA_STATE))
        .isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> new PlaceOfSupply(StateCode.GOA, null, SupplyType.INTRA_STATE))
        .isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> new PlaceOfSupply(StateCode.GOA, StateCode.GOA, null))
        .isInstanceOf(NullPointerException.class);
  }
}
