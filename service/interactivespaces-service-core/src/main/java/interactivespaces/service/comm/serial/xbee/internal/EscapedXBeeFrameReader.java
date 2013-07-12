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

import interactivespaces.InteractiveSpacesException;
import interactivespaces.service.comm.serial.SerialCommunicationEndpoint;
import interactivespaces.service.comm.serial.xbee.XBeeApiConstants;
import interactivespaces.util.InteractiveSpacesUtilities;

/**
 * An XBee frame reader which supports the Escaped API.
 *
 * <p>
 * Instances of this class are not thread safe and should only be used by one
 * reader thread.
 *
 * @author Keith M. Hughes
 */
public class EscapedXBeeFrameReader {

  /**
   * The number of milliseconds to wait for a start frame if we don't have one.
   */
  public static final int START_FRAME_WAIT_DELAY = 100;

  /**
   * The endpoint which is connected to the XBee.
   */
  private SerialCommunicationEndpoint endpoint;

  public EscapedXBeeFrameReader(SerialCommunicationEndpoint endpoint) {
    this.endpoint = endpoint;
  }

  /**
   * Scan until either the start frame is found, or the end of stream is found.
   *
   * @return {@code true} if the start of frame was found, {@code false} if
   *         stream ended
   */
  public boolean waitForStartFrame() throws InterruptedException {
    // Read until either the frame start byte is read or the end of stream
    // is reached.
    int b;
    while (endpoint.available() == 0) {
      Thread.sleep(START_FRAME_WAIT_DELAY);
    }

    while ((b = endpoint.read()) != XBeeApiConstants.FRAME_START_BYTE && b != -1)
      ;

    return b != -1;
  }

  /**
   * Get a byte from the frame.
   *
   * @return the byte
   */
  public int readByte() {
    // This will check for an escape byte and fix it.
    int b = endpoint.read();

    if (b == -1) {
      throw new InteractiveSpacesException("End of stream reached while reading XBee frame");
    }

    if (b == XBeeApiConstants.ESCAPE_BYTE) {
      b = endpoint.read();

      if (b == -1) {
        throw new InteractiveSpacesException("End of stream reached while reading XBee frame");
      }

      b = XBeeApiConstants.ESCAPE_BYTE_VALUE ^ b;
    }

    return b & 0xff;
  }

  /**
   * Read the packet length from the stream.
   *
   * @return the packet length, in bytes
   */
  public int readPacketLength() {
    int highLength = readByte();
    int lowLength = readByte();

    return highLength << 8 | lowLength;
  }

  /**
   * Read the number of bytes into an array.
   *
   * @param bytesToRead
   *          the number of bytes to read
   *
   * @return a new array with the read bytes
   */
  public byte[] readData(int bytesToRead) {
    byte[] bytes = new byte[bytesToRead];

    for (int i = 0; i < bytesToRead; i++) {
      bytes[i] = (byte) readByte();
    }

    return bytes;
  }

}
