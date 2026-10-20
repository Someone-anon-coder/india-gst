/*
 * File:      build.gradle.kts
 * Module:    india-gst-hsn-excel
 * Layer:     build
 * Purpose:   Builds, tests, formats, measures and publishes the india-gst optional HSN/SAC Excel master loader.
 * Owns:      Java 25 toolchain, test category tasks, Spotless, Checkstyle, JaCoCo, PIT, maven-publish.
 * Uses:      gradle/libs.versions.toml, ../config/checkstyle.
 * Used by:   CI workflows and developers.
 * Not here:  Library code.
 * Reuse:     Library template, CLAUDE.md §12.
 * Plan:      L2-06
 */

plugins {
    `java-library`
    `maven-publish`
    checkstyle
    jacoco
    alias(libs.plugins.spotless)
    alias(libs.plugins.pitest)
}

group = "io.github.someone-anon-coder"
// Release builds pass -PreleaseVersion=x.y.z (release workflow) or set VERSION (JitPack).
version = (findProperty("releaseVersion") as String?) ?: System.getenv("VERSION") ?: "0.1.0-SNAPSHOT"

java {
    toolchain { languageVersion = JavaLanguageVersion.of(25) }
    withSourcesJar()
    withJavadocJar()
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release = 25
    options.compilerArgs.addAll(listOf("-Xlint:all", "-Werror", "-parameters", "-Xpkginfo:always"))
}

tasks.withType<Javadoc>().configureEach {
    (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:all", "-quiet")
}

dependencies {
    api(project(":"))
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj.core)
    testImplementation(libs.jqwik)
    testImplementation(libs.archunit.junit5)
    testRuntimeOnly(libs.junit.platform.launcher)
}

// Tag names match the meta-annotations in src/test/java/.../testing (CLAUDE.md §8.2).
val categories = mapOf(
    "Unit" to "unit", "Property" to "property", "Golden" to "golden",
    "Arch" to "arch", "Security" to "security",
)

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

categories.forEach { (name, tag) ->
    tasks.register<Test>("test$name") {
        group = "verification"
        description = "Runs only tests tagged '$tag'."
        testClassesDirs = sourceSets.test.get().output.classesDirs
        classpath = sourceSets.test.get().runtimeClasspath
        failOnNoDiscoveredTests = false
        useJUnitPlatform { includeTags(tag) }
    }
}

spotless {
    java {
        target("src/**/*.java")
        googleJavaFormat(libs.versions.google.java.format.get())
        removeUnusedImports()
    }
}

checkstyle {
    toolVersion = libs.versions.checkstyle.get()
    configDirectory = rootProject.layout.projectDirectory.dir("config/checkstyle")
    maxWarnings = 0
}
// Header and Javadoc rules apply to production code only.
tasks.named("checkstyleTest") { enabled = false }

jacoco { toolVersion = "0.8.14" }
tasks.jacocoTestReport { reports { xml.required = true } }
tasks.jacocoTestCoverageVerification {
    violationRules { rule { limit { counter = "LINE"; minimum = "0.90".toBigDecimal() } } }
}
tasks.check { dependsOn(tasks.jacocoTestCoverageVerification) }

pitest {
    junit5PluginVersion = libs.versions.pitest.junit5.get()
    targetClasses = listOf("io.github.someoneanoncoder.indiagst.hsnexcel.*")
    mutationThreshold = 70
    outputFormats = listOf("HTML", "XML")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            pom {
                name = "india-gst-hsn-excel"
                description = "Optional loader for the official HSN/SAC Excel master, built on the JDK only."
                url = "https://github.com/Someone-anon-coder/india-gst"
                licenses { license { name = "MIT"; url = "https://opensource.org/license/mit" } }
                scm { url = "https://github.com/Someone-anon-coder/india-gst" }
            }
        }
    }
}
