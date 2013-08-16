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

/**
 * A communication endpoint for UDP clients.
 *
 * @author Keith M. Hughes
 */
public interface UdpClientNetworkCommunicationEndpoint extends ManagedResource {

  /**
   * Write a packet to the remote server.
   *
   * @param data
   *          data in the packet
   */
  void write(byte[] data);

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