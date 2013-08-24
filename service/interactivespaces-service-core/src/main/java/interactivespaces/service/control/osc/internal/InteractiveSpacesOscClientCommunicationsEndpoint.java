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

package interactivespaces.service.control.osc.internal;

import interactivespaces.service.comm.network.client.UdpClientNetworkCommunicationEndpoint;
import interactivespaces.service.comm.network.client.UdpPacket;
import interactivespaces.service.control.osc.OscClientCommunicationEndpoint;
import interactivespaces.service.control.osc.OscClientPacket;

import java.net.InetSocketAddress;

import org.apache.commons.logging.Log;

/**
 * An Interactive Spaces implementation of an {@link OscClientCommunicationEndpoint}.
 *
 * @author Keith M. Hughes
 */
public class InteractiveSpacesOscClientCommunicationsEndpoint implements OscClientCommunicationEndpoint {

  /**
   * Padding bytes.
   */
  public static final byte[] PADDING = new byte[] { 0, 0, 0, 0 };

  /**
   * ASCII for a comma
   */
  public static final byte COMMA = 0x2c;

  /**
   * Address of the remote socket.
   */
  private InetSocketAddress remoteAddress;

  /**
   * Communication endpoint for talking to the OSC server.
   */
  private UdpClientNetworkCommunicationEndpoint commEndpoint;

  /**
   * Log for the connection
   */
  private Log log;

  public InteractiveSpacesOscClientCommunicationsEndpoint(InetSocketAddress remoteAddress,
      UdpClientNetworkCommunicationEndpoint commEndpoint, Log log) {
    this.remoteAddress = remoteAddress;
    this.commEndpoint = commEndpoint;
    this.log = log;
  }

  @Override
  public void startup() {
    commEndpoint.startup();
  }

  @Override
  public void shutdown() {
    commEndpoint.shutdown();
  }

  @Override
  public OscClientPacket newOscPacket(String address, String types) {
    return new InteractiveSpacesOscPacket(address, types);
  }

  /**
   * An OSC packet for an {@link InteractiveSpacesOscClientCommunicationsEndpoint}.
   *
   * @author Keith M. Hughes
   */
  public class InteractiveSpacesOscPacket implements OscClientPacket {

    /**
     * OSC Address for the packet
     */
    private String oscAddress;

    /**
     * UDP packet which will be sent to the remote server.
     */
    private UdpPacket packet;

    public InteractiveSpacesOscPacket(String address, String types) {
      packet = commEndpoint.newUdpPacket();

      writeString(address);
      writeArgumentTypes(types);
    }

    @Override
    public String getOscAddress() {
      return oscAddress;
    }

    @Override
    public void write() {
      packet.write(remoteAddress);
    }

    @Override
    public void writeString(String value) {
      byte[] bytes = value.getBytes();
      packet.writeBytes(bytes);
      packet.writeBytes(PADDING, 0, 4 - (bytes.length % 4));
    }

    @Override
    public void writeInt(int value) {
      packet.writeInt(value);
    }

    @Override
    public void writeLong(long value) {
      packet.writeLong(value);
    }

    @Override
    public void writeFloat(float value) {
      packet.writeFloat(value);
    }

    @Override
    public void writeDouble(double value) {
      packet.writeDouble(value);
    }

    /**
     * Write out the argument types.
     *
     * @param types
     *          the argument types
     */
    private void writeArgumentTypes(String types) {
      packet.writeByte(COMMA);
      byte[] bytes = types.getBytes();
      packet.writeBytes(bytes);
      packet.writeBytes(PADDING, 0, 4 - ((bytes.length + 1) % 4));
    }
  }
}
