/*
 * File:      GstinSecurityTest.java
 * Module:    india-gst
 * Layer:     test
 * Purpose:   Security tests: GSTIN text is untrusted input.
 * Owns:      Hostile strings never throw anything but IllegalArgumentException and never hang.
 * Uses:      Gstin, InvoiceNumber
 * Used by:   Gradle test tasks.
 * Not here:  Production code.
 * Reuse:     Test only.
 * Plan:      L2-01
 */
package io.github.someoneanoncoder.indiagst;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import io.github.someoneanoncoder.indiagst.testing.SecurityTest;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

@SecurityTest
class GstinSecurityTest {

  private static final String[] HOSTILE = {
    "",
    " ",
    "\u0000",
    "27AAPFU0939F1Z\u0000",
    "27AAPFU0939F1ZV\n27AAPFU0939F1ZV",
    "٢٧AAPFU0939F1ZV",
    "27ÀAPFU0939F1ZV",
    "27AAPFU0939F1ZV".repeat(1000),
    "A".repeat(10_000_000),
    "%s%n",
    "${jndi:ldap://x}",
    "<script>",
    "'; DROP TABLE party;--",
    "27AAPFU0939F1ZV‮",
    "\uD800",
  };

  @Test
  @DisplayName("hostile strings are rejected without any other exception")
  @Timeout(value = 5)
  void shouldRejectCleanly_whenGstinIsHostile() {
    for (String text : HOSTILE) {
      assertThatCode(() -> assertThat(Gstin.isValid(text)).isFalse()).doesNotThrowAnyException();
      assertThat(Gstin.tryParse(text)).isEmpty();
    }
  }

  @Test
  @DisplayName("a ten-million-character input is rejected quickly")
  void shouldRejectFast_whenInputIsHuge() {
    org.junit.jupiter.api.Assertions.assertTimeout(
        Duration.ofSeconds(2), () -> Gstin.isValid("9".repeat(10_000_000)));
  }

  @Test
  @DisplayName("invoice numbers reject hostile text too")
  void shouldRejectCleanly_whenInvoiceNumberIsHostile() {
    for (String text : HOSTILE) {
      assertThat(InvoiceNumber.isValid(text)).isFalse();
    }
    assertThat(InvoiceNumber.isValid("A/25-26/000123\n")).isFalse();
    assertThat(InvoiceNumber.isValid("A/25-26/000123")).isTrue();
  }
}
