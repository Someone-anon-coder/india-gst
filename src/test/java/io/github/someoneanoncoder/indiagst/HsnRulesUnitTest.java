/*
 * File:      HsnRulesUnitTest.java
 * Module:    india-gst
 * Layer:     test
 * Purpose:   Unit tests for HsnRules, HsnEntry and HsnMaster.
 * Owns:      Digit thresholds, omission proviso and format checks.
 * Uses:      HsnRules, HsnMaster
 * Used by:   Gradle test tasks.
 * Not here:  Production code.
 * Reuse:     Test only.
 * Plan:      L2-06
 */
package io.github.someoneanoncoder.indiagst;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.someoneanoncoder.indiagst.testing.UnitTest;
import io.github.someoneanoncoder.indiamoney.Money;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@UnitTest
class HsnRulesUnitTest {

  @Test
  @DisplayName("four digits up to and including five crore, six digits above")
  void shouldChooseDigits_whenTurnoverCrossesFiveCrore() {
    assertThat(HsnRules.requiredDigits(Money.ZERO)).isEqualTo(4);
    assertThat(HsnRules.requiredDigits(Money.ofRupees(5_00_00_000L))).isEqualTo(4);
    assertThat(HsnRules.requiredDigits(Money.ofPaise(5_00_00_000L * 100 + 1))).isEqualTo(6);
    assertThat(HsnRules.requiredDigits(Money.ofRupees(100_00_00_000L))).isEqualTo(6);
  }

  @Test
  @DisplayName("digits may be omitted only up to five crore and for unregistered recipients")
  void shouldAllowOmission_whenSmallAndB2c() {
    assertThat(HsnRules.mayOmit(Money.ofRupees(1), false)).isTrue();
    assertThat(HsnRules.mayOmit(Money.ofRupees(5_00_00_000L), false)).isTrue();
    assertThat(HsnRules.mayOmit(Money.ofRupees(1), true)).isFalse();
    assertThat(HsnRules.mayOmit(Money.ofRupees(5_00_00_001L), false)).isFalse();
  }

  @Test
  @DisplayName("format accepts 2, 4, 6 and 8 digits only")
  void shouldCheckFormat_whenValidatingCodes() {
    for (String ok : List.of("84", "8471", "847130", "84713010", "998314")) {
      assertThat(HsnRules.isValidFormat(ok)).as(ok).isTrue();
    }
    for (String bad :
        new String[] {
          null, "", "8", "847", "84713", "8471301", "847130100", "84A1", "84 1", "８４７１"
        }) {
      assertThat(HsnRules.isValidFormat(bad)).as(String.valueOf(bad)).isFalse();
    }
  }

  @Test
  @DisplayName("problemOf explains what is wrong")
  void shouldExplain_whenCodeIsUnacceptable() {
    Money small = Money.ofRupees(1_00_00_000L);
    Money large = Money.ofRupees(10_00_00_000L);
    assertThat(HsnRules.problemOf("8471", small, true)).isEmpty();
    assertThat(HsnRules.problemOf("847130", large, true)).isEmpty();
    assertThat(HsnRules.problemOf("8471", large, true))
        .hasValueSatisfying(m -> assertThat(m).contains("6 digits"));
    assertThat(HsnRules.problemOf("84", small, true))
        .hasValueSatisfying(m -> assertThat(m).contains("4 digits"));
    assertThat(HsnRules.problemOf("84X1", small, true))
        .hasValueSatisfying(m -> assertThat(m).contains("digits"));
    assertThat(HsnRules.problemOf("", small, true))
        .hasValueSatisfying(m -> assertThat(m).contains("required"));
    assertThat(HsnRules.problemOf(null, small, false)).isEmpty();
    assertThat(HsnRules.problemOf(null, large, false)).isPresent();
  }

  @Test
  @DisplayName("HsnMaster finds entries and later duplicates win")
  void shouldFindEntries_whenMasterIsBuilt() {
    HsnMaster master =
        HsnMaster.of(
            List.of(
                new HsnEntry("8471", "Computers"),
                new HsnEntry("8471", "Machines"),
                new HsnEntry("9983", "Services")));
    assertThat(master.size()).isEqualTo(2);
    assertThat(master.find("8471")).hasValue(new HsnEntry("8471", "Machines"));
    assertThat(master.find("0000")).isEmpty();
    assertThat(master.find(null)).isEmpty();
    assertThatThrownBy(() -> new HsnEntry("12", null)).isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> new HsnEntry("123", "x")).isInstanceOf(IllegalArgumentException.class);
  }
}
