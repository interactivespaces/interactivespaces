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

import interactivespaces.service.comm.serial.SerialCommunicationEndpoint;
import interactivespaces.service.comm.serial.xbee.RequestXBeeFrame;
import interactivespaces.service.comm.serial.xbee.XBeeApiConstants;
import interactivespaces.service.comm.serial.xbee.XBeeCommunicationEndpoint;

import java.io.ByteArrayOutputStream;

/**
 * The base for all XBee Frame Writers.
 *
 * @author Keith M. Hughes
 */
public class BaseRequestXBeeFrame implements RequestXBeeFrame {

  /**
   * Where bytes are written for the frame
   */
  private ByteArrayOutputStream bos = new ByteArrayOutputStream();

  /**
   * The checksum for the frame.
   */
  private int checksum = 0;

  /**
   * The total number of bytes in the frame counted in the length field of the
   * frame.
   */
  private int length = 0;

  /**
   * Construct an XBee Write Frame
   *
   * @param frameType
   *          the type of frame
   */
  public BaseRequestXBeeFrame(int frameType) {
    add(frameType);
  }

  @Override
  public void add(int b) {
    writeByte(b);
    checksum += b;
    length++;
  }

  @Override
  public void add16(int i) {
    int b1 = (i >> 8) & 0xff;
    int b2 = i & 0xff;

    writeByte(b1);
    writeByte(b2);
    checksum += b1 + b2;
    length += 2;
  }

  @Override
  public void add(byte[] ba) {
    for (byte b : ba) {
      writeByte(b);
      checksum += ((int) b & 0xff);
    }
    length += ba.length;
  }

  @Override
  public void add(int[] ia) {
    for (int i : ia) {
      writeByte(i);
      checksum += i & 0xff;
    }
    length += ia.length;
  }

  @Override
  public void write(XBeeCommunicationEndpoint xbeeEndpoint) {
    checksum &= 0xff;
    checksum = 0xff - checksum;
    writeByte(checksum);

    byte[] bytes = bos.toByteArray();

    // Get proper length into the frame.
    int lengthUpper = ((length >> 8) & 0xff);
    int lengthLower = (length & 0xff);

    SerialCommunicationEndpoint commEndpoint = xbeeEndpoint.getSerialCommunicationEndpoint();
    commEndpoint.write(XBeeApiConstants.FRAME_START_BYTE);

    writeByte(lengthUpper, commEndpoint);
    writeByte(lengthLower, commEndpoint);

    commEndpoint.write(bytes);
    commEndpoint.flush();
    System.out.println("Done writing frame");
  }

  /**
   * Write a value appropriately escaped value to the endpoint.
   *
   * @param value
   *          the value to write
   * @param commEndpoint
   *          the endpoint
   */
  private void writeByte(int value, SerialCommunicationEndpoint commEndpoint) {
    if (XBeeApiConstants.isEscaped(value)) {
      commEndpoint.write(XBeeApiConstants.ESCAPE_BYTE);
      commEndpoint.write(XBeeApiConstants.ESCAPE_BYTE_VALUE ^ value);
    } else {
      commEndpoint.write(value);
    }
  }

  /**
   * Write a value appropriately escaped value to the internal byte stream.
   *
   * @param value
   *          the value to write
   */
  private void writeByte(int value) {
    if (XBeeApiConstants.isEscaped(value)) {
      bos.write(XBeeApiConstants.ESCAPE_BYTE);
      bos.write(XBeeApiConstants.ESCAPE_BYTE_VALUE ^ value);
    } else {
      bos.write(value);
    }
  }
}
