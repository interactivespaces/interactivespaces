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

/**
 * An incoming Open Sound Control message that can generate a response.
 *
 * @author Keith M. Hughes
 */
public interface RespondableOpenSoundControlIncomingMessage extends OpenSoundControlIncomingMessage {

  /**
   * Create a new OSC message that is a reply to the current message and immediately send it.
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
  OpenSoundControlOutgoingMessage sendResponseMessage(String address, Object... arguments);

  /**
   * Create a new OSC message that is a reply to the current message.
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
  OpenSoundControlOutgoingMessage newResponseMessage(String address, Object... arguments);

  /**
   * Create a new OSC message that is a reply to the current message by specifying all argument types.
   *
   * <p>
   * The arguments will have to be added to the message before it is sent.
   *
   * @param address
   *          the OSC address for the message
   * @param types
   *          OSC type tag string
   *
   * @return the new OSC message
   */
  OpenSoundControlOutgoingMessage newResponseMessageWithTypes(String address, String types);

  /**
   * Create a new OSC message that is a reply to the current message by specifying all argument types.
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
  OpenSoundControlOutgoingMessage newResponseMessageWithTypes(String address, byte... types);
}
