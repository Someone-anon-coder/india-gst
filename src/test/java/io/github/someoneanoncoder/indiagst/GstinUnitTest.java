/*
 * File:      GstinUnitTest.java
 * Module:    india-gst
 * Layer:     test
 * Purpose:   Unit tests for Gstin parsing, generation and accessors.
 * Owns:      Format, state, PAN and check character cases.
 * Uses:      Gstin
 * Used by:   Gradle test tasks.
 * Not here:  Production code.
 * Reuse:     Test only.
 * Plan:      L2-01
 */
package io.github.someoneanoncoder.indiagst;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.someoneanoncoder.indiagst.testing.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@UnitTest
class GstinUnitTest {

  @Test
  @DisplayName("a published sample GSTIN parses and exposes its parts")
  void shouldExposeParts_whenGstinIsValid() {
    Gstin gstin = Gstin.parse("27AAPFU0939F1ZV");
    assertThat(gstin.state()).isEqualTo(StateCode.MAHARASHTRA);
    assertThat(gstin.pan()).isEqualTo("AAPFU0939F");
    assertThat(gstin.entityNumber()).isEqualTo('1');
    assertThat(gstin.checkCharacter()).isEqualTo('V');
    assertThat(gstin).hasToString("27AAPFU0939F1ZV");
  }

  @Test
  @DisplayName("a second sample from Tamil Nadu parses")
  void shouldParse_whenGstinIsFromTamilNadu() {
    assertThat(Gstin.parse("33AAACC1206D1ZN").state()).isEqualTo(StateCode.TAMIL_NADU);
  }

  @Test
  @DisplayName("spaces and lower case are tolerated and normalised")
  void shouldNormalise_whenTypedLowerCaseWithSpaces() {
    assertThat(Gstin.parse("  27aapfu0939f1zv ").value()).isEqualTo("27AAPFU0939F1ZV");
  }

  @Test
  @DisplayName("a wrong check character is rejected")
  void shouldReject_whenCheckCharacterIsWrong() {
    assertThat(Gstin.isValid("27AAPFU0939F1ZO")).isFalse();
    assertThatThrownBy(() -> Gstin.parse("27AAPFU0939F1ZO"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("check character");
  }

  @Test
  @DisplayName("wrong length, layout, entity number, fixed Z and state are rejected")
  void shouldReject_whenLayoutIsWrong() {
    assertThat(Gstin.tryParse(null)).isEmpty();
    assertThat(Gstin.tryParse("27AAPFU0939F1Z")).isEmpty();
    assertThat(Gstin.tryParse("27AAPFU0939F1ZVV")).isEmpty();
    assertThat(Gstin.tryParse("A7AAPFU0939F1ZV")).isEmpty();
    assertThat(Gstin.tryParse("27AAPFU0939F0ZV")).isEmpty();
    assertThat(Gstin.tryParse("27AAPFU0939F1AV")).isEmpty();
    assertThat(Gstin.tryParse("27AAPF10939F1ZV")).isEmpty();
    assertThat(Gstin.tryParse("27AAPFUA939F1ZV")).isEmpty();
    assertThat(Gstin.tryParse("27AAPFU09391" + "1ZV")).isEmpty();
    assertThat(Gstin.tryParse("00AAPFU0939F1ZV")).isEmpty();
    assertThat(Gstin.tryParse("97AAPFU0939F1ZV")).isEmpty();
    assertThat(Gstin.tryParse("27AAPFU0939F1Z!")).isEmpty();
  }

  @Test
  @DisplayName("generate completes a 14-character prefix with the right check character")
  void shouldAppendCheckCharacter_whenGeneratingFromPrefix() {
    assertThat(Gstin.generate("27AAPFU0939F1Z")).hasToString("27AAPFU0939F1ZV");
    assertThat(Gstin.generate("33aaacc1206d1z")).hasToString("33AAACC1206D1ZN");
    assertThatThrownBy(() -> Gstin.generate("27AAPFU0939F1"))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> Gstin.generate(null)).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> Gstin.generate("27AAPFU0939F1X"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("equal text gives equal GSTINs")
  void shouldBeEqual_whenTextMatches() {
    assertThat(Gstin.parse("27AAPFU0939F1ZV"))
        .isEqualTo(Gstin.parse("27aapfu0939f1zv"))
        .hasSameHashCodeAs(Gstin.parse("27AAPFU0939F1ZV"))
        .isNotEqualTo(Gstin.parse("33AAACC1206D1ZN"))
        .isNotEqualTo("27AAPFU0939F1ZV");
  }
}
