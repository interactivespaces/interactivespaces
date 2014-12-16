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

import interactivespaces.comm.CommunicationEndpoint;
import interactivespaces.service.comm.serial.SerialCommunicationEndpoint;

/**
 * An XBee communication endpoint.
 *
 * <p>
 * This communication endpoint assumes the XBee is a Series 2 radio in escaped API mode (AP=2).
 *
 * @author Keith M. Hughes
 */
public interface XBeeCommunicationEndpoint extends CommunicationEndpoint {

  /**
   * Add a listener to the endpoint.
   *
   * @param listener
   *          the listener to add
   */
  void addListener(XBeeResponseListener listener);

  /**
   * Remove a listener from the endpoint.
   *
   * <p>
   * Does nothing if the listener was never added
   *
   * @param listener
   *          the listener to remove
   */
  void removeListener(XBeeResponseListener listener);

  /**
   * Construct a new 16 bit XBee address.
   *
   * @param address
   *          the address as a hexadecimal string
   *
   * @return the new 16 bit address
   */
  XBeeAddress16 newXBeeAddress16(String address);

  /**
   * Create a new 16 bit XBee address.
   *
   * @param a1
   *          high order portion of address
   * @param a2
   *          low order portion of address
   *
   * @return the new address
   */
  XBeeAddress16 newXBeeAddress16(int a1, int a2);

  /**
   * Get the 16 bit broadcast address.
   *
   * @return the 16 bit broadcast address
   */
  XBeeAddress16 getBroadcastAddress16();

  /**
   * Construct a new 64 bit XBee address.
   *
   * @param addr
   *          the address as a hexadecimal string
   *
   * @return the new 64 bit address
   */
  XBeeAddress64 newXBeeAddress64(String addr);

  /**
   * Construct an XBee 64 address using the individual bytes.
   *
   * @param a1
   *          first byte of address, most significant
   * @param a2
   *          second byte of address
   * @param a3
   *          third byte of address
   * @param a4
   *          fourth byte of address
   * @param a5
   *          fifth byte of address
   * @param a6
   *          sixth byte of address
   * @param a7
   *          seventh byte of address
   * @param a8
   *          eighth byte of address
   *
   * @return the new 64 bit address
   */
  XBeeAddress64 newXBeeAddress64(int a1, int a2, int a3, int a4, int a5, int a6, int a7, int a8);

  /**
   * Get the special 64 address for the coordinator radio.
   *
   * @return the special 64 address for the coordinator radio
   */
  XBeeAddress64 getCoordinatorAddress();

  /**
   * Get the 64 bit broadcast address.
   *
   * @return the 64 bit broadcast address
   */
  XBeeAddress64 getBroadcastAddress64();

  /**
   * Construct a new XBee AT Local request.
   *
   * <p>
   * A frame number is automatically supplied by this endpoint.
   *
   * @param command
   *          the AT command to send
   *
   * @return a new XBee AT Local request
   */
  AtLocalRequestXBeeFrame newAtLocalRequestXBeeFrame(int[] command);

  /**
   * Construct a new XBee AT Local request.
   *
   * @param command
   *          the AT command to send
   * @param frameNumber
   *          the frame number
   *
   * @return a new XBee AT Local request
   */
  AtLocalRequestXBeeFrame newAtLocalRequestXBeeFrame(int[] command, int frameNumber);

  /**
   * Construct an XBee AT Remote request with fully specified address.
   *
   * <p>
   * A frame number is automatically supplied by this endpoint.
   *
   * @param address64
   *          the 64 bit destination address
   * @param address16
   *          the 16 bit destination address
   * @param command
   *          the AT command to send
   * @param options
   *          options for the frame
   *
   * @return a new remote request
   */
  AtRemoteRequestXBeeFrame newAtRemoteRequestXBeeFrame(XBeeAddress64 address64, XBeeAddress16 address16, int[] command,
      int options);

  /**
   * Construct an XBee AT Remote request with fully specified address.
   *
   * <p>
   * No response will be sent back.
   *
   * @param address64
   *          the 64 bit destination address
   * @param address16
   *          the 16 bit destination address
   * @param command
   *          the AT command to send
   * @param options
   *          options for the frame
   *
   * @return a new remote request
   */
  AtRemoteRequestXBeeFrame newAtRemoteRequestXBeeFrameNoResponse(XBeeAddress64 address64, XBeeAddress16 address16,
      int[] command, int options);

  /**
   * Construct an XBee AT Remote request with fully specified address.
   *
   * @param address64
   *          the 64 bit destination address
   * @param address16
   *          the 16 bit destination address
   * @param command
   *          the AT command to send
   * @param frameNumber
   *          the frame number, if {@code 0} no response is sent
   * @param options
   *          options for the frame
   *
   * @return a new remote request
   */
  AtRemoteRequestXBeeFrame newAtRemoteRequestXBeeFrame(XBeeAddress64 address64, XBeeAddress16 address16, int[] command,
      int frameNumber, int options);

  /**
   * Construct an AT Remote command XBee if the 16 bit address isn't known.
   *
   * <p>
   * A frame number is automatically supplied by this endpoint.
   *
   * @param address64
   *          the 64 bit destination address
   * @param command
   *          the AT command to send
   * @param options
   *          options for the frame
   *
   * @return a new remote request
   */
  AtRemoteRequestXBeeFrame newAtRemoteRequestXBeeFrame(XBeeAddress64 address64, int[] command, int options);

