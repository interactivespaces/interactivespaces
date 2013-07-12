/*
 * Copyright (C) 2012 Google Inc.
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

package interactivespaces.service.comm.serial;

import interactivespaces.comm.CommunicationEndpoint;

/**
 * An endpoint for serial communication.
 *
 * @author Keith M. Hughes
 */
public interface SerialCommunicationEndpoint extends CommunicationEndpoint {

  /**
   * How many bytes are available on the input stream?
   *
   * @return the number of bytes available for reading
   */
  int available();

  /**
   * Read a single byte
   *
   * @return the byte
   */
  int read();

  /**
   * Read bytes into the buffer
   *
   * @param buffer
   *          the buffer to read the bytes into
   *
   * @return the number of bytes read
   */
  int read(byte[] buffer);

  /**
   * Read bytes into the buffer.
   *
   * @param buffer
   *          the buffer to read the bytes into
   * @param offset
   *          the first position in the buffer to place the bytes read
   * @param length
   *          the number of bytes to try and read
   *
   * @return the actual number of bytes read
   */
  int read(byte[] buffer, int offset, int length);

  /**
   * Flush the output buffering.
   */
  void flush();

  /**
   * Write the byte out to the serial port
   *
   * @param b
   *          the byte value to write
   */
  void write(int b);

  /**
   * Write the array of bytes out to the serial port
   *
   * @param b
   *          the byte array to write
   */
  void write(byte[] b);

  /**
   * Write the array of bytes out to the serial port
   *
   * @param b
   *          the byte array to write
   * @param offset
   *          the position of the first byte to be written in the array
   * @param length
   *          the number of bytes to write
   */
  void write(byte[] b, int offset, int length);

  /**
   * Set the baud rate of the connection.
   *
   * @param baud
   *          the baud rate
   *
   * @return the endpoint
   */
  SerialCommunicationEndpoint setBaud(int baud);

  /**
   * Set the number of data bits for the connection.
   *
   * @param dataBits
   *          the number of data bits
   *
   * @return the endpoint
   */
  SerialCommunicationEndpoint setDataBits(int dataBits);

  /**
   * Set the number of stop bits for the connection.
   *
   * @param stopBits
   *          the number of stop bits
   *
   * @return the endpoint
   */
  SerialCommunicationEndpoint setStopBits(int stopBits);

  /**
   * Set the type of parity for the connection.
   *
   * @param parity
   *          the type of parity
   *
   * @return the endpoint
   */
  SerialCommunicationEndpoint setParity(Parity parity);

  /**
   * Types of parity.
   *
   * @author Keith M. Hughes
   */
  public enum Parity {
    NONE, EVEN, ODD, SPACE, MARK
  }
}