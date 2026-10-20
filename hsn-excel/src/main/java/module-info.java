/*
 * File:      module-info.java
 * Module:    india-gst-hsn-excel
 * Layer:     api
 * Purpose:   JPMS descriptor for the optional HSN/SAC Excel loader.
 * Owns:      Exported package and the dependency on india-gst and the JDK XML API.
 * Uses:      india-gst, java.xml.
 * Used by:   Consumers that import the official HSN master.
 * Not here:  Any third-party dependency.
 * Reuse:     Keep exports minimal.
 * Plan:      L2-06
 */

/** Loads an HSN/SAC master from an .xlsx file using only the JDK. */
module io.github.someoneanoncoder.indiagst.hsnexcel {
  requires transitive io.github.someoneanoncoder.indiagst;
  requires java.xml;

  exports io.github.someoneanoncoder.indiagst.hsnexcel;
}
