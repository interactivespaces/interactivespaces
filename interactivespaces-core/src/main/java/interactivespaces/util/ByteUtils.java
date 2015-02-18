/*
 * Copyright (C) 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package interactivespaces.util;

/**
 * A collection of utilities for working with bytes.
 *
 * @author Keith M. Hughes
 */
public class ByteUtils {

  /**
   * Get the hex string for the bytes.
   *
   * <p>
   * Only the lower 8 bits of each int is used for the byte.
   *
   * <p>
   * The bytes are assumed to be most significant byte first.
   *
   * @param ia
   *          the array of "bytes"
   *
   * @return hex string for the bytes
   */
  public static String toHexString(int[] ia) {
    return toHexString(ia, 0, ia.length, 0);
  }

  /**
   * Get the hex string for the bytes.
   *
   * <p>
   * Only the lower 8 bits of each int is used for the byte.
   *
   * <p>
   * The bytes are assumed to be most significant byte first.
   *
   * @param ia
   *          the array of "bytes"
   * @param offset
   *          where in the array to begin
   * @param length
   *          the number of bytes to use from the array
   * @param width
   *          the number of bytes to be displayed per line, {@code 0} if no
   *          width limit
   *
   * @return hex string for the bytes
   */
  public static String toHexString(int[] ia, int offset, int length, int width) {
    StringBuilder builder = new StringBuilder();

    int outputCount = 0;
    for (int i = 0; i < length; i++) {
      int b = ia[offset + i];

      if (width != 0 && outputCount % width == 0) {
        builder.append("\n");
      } else {
        builder.append(" ");
      }

      String istr = Integer.toHexString(b & 0xff);
      if (istr.length() < 2) {
        builder.append('0');
      }
      builder.append(istr);

      outputCount++;
    }

    // Skip the first spacer character.
    return builder.substring(1);
  }

  /**
   * Get the hex string for the bytes.
   *
   * <p>
   * The bytes are assumed to be most significant byte first.
   *
   * @param ba
   *          the array of bytes
   *
   * @return hex string for the bytes
   */
  public static String toHexString(byte[] ba) {
    return toHexString(ba, 0, ba.length, 0);
  }

  /**
   * Get the hex string for the bytes.
   *
   * <p>
   * The bytes are assumed to be most significant byte first.
   *
   * @param ba
   *          the array of bytes
   * @param offset
   *          where in the array to begin
   * @param length
   *          the number of bytes to use from the array
   * @param width
   *          the number of bytes to be displayed per line, {@code 0} if no
   *          width limit
   *
   * @return hex string for the bytes
   */
  public static String toHexString(byte[] ba, int offset, int length, int width) {
    StringBuilder builder = new StringBuilder();

    int outputCount = 0;
    for (int i = 0; i < length; i++) {
      int b = ba[offset + i];

      if (width != 0 && outputCount % width == 0) {
        builder.append("\n");
      } else {
        builder.append(" ");
      }
      String bstr = Integer.toHexString(b & 0xff);
      if (bstr.length() < 2) {
        builder.append('0');
      }
      builder.append(bstr);

      outputCount++;
    }

    // Skip the first spacer character.
    return builder.substring(1);
  }
}
