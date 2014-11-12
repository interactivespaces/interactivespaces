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

package interactivespaces.service.comm.network.server;

import interactivespaces.service.comm.network.WriteableUdpPacket;

import java.net.InetSocketAddress;

/**
 * A request which has come into a {@link UdpServerNetworkCommunicationEndpoint}
 * .
 *
 * @author Keith M. Hughes
 */
public interface UdpServerRequest {

  /**
   * Get the address of the remote connection.
   *
   * @return the address of the remote connection
   */
  InetSocketAddress getRemoteAddress();

  /**
   * Get the request to the server.
   *
   * @return the request data
   */
  byte[] getRequest();

  /**
   * Write a response to the request.
   *
   * @param response
   *          the response
   */
  void writeResponse(byte[] response);

  /**
   * Create a new UDP packet.
   *
   * <p>
   * The packet will be of a dynamic size.
   *
   * @return UDP packet of the proper endian
   */
  WriteableUdpPacket newDynamicWriteableUdpPacket();

  /**
   * Create a new UDP packet.
   *
   * @param size
   *          size of the packet
   *
   * @return UDP packet of the proper endian
   */
  WriteableUdpPacket newWriteableUdpPacket(int size);
}
