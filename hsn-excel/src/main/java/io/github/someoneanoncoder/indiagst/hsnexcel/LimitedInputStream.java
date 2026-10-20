/*
 * File:      LimitedInputStream.java
 * Module:    india-gst-hsn-excel
 * Layer:     io
 * Purpose:   Stops reading after a byte limit so a zip bomb cannot exhaust memory.
 * Owns:      The decompressed-size guard.
 * Uses:      java.io.
 * Used by:   XlsxSheetReader.
 * Not here:  Parsing.
 * Reuse:     Package-private.
 * Plan:      L2-06
 */
package io.github.someoneanoncoder.indiagst.hsnexcel;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/** Fails with {@link IOException} when more than the allowed bytes are read. */
final class LimitedInputStream extends FilterInputStream {

  private long remaining;

  LimitedInputStream(InputStream in, long limit) {
    super(in);
    this.remaining = limit;
  }

  @Override
  public int read() throws IOException {
    int b = super.read();
    if (b >= 0) {
      consume(1);
    }
    return b;
  }

  @Override
  public int read(byte[] buffer, int offset, int length) throws IOException {
    int n = super.read(buffer, offset, length);
    if (n > 0) {
      consume(n);
    }
    return n;
  }

  private void consume(long n) throws IOException {
    remaining -= n;
    if (remaining < 0) {
      throw new IOException("decompressed size limit exceeded");
    }
  }
}
