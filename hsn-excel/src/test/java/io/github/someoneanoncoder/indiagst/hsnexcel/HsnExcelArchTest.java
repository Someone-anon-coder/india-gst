/*
 * File:      HsnExcelArchTest.java
 * Module:    india-gst-hsn-excel
 * Layer:     test
 * Purpose:   Architecture rules for the HSN Excel module.
 * Owns:      JDK and india-gst dependencies only, no System streams.
 * Uses:      Main classes
 * Used by:   Gradle test tasks.
 * Not here:  Production code.
 * Reuse:     Test only.
 * Plan:      L2-06
 */
package io.github.someoneanoncoder.indiagst.hsnexcel;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import io.github.someoneanoncoder.indiagst.hsnexcel.testing.ArchTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@ArchTest
class HsnExcelArchTest {

  private static final JavaClasses MAIN =
      new ClassFileImporter()
          .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
          .importPackages("io.github.someoneanoncoder.indiagst.hsnexcel");

  @Test
  @DisplayName("module depends on the JDK and india-gst only")
  void shouldDependOnJdkAndCoreOnly_whenAnalysingMainClasses() {
    classes()
        .should()
        .onlyDependOnClassesThat()
        .resideInAnyPackage(
            "io.github.someoneanoncoder.indiagst..",
            "io.github.someoneanoncoder.indiamoney..",
            "java..",
            "javax.xml..")
        .check(MAIN);
  }

  @Test
  @DisplayName("no access to System streams")
  void shouldNotTouchSystem_whenAnalysingMainClasses() {
    noClasses().should().accessClassesThat().haveFullyQualifiedName("java.lang.System").check(MAIN);
  }
}
