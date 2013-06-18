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

package interactivespaces.service.comm.serial.xbee;

/**
 * The base for all XBee Frame Writers.
 *
 * @author Keith M. Hughes
 */
public interface RequestXBeeFrame {

  /**
   * Add a new byte to the frame.
   *
   * @param b
   *          the byte to add
   */
  void add(int b);

  /**
   * Add a new 16-bit integer to the frame.
   *
   * @param i
   *          the integer to add
   */
  void add16(int i);

  /**
   * Add an array of bytes to the frame.
   *
   * @param ba
   *          the bytes to add
   */
  void add(byte[] ba);

  /**
   * Add an array of bytes to the frame.
   *
   * <p>
   * The bottom 8 bits of each integer is added.
   *
   * @param ia
   *          the bytes to add
   */
  void add(int[] ia);

  /**
   * Write the frame to the XBee.
   *
   * @param commEndpoint
   *          the communication endpoint for the XBee
   */
  void write(XBeeCommunicationEndpoint commEndpoint);

}