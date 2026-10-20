/*
 * File:      settings.gradle.kts
 * Module:    india-gst
 * Layer:     build
 * Purpose:   Declares the core build plus the optional HSN Excel loader build and the version catalog location.
 * Owns:      Root project name and the included hsn-excel module.
 * Uses:      gradle/libs.versions.toml.
 * Used by:   Gradle.
 * Not here:  Dependency versions (catalog) and build logic (build.gradle.kts).
 * Reuse:     Library template, CLAUDE.md §12.
 * Plan:      L2
 */

rootProject.name = "india-gst"

// india-money is published on JitPack only; restrict that repository to its group.
dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven("https://jitpack.io") { content { includeGroup("com.github.Someone-anon-coder") } }
    }
}

include("hsn-excel")
