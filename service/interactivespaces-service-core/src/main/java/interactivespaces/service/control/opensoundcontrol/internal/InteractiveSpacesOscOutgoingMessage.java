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

package interactivespaces.service.control.opensoundcontrol.internal;

import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.service.comm.network.WriteableUdpPacket;
import interactivespaces.service.control.opensoundcontrol.OpenSoundControlConstants;
import interactivespaces.service.control.opensoundcontrol.OpenSoundControlOutgoingMessage;

import java.net.InetSocketAddress;

/**
 * An OSC outgoing message for an {@link InteractiveSpacesUdpOpenSoundControlClientCommunicationsEndpoint}.
 *
 * @author Keith M. Hughes
 */
public class InteractiveSpacesOscOutgoingMessage implements OpenSoundControlOutgoingMessage {

  /**
   * Padding bytes.
   */
  public static final byte[] PADDING = new byte[] { 0, 0, 0, 0 };

  /**
   * ASCII for a comma.
   */
  public static final byte COMMA = 0x2c;

  /**
   * OSC Address for the message.
   */
  private String oscAddress;

  /**
   * UDP packet that will be sent to the remote server.
   */
  private WriteableUdpPacket message;

  /**
   * The address where the message will be sent.
   */
  private InetSocketAddress remoteAddress;

  /**
   * Construct a new message.
   *
   * @param message
   *          the message being built up
   * @param remoteAddress
   *          the address where the message will be sent
   * @param address
   *          the OSC address
   * @param types
   *          the types of the arguments
   */
  public InteractiveSpacesOscOutgoingMessage(WriteableUdpPacket message, InetSocketAddress remoteAddress,
      String address, String types) {
    this(message, remoteAddress, address, types.getBytes());
  }

  /**
   * Construct a new message.
   *
   * @param message
   *          the message being built up
   * @param remoteAddress
   *          the address where the message will be sent
   * @param address
   *          the OSC address
   * @param types
   *          the types of the arguments
   */
  public InteractiveSpacesOscOutgoingMessage(WriteableUdpPacket message, InetSocketAddress remoteAddress,
      String address, byte... types) {
    this.message = message;
    this.remoteAddress = remoteAddress;

    addString(address);
    addArgumentTypeTags(types);
  }

  @Override
  public String getAddress() {
    return oscAddress;
  }

  @Override
  public void send() {
    message.write(remoteAddress);
  }

  @Override
  public OpenSoundControlOutgoingMessage addString(String value) {
    byte[] bytes = value.getBytes();
    message.writeBytes(bytes);
    message.writeBytes(PADDING, 0, OpenSoundControlConstants.OPEN_SOUND_CONTROL_BYTE_BOUNDARY
        - (bytes.length % OpenSoundControlConstants.OPEN_SOUND_CONTROL_BYTE_BOUNDARY));

    return this;
  }

  @Override
  public OpenSoundControlOutgoingMessage addInt(int value) {
    message.writeInt(value);

    return this;
  }

  @Override
  public OpenSoundControlOutgoingMessage addLong(long value) {
    message.writeLong(value);

    return this;
  }

  @Override
  public OpenSoundControlOutgoingMessage addFloat(float value) {
    message.writeFloat(value);

    return this;
  }

  @Override
  public OpenSoundControlOutgoingMessage addDouble(double value) {
    message.writeDouble(value);

    return this;
  }

  @Override
  public OpenSoundControlOutgoingMessage addBlob(byte[] blob) {
    return addBlob(blob, 0, blob.length);
  }

  @Override
  public OpenSoundControlOutgoingMessage addBlob(byte[] blob, int start, int length) {
    if (start + length > blob.length) {
      throw new SimpleInteractiveSpacesException(String.format(
          "Tried to write more data from a blob than there was available,  offset %d, length %d, actual length %d",
          start, length, blob.length));
    }

    addInt(length);
    message.writeBytes(blob, start, length);
    int byteBoundaryPos = blob.length % OpenSoundControlConstants.OPEN_SOUND_CONTROL_BYTE_BOUNDARY;
    if (byteBoundaryPos != 0) {
      message.writeBytes(PADDING, 0, OpenSoundControlConstants.OPEN_SOUND_CONTROL_BYTE_BOUNDARY - byteBoundaryPos);
    }

    return this;
  }

  @Override
  public int getMessageSize() {
    return message.getPacketSize();
  }

  /**
   * Add the argument type tags.
   *
   * @param typeTags
   *          the argument type tags
   */
  private void addArgumentTypeTags(byte... typeTags) {
    message.writeByte(COMMA);
    message.writeBytes(typeTags);
    message.writeBytes(PADDING, 0, OpenSoundControlConstants.OPEN_SOUND_CONTROL_BYTE_BOUNDARY
        - ((typeTags.length + 1) % OpenSoundControlConstants.OPEN_SOUND_CONTROL_BYTE_BOUNDARY));
  }
}
