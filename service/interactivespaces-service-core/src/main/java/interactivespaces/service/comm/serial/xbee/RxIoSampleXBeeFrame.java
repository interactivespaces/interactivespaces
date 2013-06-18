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

import java.util.List;

/**
 * An RX IO Sample XBee frame.
 *
 * @author Keith M. Hughes
 */
public interface RxIoSampleXBeeFrame {

  /**
   * Get the 64 bit address of the sender of the RX frame.
   *
   * @return the sender's 64 bit address
   */
  XBeeAddress64 getAddress64();

  /**
   * Get the 16 bit address of the sender of the RX frame.
   *
   * @return the sender's 16 bit address
   */
  XBeeAddress16 getAddress16();

  /**
   * @return the receiveOptions
   */
  int getReceiveOptions();

  /**
   * Get the mask of used digital channels.
   *
   * @return the mask of used digital channels
   */
  int getDigitalChannelMask();

  /**
   * Get the mask of used analog channels.
   *
   * @return the mask of used analog channels
   */
  int getAnalogChannelMask();

  /**
   * Get all digital samples.
   */
  int getDigitalSamples();

  /**
   * Get a digital sample.
   *
   * @param sample
   *          code for the sample wanted
   *
   * @return {@code true} if sample was on
   */
  boolean getDigitalSample(int sample);

  /**
   * Get all analog samples.
   *
   * @return all analog samples
   */
  List<Integer> getAnalogSamples();
}
