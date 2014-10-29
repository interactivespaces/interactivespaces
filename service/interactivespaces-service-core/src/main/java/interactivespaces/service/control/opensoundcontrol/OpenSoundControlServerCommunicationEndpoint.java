/*
 * Copyright (C) 2014 Google Inc.
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

package interactivespaces.service.control.opensoundcontrol;

import interactivespaces.util.resource.ManagedResource;

/**
 * A server endpoint for Open Sound Control requests.
 *
 * <p>
 * This version does not support wild card processing for OSC addresses.
 *
 * TODO(keith): Add wild card processing.
 *
 * @author Keith M. Hughes
 */
public interface OpenSoundControlServerCommunicationEndpoint extends ManagedResource {

  /**
   * Get the server port for the endpoint.
   *
   * @return the server port
   */
  int getServerPort();

  /**
   * Register a method for a specific OSC address.
   *
   * <p>
   * Multiple methods can be attached to a given address.
   *
   * @param oscAddress
   *          the OSC address the method will handle
   * @param method
   *          the method for the addressed packets
   */
  void registerMethod(String oscAddress, OpenSoundControlMethod method);

  /**
   * Unregister a method for a specific OSC address.
   *
   * <p>
   * Does nothing if the method has not been registered for the particular address.
   *
   * @param oscAddress
   *          the OSC address the method was handling
   * @param method
   *          the method for the addressed packets
   */
  void unregisterMethod(String oscAddress, OpenSoundControlMethod method);

  /**
   * Register a method for handling unknown OSC messages.
   *
   * <p>
   * Unknown OSC messages for this endpoint are defined to be ones which did not have an explicit address registered for
   * handling them.
   *
   * @param method
   *          the method
   */
  void registerUnknownMessageMethod(OpenSoundControlMethod method);

  /**
   * Unregister a method for handling unknown messages.
   *
   * <p>
   * Does nothing if the method has not been registered.
   *
   * @param method
   *          the method for the addressed packets
   */
  void unregisterUnknownMessageMethod(OpenSoundControlMethod method);
}