  /**
   * Construct an AT Remote command XBee if the 16 bit address isn't known.
   *
   * <p>
   * No response will be sent back.
   *
   * @param address64
   *          the 64 bit destination address
   * @param command
   *          the AT command to send
   * @param options
   *          options for the frame
   *
   * @return a new remote request
   */
  AtRemoteRequestXBeeFrame newAtRemoteRequestXBeeFrameNoResponse(XBeeAddress64 address64, int[] command, int options);

  /**
   * Construct an AT Remote command XBee if the 16 bit address isn't known.
   *
   * @param address64
   *          the 64 bit destination address
   * @param command
   *          the AT command to send
   * @param frameNumber
   *          the frame number, if {@code 0} no response is sent
   * @param options
   *          options for the frame
   *
   * @return a new remote request
   */
  AtRemoteRequestXBeeFrame newAtRemoteRequestXBeeFrame(XBeeAddress64 address64, int[] command, int frameNumber,
      int options);

  /**
   * Construct an XBee Remote Transmit Request with fully specified address.
   *
   * <p>
   * A frame number is automatically supplied by this endpoint.
   *
   * @param address64
   *          the 64 bit destination address
   * @param address16
   *          the 16 bit destination address
   * @param broadcastRadius
   *          the maximum number of hops to deliver the data if a broadcast transmission, if {@code 0} will be the
   *          maximum hops value as set by the {@code BH} parameter on the XBee
   * @param options
   *          options for the frame
   *
   * @return the new request
   */
  TxRequestXBeeFrame newTxRequestXBeeFrame(XBeeAddress64 address64, XBeeAddress16 address16, int broadcastRadius,
      int options);

  /**
   * Construct an XBee Remote Transmit Request with fully specified address.
   *
   * <p>
   * No response will be sent back.
   *
   * @param address64
   *          the 64 bit destination address
   * @param address16
   *          the 16 bit destination address
   * @param broadcastRadius
   *          the maximum number of hops to deliver the data if a broadcast transmission, if {@code 0} will be the
   *          maximum hops value as set by the {@code BH} parameter on the XBee
   * @param options
   *          options for the frame
   *
   * @return the new request
   */
  TxRequestXBeeFrame newTxRequestXBeeFrameNoResponse(XBeeAddress64 address64, XBeeAddress16 address16,
      int broadcastRadius, int options);

  /**
   * Construct an XBee Remote Transmit Request with a fully specified address.
   *
   * @param address64
   *          the 64 bit destination address
   * @param address16
   *          the 16 bit destination address
   * @param frameNumber
   *          the frame number, if {@code 0} no response is sent
   * @param broadcastRadius
   *          the maximum number of hops to deliver the data if a broadcast transmission, if {@code 0} will be the
   *          maximum hops value as set by the {@code BH} parameter on the XBee
   * @param options
   *          options for the frame
   *
   * @return the new request
   */
  TxRequestXBeeFrame newTxRequestXBeeFrame(XBeeAddress64 address64, XBeeAddress16 address16, int frameNumber,
      int broadcastRadius, int options);

  /**
   * Construct an XBee Remote transmit XBee request with an unknown 16 bit address.
   *
   * <p>
   * A frame number is automatically supplied by this endpoint.
   *
   * @param address64
   *          the 64 bit destination address
   * @param broadcastRadius
   *          the maximum number of hops to deliver the data if a broadcast transmission, if {@code 0} will be the
   *          maximum hops value as set by the {@code BH} parameter on the XBee
   * @param options
   *          options for the frame
   *
   * @return the new request
   */
  TxRequestXBeeFrame newTxRequestXBeeFrame(XBeeAddress64 address64, int broadcastRadius, int options);

  /**
   * Construct an Remote transmit XBee request if the 16 bit address isn't known.
   *
   * <p>
   * No response will be sent back.
   *
   * @param address64
   *          the 64 bit destination address
   * @param broadcastRadius
   *          the maximum number of hops to deliver the data if a broadcast transmission, if {@code 0} will be the
   *          maximum hops value as set by the {@code BH} parameter on the XBee
   * @param options
   *          options for the frame
   *
   * @return the new request
   */
  TxRequestXBeeFrame newTxRequestXBeeFrameNoResponse(XBeeAddress64 address64, int broadcastRadius, int options);

  /**
   * Construct an XBree Remote Transmit Request if the 16 bit address isn't known.
   *
   * @param address64
   *          the 64 bit destination address
   * @param frameNumber
   *          the frame number, if {@code 0} no response is sent
   * @param broadcastRadius
   *          the maximum number of hops to deliver the data if a broadcast transmission, if {@code 0} will be the
   *          maximum hops value as set by the {@code BH} parameter on the XBee
   * @param options
   *          options for the frame
   *
   * @return the new request
   */
  TxRequestXBeeFrame newTxRequestXBeeFrame(XBeeAddress64 address64, int frameNumber, int broadcastRadius, int options);

  /**
   * Get the serial communication endpoint associated with this endpoint.
   *
   * @return the communication endpoint
   */
  SerialCommunicationEndpoint getSerialCommunicationEndpoint();
}
