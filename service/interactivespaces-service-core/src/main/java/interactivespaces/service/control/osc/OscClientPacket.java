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

package interactivespaces.service.control.osc;

/**
 * An Open Sound Control packet.
 *
 * @author Keith M. Hughes
 */
public interface OscClientPacket {

  /**
   * Get the OSC address for the packet.
   *
   * @return the OSC address
   */
  String getOscAddress();

  /**
   * Write the packet to the remote OSC server.
   */
  void write();

  /**
   * Write a string into the packet.
   *
   * @param value
   *          the string to add
   */
  void writeString(String value);

  /**
   * Write a int value into the packet.
   *
   * @param value
   *          the value to write
   */
  void writeInt(int value);

  /**
   * Write a long value into the packet.
   *
   * @param value
   *          the value to write
   */
  void writeLong(long value);

  /**
   * Write a float value into the packet.
   *
   * @param value
   *          the value to write
   */
  void writeFloat(float value);

  /**
   * Write a double value into the packet.
   *
   * @param value
   *          the value to write
   */
  void writeDouble(double value);

}