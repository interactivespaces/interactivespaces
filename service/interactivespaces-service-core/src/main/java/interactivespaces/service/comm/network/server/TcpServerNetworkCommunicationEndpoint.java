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

import interactivespaces.util.resource.ManagedResource;

/**
 * Communication endpoint for a TCP based server.
 *
 * @param <T>
 *          the type for the messages
 *
 * @author Keith M. Hughes
 */
public interface TcpServerNetworkCommunicationEndpoint<T> extends ManagedResource {

  /**
   * Get the server port being listened to.
   *
   * @return the server port being listened to
   */
  int getServerPort();

  /**
   * Add a listener to the endpoint.
   *
   * @param listener
   *          the listener to add
   */
  void addListener(TcpServerNetworkCommunicationEndpointListener<T> listener);

  /**
   * Remove a listener from the endpoint.
   *
   * <p>
   * A noop if the listener wasn't previously added.
   *
   * @param listener
   *          the listener to remove
   */
  void removeListener(TcpServerNetworkCommunicationEndpointListener<T> listener);
}
