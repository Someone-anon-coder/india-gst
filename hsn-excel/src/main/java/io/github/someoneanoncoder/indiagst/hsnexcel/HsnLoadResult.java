/*
 * File:      HsnLoadResult.java
 * Module:    india-gst-hsn-excel
 * Layer:     api
 * Purpose:   Carries a loaded HsnMaster and the number of rows that were skipped.
 * Owns:      The result shape.
 * Uses:      HsnMaster.
 * Used by:   HsnExcelLoader and callers.
 * Not here:  Loading.
 * Reuse:     Value type.
 * Plan:      L2-06
 */
package io.github.someoneanoncoder.indiagst.hsnexcel;

import io.github.someoneanoncoder.indiagst.HsnMaster;

/**
 * The outcome of loading a master file.
 *
 * @param master the loaded entries
 * @param skippedRows non-blank rows after the header that did not hold a valid code
 */
public record HsnLoadResult(HsnMaster master, int skippedRows) {}
