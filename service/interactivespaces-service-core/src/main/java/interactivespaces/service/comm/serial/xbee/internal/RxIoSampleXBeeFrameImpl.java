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

import interactivespaces.service.comm.serial.xbee.RxIoSampleXBeeFrame;
import interactivespaces.service.comm.serial.xbee.XBeeAddress16;
import interactivespaces.service.comm.serial.xbee.XBeeAddress64;

import java.util.List;

/**
 * A response frame for an XBee RX IO Sample.
 *
 * @author Keith M. Hughes
 */
public class RxIoSampleXBeeFrameImpl implements RxIoSampleXBeeFrame {

  /**
   * The 64 bit address for the remote radio
   */
  private XBeeAddress64 address64;

  /**
   * The 16 bit address for the remote radio
   */
  private XBeeAddress16 address16;

  /**
   * The receive options.
   */
  private int receiveOptions;

  /**
   * Mask for the digital IO channels.
   */
  private int digitalchannelMask;

  /**
   * Mask for the analog IO channels.
   */
  private int analogChannelMask;

  /**
   * The digital samples.
   */
  private int digitalSamples;

  /**
   * All analog samples that were taken.
   */
  private List<Integer> analogSamples;

  public RxIoSampleXBeeFrameImpl(XBeeAddress64 address64, XBeeAddress16 address16,
      int receiveOptions, int digitalchannelMask, int analogChannelMask, int digitalSamples,
      List<Integer> analogSamples) {
    this.address64 = address64;
    this.address16 = address16;
    this.digitalchannelMask = digitalchannelMask;
    this.analogChannelMask = analogChannelMask;
    this.digitalSamples = digitalSamples;
    this.analogSamples = analogSamples;
  }

  @Override
  public XBeeAddress64 getAddress64() {
    return address64;
  }

  @Override
  public XBeeAddress16 getAddress16() {
    return address16;
  }

  @Override
  public int getReceiveOptions() {
    return receiveOptions;
  }

  @Override
  public int getDigitalChannelMask() {
    return digitalchannelMask;
  }

  @Override
  public int getAnalogChannelMask() {
    return analogChannelMask;
  }

  @Override
  public int getDigitalSamples() {
    return digitalSamples;
  }

  @Override
  public boolean getDigitalSample(int sample) {
    return (digitalSamples & sample) != 0;
  }

  @Override
  public List<Integer> getAnalogSamples() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String toString() {
    return "RxIoSampleXBeeFrameImpl [address64=" + address64 + ", address16=" + address16
        + ", receiveOptions=" + receiveOptions + "]";
  }
}
