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

import com.google.common.collect.Lists;

import interactivespaces.service.comm.serial.xbee.AtLocalResponseXBeeFrame;
import interactivespaces.service.comm.serial.xbee.AtRemoteResponseXBeeFrame;
import interactivespaces.service.comm.serial.xbee.RxIoSampleXBeeFrame;
import interactivespaces.service.comm.serial.xbee.RxResponseXBeeFrame;
import interactivespaces.service.comm.serial.xbee.TxStatusXBeeFrame;
import interactivespaces.service.comm.serial.xbee.XBeeAddress16;
import interactivespaces.service.comm.serial.xbee.XBeeAddress64;
import interactivespaces.service.comm.serial.xbee.XBeeApiConstants;

import org.apache.commons.logging.Log;

import java.util.List;

/**
 * A frame parser for escaped XBee API frames.
 *
 * @author Keith M. Hughes
 */
public class SimpleXBeeResponseFrameParser implements XBeeResponseFrameParser {

  @Override
  public AtLocalResponseXBeeFrame parseAtLocalResponse(EscapedXBeeFrameReader reader,
      int bytesLeft, Log log) {
    log.info("AT Local Response");

    int frameId = reader.readByte();

    byte atCommandUpper = (byte) reader.readByte();
    byte atCommandLower = (byte) reader.readByte();

    int commandStatus = reader.readByte();

    byte[] commandData = reader.readData(bytesLeft - 4);

    return new AtLocalResponseXBeeFrameImpl(frameId, new byte[] { atCommandUpper, atCommandLower },
        commandStatus, commandData);
  }

  @Override
  public AtRemoteResponseXBeeFrame parseAtRemoteResponse(EscapedXBeeFrameReader reader,
      int bytesLeft, Log log) {
    log.info("Received AT Remote Response");

    int frameId = reader.readByte();

    XBeeAddress64 address64 = parseXBeeAddress64(reader);
    XBeeAddress16 address16 = parseXBeeAddress16(reader);

    byte atCommandUpper = (byte) reader.readByte();
    byte atCommandLower = (byte) reader.readByte();

    int commandStatus = reader.readByte();

    byte[] commandData = reader.readData(bytesLeft - 14);

    return new AtRemoteResponseXBeeFrameImpl(frameId, address64, address16, new byte[] {
        atCommandUpper, atCommandLower }, commandStatus, commandData);
  }

  @Override
  public TxStatusXBeeFrame parseTxStatus(EscapedXBeeFrameReader reader, int bytesLeft, Log log) {
    log.info("Received TX Response");

    int frameId = reader.readByte();
    XBeeAddress16 address16 = parseXBeeAddress16(reader);
    int transmitRetryCount = reader.readByte();
    int deliveryStatus = reader.readByte();
    int discoveryStatus = reader.readByte();

    return new TxStatusXBeeFrameImpl(frameId, address16, transmitRetryCount, deliveryStatus,
        discoveryStatus);
  }

  @Override
  public RxResponseXBeeFrame parseReceiveResponse(EscapedXBeeFrameReader reader, int bytesLeft,
      Log log) {
    log.info("RX Receive");

    XBeeAddress64 address64 = parseXBeeAddress64(reader);
    XBeeAddress16 address16 = parseXBeeAddress16(reader);

    int receiveOptions = reader.readByte();

    byte[] receivedData = reader.readData(bytesLeft - 11);

    return new RxResponseXBeeFrameImpl(address64, address16, receiveOptions, receivedData);
  }

  @Override
  public RxIoSampleXBeeFrame parseIoSampleResponse(EscapedXBeeFrameReader reader, int bytesLeft,
      Log log) {
    log.info("RX Receive");

    XBeeAddress64 address64 = parseXBeeAddress64(reader);
    XBeeAddress16 address16 = parseXBeeAddress16(reader);

    int receiveOptions = reader.readByte();

    // The number of samples field is currently unused.
    reader.readByte();

    int digitalChannelMask = reader.readByte() << 8 | reader.readByte();
    int analogChannelMask = reader.readByte();

    int digitalSamples = 0;
    if (digitalChannelMask != 0) {
      digitalSamples = reader.readByte() << 8 | reader.readByte();
    }

    List<Integer> analogSamples = Lists.newArrayList();
    if ((analogChannelMask & XBeeApiConstants.RX_IO_SAMPLE_ANALOG_A0) != 0) {
      analogSamples.add(reader.readByte() << 8 | reader.readByte());
    }
    if ((analogChannelMask & XBeeApiConstants.RX_IO_SAMPLE_ANALOG_A1) != 0) {
      analogSamples.add(reader.readByte() << 8 | reader.readByte());
    }
    if ((analogChannelMask & XBeeApiConstants.RX_IO_SAMPLE_ANALOG_A2) != 0) {
      analogSamples.add(reader.readByte() << 8 | reader.readByte());
    }
    if ((analogChannelMask & XBeeApiConstants.RX_IO_SAMPLE_ANALOG_A3) != 0) {
      analogSamples.add(reader.readByte() << 8 | reader.readByte());
    }

    return new RxIoSampleXBeeFrameImpl(address64, address16, receiveOptions, digitalChannelMask,
        analogChannelMask, digitalSamples, analogSamples);
  }

  /**
   * Get an XBee 16 bit address from the frame
   *
   * @param reader
   *          the frame reader
   *
   * @return the XBee 64 bit address
   */
  private XBeeAddress64 parseXBeeAddress64(EscapedXBeeFrameReader reader) {
    return new XBeeAddress64Impl(reader.readByte(), reader.readByte(), reader.readByte(),
        reader.readByte(), reader.readByte(), reader.readByte(), reader.readByte(),
        reader.readByte());
  }

  /**
   * Get an XBee 16 bit address from the frame
   *
   * @param reader
   *          the frame reader
   *
   * @return the XBee 16 bit address
   */
  private XBeeAddress16 parseXBeeAddress16(EscapedXBeeFrameReader reader) {
    return new XBeeAddress16Impl(reader.readByte(), reader.readByte());
  }
}
