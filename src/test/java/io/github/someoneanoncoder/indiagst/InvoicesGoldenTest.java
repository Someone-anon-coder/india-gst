/*
 * File:      InvoicesGoldenTest.java
 * Module:    india-gst
 * Layer:     test
 * Purpose:   Golden test: 50 hand-checked worked invoices.
 * Owns:      Parsing of invoices.txt and comparison with TaxCalculator, PlaceOfSupply and RoundOff.
 * Uses:      TaxCalculator, PlaceOfSupply, RoundOff
 * Used by:   Gradle test tasks.
 * Not here:  Production code.
 * Reuse:     Test only.
 * Plan:      L2-08
 */
package io.github.someoneanoncoder.indiagst;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.someoneanoncoder.indiagst.testing.GoldenTest;
import io.github.someoneanoncoder.indiamoney.Money;
import io.github.someoneanoncoder.indiamoney.RoundOff;
import io.github.someoneanoncoder.indiamoney.RoundOffMode;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@GoldenTest
class InvoicesGoldenTest {

  private static final String FILE =
      "/io/github/someoneanoncoder/indiagst/golden/Invoices/invoices.txt";

  private static final class Case {
    final String id;
    final String title;
    String supplier;
    String delivery;
    String buyer;
    boolean directed;
    String mode;
    final List<String[]> lines = new ArrayList<>();
    final List<String[]> expectedLines = new ArrayList<>();
    final List<String[]> expectedSummary = new ArrayList<>();
    String type;
    String stateTax;
    String[] total;
    String[] roundOff;

    Case(String id, String title) {
      this.id = id;
      this.title = title;
    }
  }

  private static List<Case> cases() throws IOException {
    try (InputStream in = InvoicesGoldenTest.class.getResourceAsStream(FILE)) {
      assertThat(in).as("golden file").isNotNull();
      List<Case> cases = new ArrayList<>();
      Case current = null;
      for (String text : new String(in.readAllBytes(), StandardCharsets.UTF_8).split("\n")) {
        if (text.isBlank() || text.startsWith("#")) {
          continue;
        }
        String[] f = text.split("\\|", -1);
        if (f[0].equals("case")) {
          current = new Case(f[1], f[2]);
          cases.add(current);
        } else if (current != null) {
          apply(current, f);
        }
      }
      return cases;
    }
  }

  private static void apply(Case c, String[] f) {
    switch (f[0]) {
      case "supplier" -> c.supplier = f[1];
      case "delivery" -> c.delivery = f[1];
      case "buyer" -> c.buyer = f[1];
      case "directed" -> c.directed = Boolean.parseBoolean(f[1]);
      case "mode" -> c.mode = f[1];
      case "line" -> c.lines.add(f);
      case "expect-type" -> c.type = f[1];
      case "expect-state-tax" -> c.stateTax = f[1];
      case "expect-line" -> c.expectedLines.add(f);
      case "expect-summary" -> c.expectedSummary.add(f);
      case "expect-total" -> c.total = f;
      case "roundoff" -> c.roundOff = f;
      case "end" -> {}
      default -> throw new IllegalStateException("unknown record " + f[0]);
    }
  }

  private static Money money(String rupees) {
    return Money.ofRupees(new BigDecimal(rupees), RoundingMode.UNNECESSARY);
  }

  private static TaxResult run(Case c) {
    StateCode supplier = StateCode.fromCode(c.supplier).orElseThrow();
    Optional<StateCode> delivery = Optional.ofNullable(c.delivery).flatMap(StateCode::fromCode);
    Optional<Gstin> buyer = Optional.ofNullable(c.buyer).map(Gstin::parse);
    PlaceOfSupply place =
        c.directed
            ? PlaceOfSupply.forDirectedDelivery(supplier, buyer.orElseThrow())
            : PlaceOfSupply.resolve(supplier, delivery, buyer);
    List<TaxLineInput> inputs = new ArrayList<>();
    for (String[] l : c.lines) {
      inputs.add(
          new TaxLineInput(money(l[1]), money(l[2]), new BigDecimal(l[3]), new BigDecimal(l[4])));
    }
    return TaxCalculator.calculate(place, PricingMode.valueOf(c.mode), inputs);
  }

  private static void assertHeads(String label, Money[] actual, String[] expected, int from) {
    for (int i = 0; i < actual.length; i++) {
      assertThat(actual[i]).as("%s head %d", label, i).isEqualTo(money(expected[from + i]));
    }
  }

  @Test
  @DisplayName("the golden file holds 50 uniquely numbered cases")
  void shouldHoldFiftyCases_whenFileIsLoaded() throws IOException {
    List<Case> cases = cases();
    assertThat(cases).hasSize(50);
    assertThat(cases.stream().map(c -> c.id).distinct()).hasSize(50);
    assertThat(cases.stream().filter(c -> c.mode.equals("INCLUSIVE")).count())
        .isGreaterThanOrEqualTo(10);
    assertThat(cases.stream().filter(c -> c.type.equals("INTER_STATE")).count())
        .isGreaterThanOrEqualTo(8);
    assertThat(cases.stream().filter(c -> c.roundOff != null).count()).isGreaterThanOrEqualTo(4);
  }

  @Test
  @DisplayName("every worked invoice matches the hand-checked figures")
  void shouldMatchHandCheckedFigures_whenCalculatingEachInvoice() throws IOException {
    for (Case c : cases()) {
      TaxResult r = run(c);
      String id = "case " + c.id + " " + c.title;
      assertThat(r.placeOfSupply().supplyType().name()).as(id).isEqualTo(c.type);
      assertThat(r.placeOfSupply().stateTaxKind().name()).as(id).isEqualTo(c.stateTax);
      assertThat(r.lines()).as(id).hasSameSizeAs(c.expectedLines);
      for (int i = 0; i < c.expectedLines.size(); i++) {
        TaxLine l = r.lines().get(i);
        assertHeads(
            id + " line " + (i + 1),
            new Money[] {
              l.taxableValue(), l.centralTax(), l.stateTax(), l.integratedTax(), l.cess()
            },
            c.expectedLines.get(i),
            1);
      }
      assertThat(r.summary()).as(id).hasSameSizeAs(c.expectedSummary);
      for (int i = 0; i < c.expectedSummary.size(); i++) {
        TaxRateSummary s = r.summary().get(i);
        String[] e = c.expectedSummary.get(i);
        assertThat(s.gstRate()).as(id).isEqualByComparingTo(e[1]);
        assertThat(s.cessRate()).as(id).isEqualByComparingTo(e[2]);
        assertHeads(
            id + " summary " + (i + 1),
            new Money[] {
              s.taxableValue(), s.centralTax(), s.stateTax(), s.integratedTax(), s.cess()
            },
            e,
            3);
      }
      assertHeads(
          id + " total",
          new Money[] {
            r.taxableTotal(),
            r.centralTaxTotal(),
            r.stateTaxTotal(),
            r.integratedTaxTotal(),
            r.cessTotal(),
            r.grandTotal()
          },
          c.total,
          1);
      if (c.roundOff != null) {
        RoundOff ro = RoundOff.of(r.grandTotal(), RoundOffMode.valueOf(c.roundOff[1]));
        assertThat(ro.rounded()).as(id).isEqualTo(money(c.roundOff[2]));
        assertThat(ro.adjustment()).as(id).isEqualTo(money(c.roundOff[3]));
      }
    }
  }
}
