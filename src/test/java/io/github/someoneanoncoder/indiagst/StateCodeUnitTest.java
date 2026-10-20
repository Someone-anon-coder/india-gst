/*
 * File:      StateCodeUnitTest.java
 * Module:    india-gst
 * Layer:     test
 * Purpose:   Unit tests for the StateCode table.
 * Owns:      Codes, uniqueness, tax head kind, legacy and special-category flags.
 * Uses:      StateCode
 * Used by:   Gradle test tasks.
 * Not here:  Production code.
 * Reuse:     Test only.
 * Plan:      L2-02
 */
package io.github.someoneanoncoder.indiagst;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.someoneanoncoder.indiagst.testing.UnitTest;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@UnitTest
class StateCodeUnitTest {

  @Test
  @DisplayName("codes are unique two-digit strings and round-trip through fromCode")
  void shouldRoundTrip_whenLookingUpEveryCode() {
    assertThat(Arrays.stream(StateCode.values()).map(StateCode::code).collect(Collectors.toSet()))
        .hasSize(StateCode.values().length);
    for (StateCode state : StateCode.values()) {
      assertThat(state.code()).matches("\\d{2}");
      assertThat(StateCode.fromCode(state.code())).contains(state);
      assertThat(state.displayName()).isNotBlank();
    }
  }

  @Test
  @DisplayName("well-known codes map to the right names")
  void shouldMapKnownCodes_whenLookingUp() {
    assertThat(StateCode.fromCode("27").orElseThrow().displayName()).isEqualTo("Maharashtra");
    assertThat(StateCode.fromCode("07").orElseThrow()).isEqualTo(StateCode.DELHI);
    assertThat(StateCode.fromCode("37").orElseThrow()).isEqualTo(StateCode.ANDHRA_PRADESH);
    assertThat(StateCode.fromCode("38").orElseThrow()).isEqualTo(StateCode.LADAKH);
    assertThat(StateCode.fromCode("97").orElseThrow()).isEqualTo(StateCode.OTHER_TERRITORY);
  }

  @Test
  @DisplayName("unknown, malformed and null codes give empty")
  void shouldReturnEmpty_whenCodeIsUnknown() {
    assertThat(StateCode.fromCode("00")).isEmpty();
    assertThat(StateCode.fromCode("39")).isEmpty();
    assertThat(StateCode.fromCode("96")).isEmpty();
    assertThat(StateCode.fromCode("99")).isEmpty();
    assertThat(StateCode.fromCode("7")).isEmpty();
    assertThat(StateCode.fromCode("")).isEmpty();
    assertThat(StateCode.fromCode(null)).isEmpty();
  }

  @Test
  @DisplayName(
      "union territories without a legislature levy UTGST; States and legislature UTs levy SGST")
  void shouldChooseTaxHead_whenKindDiffers() {
    assertThat(StateCode.CHANDIGARH.stateTaxKind()).isEqualTo(StateTaxKind.UTGST);
    assertThat(StateCode.LADAKH.stateTaxKind()).isEqualTo(StateTaxKind.UTGST);
    assertThat(StateCode.LAKSHADWEEP.stateTaxKind()).isEqualTo(StateTaxKind.UTGST);
    assertThat(StateCode.ANDAMAN_AND_NICOBAR_ISLANDS.stateTaxKind()).isEqualTo(StateTaxKind.UTGST);
    assertThat(StateCode.DADRA_AND_NAGAR_HAVELI_AND_DAMAN_AND_DIU.stateTaxKind())
        .isEqualTo(StateTaxKind.UTGST);
    assertThat(StateCode.DELHI.stateTaxKind()).isEqualTo(StateTaxKind.SGST);
    assertThat(StateCode.PUDUCHERRY.stateTaxKind()).isEqualTo(StateTaxKind.SGST);
    assertThat(StateCode.JAMMU_AND_KASHMIR.stateTaxKind()).isEqualTo(StateTaxKind.SGST);
    assertThat(StateCode.GOA.stateTaxKind()).isEqualTo(StateTaxKind.SGST);
    assertThat(StateCode.OTHER_TERRITORY.stateTaxKind()).isEqualTo(StateTaxKind.NOT_APPLICABLE);
  }

  @Test
  @DisplayName("union territory, legacy and special-category flags")
  void shouldFlagCategories_whenAskingAboutState() {
    assertThat(StateCode.DELHI.isUnionTerritory()).isTrue();
    assertThat(StateCode.CHANDIGARH.isUnionTerritory()).isTrue();
    assertThat(StateCode.GOA.isUnionTerritory()).isFalse();
    assertThat(StateCode.OTHER_TERRITORY.isUnionTerritory()).isFalse();
    assertThat(StateCode.DAMAN_AND_DIU_LEGACY.isLegacy()).isTrue();
    assertThat(StateCode.ANDHRA_PRADESH_LEGACY.isLegacy()).isTrue();
    assertThat(StateCode.ANDHRA_PRADESH.isLegacy()).isFalse();
    assertThat(StateCode.MANIPUR.isSpecialCategory()).isTrue();
    assertThat(StateCode.MAHARASHTRA.isSpecialCategory()).isFalse();
    assertThat(StateCode.MAHARASHTRA.kind()).isEqualTo(StateCode.Kind.STATE);
  }
}
