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

import interactivespaces.util.resource.ManagedResource;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;

/**
 * A communication endpoint for UDP clients which listens on a UDP broadcast port.
 *
 * @author Keith M. Hughes
 */
public interface UdpBroadcastClientNetworkCommunicationEndpoint extends ManagedResource {

  /**
   * Get the port being used by the client.
   *
   * @return the port being used
   */
  int getPort();

  /**
   * Add a listener to the endpoint.
   *
   * @param listener
   *          the listener to add
   */
  void addListener(UdpBroadcastClientNetworkCommunicationEndpointListener listener);

  /**
   * Remove a listener from the endpoint.
   *
   * <p>
   * A noop if the listener wasn't previously added.
   *
   * @param listener
   *          the listener to remove
   */
  void removeListener(UdpBroadcastClientNetworkCommunicationEndpointListener listener);

  /**
   * Join a multicast group.
   *
   * @param multicastAddress
   *          the address of the group to join
   */
  void joinGroup(InetAddress multicastAddress);

  /**
   * Join a multicast group.
   *
   * @param multicastAddress
   *          the address of the group to join
   * @param networkInterface
   *          the interface to connect to the group on
   */
  void joinGroup(InetSocketAddress multicastAddress, NetworkInterface networkInterface);

  /**
   * Leave a multicast group.
   *
   * @param multicastAddress
   *          the address of the group to leave
   */
  void leaveGroup(InetAddress multicastAddress);

  /**
   * Leave a multicast group.
   *
   * @param multicastAddress
   *          the address of the group to leave
   * @param networkInterface
   *          the interface the group was joined on
   */
  void leaveGroup(InetSocketAddress multicastAddress, NetworkInterface networkInterface);
}
