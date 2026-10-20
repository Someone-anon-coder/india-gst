/*
 * File:      IndiaGstArchTest.java
 * Module:    india-gst
 * Layer:     test
 * Purpose:   Architecture rules for the library.
 * Owns:      Only JDK and india-money dependencies, no floating point, no System streams, no cycles.
 * Uses:      Public API
 * Used by:   Gradle test tasks.
 * Not here:  Production code.
 * Reuse:     Test only.
 * Plan:      L2
 */
package io.github.someoneanoncoder.indiagst;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition;
import io.github.someoneanoncoder.indiagst.testing.ArchTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@ArchTest
class IndiaGstArchTest {

  private static final String ROOT = "io.github.someoneanoncoder.indiagst";
  private static final JavaClasses MAIN =
      new ClassFileImporter()
          .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
          .importPackages(ROOT);

  @Test
  @DisplayName("library depends on the JDK and india-money only")
  void shouldDependOnJdkAndMoneyOnly_whenAnalysingMainClasses() {
    classes()
        .should()
        .onlyDependOnClassesThat()
        .resideInAnyPackage(ROOT + "..", "java..", "io.github.someoneanoncoder.indiamoney..")
        .check(MAIN);
  }

  @Test
  @DisplayName("no floating-point fields or method signatures")
  void shouldHaveNoFloatingPoint_whenAnalysingMainClasses() {
    fields()
        .should()
        .notHaveRawType(double.class)
        .andShould()
        .notHaveRawType(float.class)
        .check(MAIN);
    methods()
        .should()
        .notHaveRawReturnType(double.class)
        .andShould()
        .notHaveRawReturnType(float.class)
        .check(MAIN);
  }

  @Test
  @DisplayName("no access to System streams")
  void shouldNotTouchSystem_whenAnalysingMainClasses() {
    noClasses().should().accessClassesThat().haveFullyQualifiedName("java.lang.System").check(MAIN);
  }

  @Test
  @DisplayName("no package cycles")
  void shouldHaveNoCycles_whenAnalysingPackages() {
    SlicesRuleDefinition.slices().matching(ROOT + ".(*)..").should().beFreeOfCycles().check(MAIN);
  }

  @Test
  @DisplayName("only Gstin uses the internal package")
  void shouldKeepInternalPrivate_whenAnalysingDependencies() {
    noClasses()
        .that()
        .resideOutsideOfPackage(ROOT + ".internal..")
        .and()
        .doNotHaveFullyQualifiedName(ROOT + ".Gstin")
        .should()
        .dependOnClassesThat()
        .resideInAPackage(ROOT + ".internal..")
        .check(MAIN);
  }
}
