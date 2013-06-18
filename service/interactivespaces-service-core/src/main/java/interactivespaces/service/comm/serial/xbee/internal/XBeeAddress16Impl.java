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

package interactivespaces.service.comm.serial.xbee.internal;

import interactivespaces.service.comm.serial.xbee.RequestXBeeFrame;
import interactivespaces.service.comm.serial.xbee.XBeeAddress16;
import interactivespaces.util.ByteUtils;

import java.util.Arrays;

/**
 * 16 bit address for an XBee
 *
 * @author Keith M. Hughes
 */
public class XBeeAddress16Impl implements XBeeAddress16 {

  /**
   * The magic address for the either broadcase or to unknown radio.
   */
  public static final XBeeAddress16 BROADCAST_ADDRESS = new XBeeAddress16Impl(0xff, 0xfe);

  /**
   * The address for the xbee.
   */
  private int[] address;

  /**
   * Construct an XBee 16 address using the individual bytes.
   *
   * <p>
   * First is the most significant byte
   *
   * @param a1
   *          high order portion of address
   * @param a2
   *          low order portion of address
   */
  public XBeeAddress16Impl(int a1, int a2) {
    address = new int[] { a1, a2 };
  }

  @Override
  public void write(RequestXBeeFrame frameWriter) {
    frameWriter.add(address);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(address);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    XBeeAddress16Impl other = (XBeeAddress16Impl) obj;
    if (!Arrays.equals(address, other.address))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "XBeeAddress16Impl [address=" + ByteUtils.toHexString(address) + "]";
  }
}
