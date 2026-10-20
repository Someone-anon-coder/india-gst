/*
 * File:      InvoiceNumberUnitTest.java
 * Module:    india-gst
 * Layer:     test
 * Purpose:   Unit tests for InvoiceNumber.
 * Owns:      Length, characters, padding and uniqueness contract.
 * Uses:      InvoiceNumber, InvoiceNumberRegistry
 * Used by:   Gradle test tasks.
 * Not here:  Production code.
 * Reuse:     Test only.
 * Plan:      L2-07
 */
package io.github.someoneanoncoder.indiagst;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.someoneanoncoder.indiagst.testing.UnitTest;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@UnitTest
class InvoiceNumberUnitTest {

  @Test
  @DisplayName("letters, digits, hyphen and slash up to sixteen characters are valid")
  void shouldAccept_whenCharactersAndLengthAreAllowed() {
    for (String ok :
        new String[] {"1", "INV-001", "A/25-26/000123", "abcDEF/-/-/-/123", "1234567890123456"}) {
      assertThat(InvoiceNumber.isValid(ok)).as(ok).isTrue();
      assertThat(InvoiceNumber.parse(ok).value()).isEqualTo(ok);
    }
  }

  @Test
  @DisplayName("empty, seventeen characters, spaces and other symbols are invalid")
  void shouldReject_whenLengthOrCharacterIsWrong() {
    for (String bad :
        new String[] {
          null, "", "12345678901234567", "A B", "A_1", "A.1", "A\\1", "Ä1", "A\n", "1\u0000"
        }) {
      assertThat(InvoiceNumber.isValid(bad)).as(String.valueOf(bad)).isFalse();
      assertThat(InvoiceNumber.tryParse(bad)).isEmpty();
    }
    assertThatThrownBy(() -> InvoiceNumber.parse("12345678901234567"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("16");
    assertThatThrownBy(() -> InvoiceNumber.parse("A B"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("character");
  }

  @Test
  @DisplayName("of joins prefix and zero-padded sequence and enforces the limit")
  void shouldPad_whenBuildingFromPrefixAndSequence() {
    assertThat(InvoiceNumber.of("A/25-26/", 123, 6)).hasToString("A/25-26/000123");
    assertThat(InvoiceNumber.of("", 7, 1)).hasToString("7");
    assertThat(InvoiceNumber.of("X", 1234567, 3)).hasToString("X1234567");
    assertThatThrownBy(() -> InvoiceNumber.of("A/25-26/", 123, 9))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> InvoiceNumber.of("A", -1, 3))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> InvoiceNumber.of("A", 1, 0))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> InvoiceNumber.of("A", 1, 17))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> InvoiceNumber.of(null, 1, 3)).isInstanceOf(NullPointerException.class);
  }

  @Test
  @DisplayName("requireUnused consults the registry and uniqueness folds case")
  void shouldCheckRegistry_whenRequiringUnused() {
    Set<String> used = new HashSet<>();
    InvoiceNumberRegistry registry =
        (scope, number) -> used.contains(scope + "|" + number.uniquenessKey());
    InvoiceNumber number = InvoiceNumber.parse("a/1");
    assertThatCode(() -> number.requireUnused("27AAPFU0939F1ZV|2025-26", registry))
        .doesNotThrowAnyException();
    used.add("27AAPFU0939F1ZV|2025-26|A/1");
    assertThatThrownBy(() -> number.requireUnused("27AAPFU0939F1ZV|2025-26", registry))
        .isInstanceOf(IllegalStateException.class);
    assertThatCode(() -> number.requireUnused("27AAPFU0939F1ZV|2026-27", registry))
        .doesNotThrowAnyException();
  }

  @Test
  @DisplayName("equality follows the exact text")
  void shouldCompareExactText_whenCheckingEquality() {
    assertThat(InvoiceNumber.parse("A/1"))
        .isEqualTo(InvoiceNumber.parse("A/1"))
        .hasSameHashCodeAs(InvoiceNumber.parse("A/1"));
    assertThat(InvoiceNumber.parse("A/1"))
        .isNotEqualTo(InvoiceNumber.parse("a/1"))
        .isNotEqualTo("A/1");
  }
}
