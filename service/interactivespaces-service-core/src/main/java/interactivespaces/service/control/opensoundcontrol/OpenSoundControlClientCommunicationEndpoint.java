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

package interactivespaces.service.control.opensoundcontrol;

import interactivespaces.util.resource.ManagedResource;

/**
 * An Open Sound Control client communication endpoint.
 *
 * @author Keith M. Hughes
 */
public interface OpenSoundControlClientCommunicationEndpoint extends ManagedResource {

  /**
   * Get the OSC server remote host.
   *
   * @return the OSC server remote host
   */
  String getRemoteHost();

  /**
   * Get the OSC server remote port.
   *
   * @return the OSC server remote port
   */
  int getRemotePort();

  /**
   * Create a new outgoing OSC message and send it immediately.
   *
   * <p>
   * The argument types will be determined by the classes of the arguments.
   *
   * @param address
   *          the OSC address for the message
   * @param arguments
   *          arguments for the message
   *
   * @return the new OSC message
   */
  OpenSoundControlOutgoingMessage sendRequestMessage(String address, Object... arguments);

  /**
   * Create a new outgoing OSC message.
   *
   * <p>
   * The argument types will be determined by the classes of the arguments.
   *
   * @param address
   *          the OSC address for the message
   * @param arguments
   *          arguments for the message
   *
   * @return the new OSC message
   */
  OpenSoundControlOutgoingMessage newRequestMessage(String address, Object... arguments);

  /**
   * Create a new outgoing OSC message by specifying all argument types.
   *
   * <p>
   * The arguments will have to be added to the message before it is sent.
   *
   * @param address
   *          the OSC address for the message
   * @param types
   *          OSC type tags string
   *
   * @return the new OSC message
   */
  OpenSoundControlOutgoingMessage newRequestMessageWithTypes(String address, String types);

  /**
   * Create a new outgoing OSC message by specifying all argument types.
   *
   * <p>
   * The arguments will have to be added to the message before it is sent.
   *
   * @param address
   *          the OSC address for the message
   * @param types
   *          OSC type tags
   *
   * @return the new OSC message
   */
  OpenSoundControlOutgoingMessage newRequestMessageWithTypes(String address, byte... types);

  /**
   * Register a response method for a specific OSC address.
   *
   * <p>
   * Multiple methods can be attached to a given address.
   *
   * @param oscAddress
   *          the OSC address the method will handle
   * @param method
   *          the method for the addressed messages
   */
  void registerResponseMethod(String oscAddress, OpenSoundControlClientResponseMethod method);

  /**
   * Unregister a response method for a specific OSC address.
   *
   * <p>
   * Does nothing if the method has not been registered for the particular address.
   *
   * @param oscAddress
   *          the OSC address the method was handling
   * @param method
   *          the method for the addressed messages
   */
  void unregisterResponseMethod(String oscAddress, OpenSoundControlClientResponseMethod method);

  /**
   * Register a method for handling unknown OSC response messages.
   *
   * <p>
   * Unknown OSC messages for this endpoint are defined to be ones that did not have an explicit address registered for
   * handling them.
   *
   * @param method
   *          the method
   */
  void registerUnknownMessageResponseMethod(OpenSoundControlClientResponseMethod method);

  /**
   * Unregister a method for handling unknown response messages.
   *
   * <p>
   * Does nothing if the method has not been registered.
   *
   * @param method
   *          the method for the addressed messages
   */
  void unregisterUnknownMessageResponseMethod(OpenSoundControlClientResponseMethod method);
}
