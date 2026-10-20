/*
 * File:      GstinPropertyTest.java
 * Module:    india-gst
 * Layer:     test
 * Purpose:   Property tests for the GSTIN check character.
 * Owns:      Generated GSTINs always validate; single-character changes are caught.
 * Uses:      Gstin
 * Used by:   Gradle test tasks.
 * Not here:  Production code.
 * Reuse:     Test only.
 * Plan:      L2-01
 */
package io.github.someoneanoncoder.indiagst;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.someoneanoncoder.indiagst.testing.PropertyTest;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;

@PropertyTest
class GstinPropertyTest {

  private static final String ALNUM = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

  @Provide
  Arbitrary<String> prefixes() {
    Arbitrary<String> state =
        Arbitraries.of(StateCode.values())
            .filter(s -> s != StateCode.OTHER_TERRITORY)
            .map(StateCode::code);
    Arbitrary<String> letters = Arbitraries.strings().withCharRange('A', 'Z').ofLength(5);
    Arbitrary<String> digits = Arbitraries.strings().numeric().ofLength(4);
    Arbitrary<String> letter = Arbitraries.strings().withCharRange('A', 'Z').ofLength(1);
    Arbitrary<String> entity =
        Arbitraries.strings().withChars("123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ").ofLength(1);
    return Combinators.combine(state, letters, digits, letter, entity)
        .as((st, l, d, c, e) -> st + l + d + c + e + "Z");
  }

  @Property
  void shouldValidate_whenCheckCharacterIsGenerated(@ForAll("prefixes") String prefix) {
    Gstin gstin = Gstin.generate(prefix);
    assertThat(Gstin.isValid(gstin.value())).isTrue();
    assertThat(gstin.value()).startsWith(prefix);
  }

  @Property
  void shouldDetectSingleCharacterChange_whenOneCharacterOfPayloadIsReplaced(
      @ForAll("prefixes") String prefix,
      @ForAll @net.jqwik.api.constraints.IntRange(min = 2, max = 11) int index,
      @ForAll @net.jqwik.api.constraints.IntRange(min = 1, max = 35) int shift) {
    String original = Gstin.generate(prefix).value();
    char old = original.charAt(index);
    char changed = ALNUM.charAt((ALNUM.indexOf(old) + shift) % 36);
    boolean digitsPosition = index >= 7 && index <= 10;
    boolean letterPosition = index < 7 || index == 11;
    boolean sameClass =
        digitsPosition ? Character.isDigit(changed) : letterPosition && Character.isLetter(changed);
    if (sameClass) {
      String mutated = original.substring(0, index) + changed + original.substring(index + 1);
      assertThat(Gstin.isValid(mutated)).isFalse();
    }
  }
}
