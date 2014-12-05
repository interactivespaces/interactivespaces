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

package interactivespaces.service.comm.network.client;

import interactivespaces.service.comm.network.WriteableUdpPacket;
import interactivespaces.util.resource.ManagedResource;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * A communication endpoint for UDP clients.
 *
 * @author Keith M. Hughes
 */
public interface UdpClientNetworkCommunicationEndpoint extends ManagedResource {

  /**
   * Get the byte order of the client.
   *
   * @return the byte order of the client
   */
  ByteOrder getByteOrder();

  /**
   * Get a byte buffer for the data in the proper endianness for the client.
   *
   * @param data
   *          the data to be wrapped by the buffer
   *
   * @return a new byte buffer
   */
  ByteBuffer newByteBuffer(byte[] data);

  /**
   * Write a packet to the remote server.
   *
   * @param remoteAddress
   *          the remote address to send the packet to
   * @param data
   *          data in the packet
   */
  void write(InetSocketAddress remoteAddress, byte[] data);

  /**
   * Write a packet to the remote server.
   *
   * @param remoteAddress
   *          the remote address to send the packet to
   * @param data
   *          data in the packet
   * @param length
   *          number of bytes to send from the array
   */
  void write(InetSocketAddress remoteAddress, byte[] data, int length);

  /**
   * Write a packet to the remote server.
   *
   * @param remoteAddress
   *          the remote address to send the packet to
   * @param data
   *          data in the packet
   * @param offset
   *          position of the first byte in the array to send
   * @param length
   *          number of bytes to send from the array
   */
  void write(InetSocketAddress remoteAddress, byte[] data, int offset, int length);

  /**
   * Create a new UDP packet.
   *
   * <p>
   * The packet will be of a dynamic size.
   *
   * @return UDP packet of the proper endian
   *
   * @depecated Use {@link #newDynamicWriteableUdpPacket()}.
   */
  @Deprecated
  UdpPacket newDynamicUdpPacket();

  /**
   * Create a new UDP packet.
   *
   * @param size
   *          size of the packet
   *
   * @return UDP packet of the proper endian
   *
   * @depecated Use {@link #newWriteableUdpPacket()}.
   */
  @Deprecated
  UdpPacket newUdpPacket(int size);

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

  /**
   * Add a listener to the endpoint.
   *
   * @param listener
   *          the listener to add
   */
  void addListener(UdpClientNetworkCommunicationEndpointListener listener);

  /**
   * Remove a listener from the endpoint.
   *
   * <p>
   * A noop if the listener wasn't previously added.
   *
   * @param listener
   *          the listener to remove
   */
  void removeListener(UdpClientNetworkCommunicationEndpointListener listener);
}
