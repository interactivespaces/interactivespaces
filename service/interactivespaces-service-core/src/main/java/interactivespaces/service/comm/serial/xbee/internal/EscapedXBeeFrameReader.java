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

import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.service.comm.serial.SerialCommunicationEndpoint;
import interactivespaces.service.comm.serial.xbee.XBeeApiConstants;

/**
 * An XBee frame reader which supports the Escaped API.
 *
 * <p>
 * Instances of this class are not thread safe and should only be used by one reader thread.
 *
 * @author Keith M. Hughes
 */
public class EscapedXBeeFrameReader {

  /**
   * The default size of the read buffer in bytes.
   */
  public static final int READ_BUFFER_SIZE_DEFAULT = 1024;

  /**
   * The number of milliseconds to wait for a start frame if we don't have one.
   */
  public static final int START_FRAME_WAIT_DELAY = 100;

  /**
   * The endpoint which is connected to the XBee.
   */
  private final SerialCommunicationEndpoint endpoint;

  /**
   * Buffer for serial data.
   */
  private final byte[] buffer;

  /**
   * Position to read bytes from in the buffer.
   */
  private int readPos;

  /**
   * The number of bytes in the buffer.
   */
  private int numberBytesInBuffer;

  /**
   * Construct a new frame reader.
   *
   * @param endpoint
   *          the endpoint that the frames come from
   */
  public EscapedXBeeFrameReader(SerialCommunicationEndpoint endpoint) {
    this.endpoint = endpoint;

    buffer = new byte[READ_BUFFER_SIZE_DEFAULT];
    readPos = 0;
    numberBytesInBuffer = 0;
  }

  /**
   * Scan until either the start frame is found, or the end of stream is found.
   *
   * @return {@code true} if the start frame was found, {@code false} if stream ended
   *
   * @throws InterruptedException
   *           the read thread was interrupted
   */
  public boolean waitForStartFrame() throws InterruptedException {
    // Read until either the frame start byte is read or the end of stream
    // is reached.
    int b = readByteFromSerial();
    while (b != -1) {
      if (b == XBeeApiConstants.FRAME_START_BYTE) {
        return true;
      }

      b = readByteFromSerial();
    }

    return false;
  }

  /**
   * Get a byte from the frame.
   *
   * @return the byte
   *
   * @throws InterruptedException
   *           the read thread was interrupted
   */
  public int readByte() throws InterruptedException {
    // This will check for an escaped byte and retrieve its unescaped value.
    int b = readByteFromSerial();

    if (b == -1) {
      throw new SimpleInteractiveSpacesException("End of stream reached while reading XBee frame");
    }

    if (b == XBeeApiConstants.ESCAPE_BYTE) {
      b = readByteFromSerial();

      if (b == -1) {
        throw new SimpleInteractiveSpacesException("End of stream reached while reading XBee frame");
      }

      b = XBeeApiConstants.ESCAPE_BYTE_VALUE ^ b;
    }

    return b;
  }

  /**
   * Read the packet length from the stream.
   *
   * @return the packet length, in bytes
   *
   * @throws InterruptedException
   *           the read thread was interrupted
   */
  public int readPacketLength() throws InterruptedException {
    int highLength = readByte();
    int lowLength = readByte();

    return highLength << 8 | lowLength;
  }

  /**
   * Read the number of bytes into a new array.
   *
   * @param bytesToRead
   *          the number of bytes to read
   *
   * @return a new array with the read bytes
   *
   * @throws InterruptedException
   *           the read thread was interrupted
   */
  public byte[] readData(int bytesToRead) throws InterruptedException {
    byte[] bytes = new byte[bytesToRead];

    readData(bytes);

    return bytes;
  }

  /**
   * Read bytes into the given array. The number read will be the size of the array.
   *
   * @param bytes
   *          the byte array
   *
   * @throws InterruptedException
   *           the read thread was interrupted
   */
  public void readData(byte[] bytes) throws InterruptedException {
    // TODO(keith): Consider reading buffer in a tighter loop until get to escape byte.
    // More complex read logic, but could be much faster.
    for (int i = 0; i < bytes.length; i++) {
      bytes[i] = (byte) readByte();
    }
  }

  /**
   * Read a byte from the serial connection.
   *
   * @return the byte
   *
   * @throws InterruptedException
   *           the read thread was interrupted
   */
  private int readByteFromSerial() throws InterruptedException {
    if (readPos == numberBytesInBuffer) {
      while (endpoint.available() == 0) {
        Thread.sleep(START_FRAME_WAIT_DELAY);
      }

      readPos = 0;
      numberBytesInBuffer = endpoint.read(buffer);

      if (numberBytesInBuffer == -1) {
        return -1;
      }
    }

    return buffer[readPos++] & 0xff;
  }
}
