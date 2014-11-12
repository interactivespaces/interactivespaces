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

package interactivespaces.service.control.opensoundcontrol;

/**
 * An Open Sound Control message that will be sent to a remote Open Sound Control endpoint.
 *
 * @author Keith M. Hughes
 */
public interface OpenSoundControlOutgoingMessage {

  /**
   * Get the OSC address for the message.
   *
   * @return the OSC address
   */
  String getAddress();

  /**
   * Send the message.
   */
  void send();

  /**
   * Write a string into the message.
   *
   * @param value
   *          the string to add
   *
   * @return the message being constructed
   */
  OpenSoundControlOutgoingMessage addString(String value);

  /**
   * Write a int value into the message.
   *
   * @param value
   *          the value to write
   *
   * @return the message being constructed
   */
  OpenSoundControlOutgoingMessage addInt(int value);

  /**
   * Write a long value into the message.
   *
   * @param value
   *          the value to write
   *
   * @return the message being constructed
   */
  OpenSoundControlOutgoingMessage addLong(long value);

  /**
   * Write a float value into the message.
   *
   * @param value
   *          the value to write
   *
   * @return the message being constructed
   */
  OpenSoundControlOutgoingMessage addFloat(float value);

  /**
   * Write a double value into the message.
   *
   * @param value
   *          the value to write
   *
   * @return the message being constructed
   */
  OpenSoundControlOutgoingMessage addDouble(double value);

  /**
   * Write out a blob field.
   *
   * <p>
   * The entire array will be sent.
   *
   * @param blob
   *          the blob data
   *
   * @return the message being constructed
   */
  OpenSoundControlOutgoingMessage addBlob(byte[] blob);

  /**
   * Write out a segment of a blob field.
   *
   * @param blob
   *          the blob data
   * @param start
   *          the beginning offset of data
   * @param length
   *          the number of bytes to send from the blob
   *
   * @return the message being constructed
   */
  OpenSoundControlOutgoingMessage addBlob(byte[] blob, int start, int length);

  /**
   * Get the size of the message so far.
   *
   * @return size of message in bytes
   */
  int getMessageSize();
}
