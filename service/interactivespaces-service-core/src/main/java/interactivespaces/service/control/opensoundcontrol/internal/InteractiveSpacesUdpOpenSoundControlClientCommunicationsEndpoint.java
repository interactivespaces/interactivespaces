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

package interactivespaces.service.control.opensoundcontrol.internal;

import interactivespaces.service.comm.network.client.UdpClientNetworkCommunicationEndpoint;
import interactivespaces.service.comm.network.client.UdpPacket;
import interactivespaces.service.control.opensoundcontrol.OpenSoundControlClientCommunicationEndpoint;
import interactivespaces.service.control.opensoundcontrol.OpenSoundControlClientPacket;

import org.apache.commons.logging.Log;

import java.net.InetSocketAddress;

/**
 * An Interactive Spaces implementation of an {@link OpenSoundControlClientCommunicationEndpoint}.
 *
 * @author Keith M. Hughes
 */
public class InteractiveSpacesUdpOpenSoundControlClientCommunicationsEndpoint implements
    OpenSoundControlClientCommunicationEndpoint {

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
  private UdpClientNetworkCommunicationEndpoint udpClientEndpoint;

  /**
   * Log for the connection
   */
  private Log log;

  public InteractiveSpacesUdpOpenSoundControlClientCommunicationsEndpoint(InetSocketAddress remoteAddress,
      UdpClientNetworkCommunicationEndpoint udpClientEndpoint, Log log) {
    this.remoteAddress = remoteAddress;
    this.udpClientEndpoint = udpClientEndpoint;
    this.log = log;
  }

  @Override
  public void startup() {
    udpClientEndpoint.startup();
  }

  @Override
  public void shutdown() {
    udpClientEndpoint.shutdown();
  }

  @Override
  public String getRemoteHost() {
    return remoteAddress.getHostName();
  }

  @Override
  public int getRemotePort() {
    return remoteAddress.getPort();
  }

  @Override
  public OpenSoundControlClientPacket newPacket(String address, String types) {
    return new InteractiveSpacesOscPacket(address, types);
  }

  @Override
  public OpenSoundControlClientPacket newPacket(String address, byte... types) {
    return new InteractiveSpacesOscPacket(address, types);
  }

  @Override
  public String toString() {
    return "InteractiveSpacesUdpOpenSoundControlClientCommunicationsEndpoint [remoteHost=" + getRemoteHost()
        + ", remotePort=" + getRemotePort() + "]";
  }

  /**
   * An OSC packet for an {@link InteractiveSpacesUdpOpenSoundControlClientCommunicationsEndpoint}.
   *
   * @author Keith M. Hughes
   */
  public class InteractiveSpacesOscPacket implements OpenSoundControlClientPacket {

    /**
     * OSC Address for the packet
     */
    private String oscAddress;

    /**
     * UDP packet which will be sent to the remote server.
     */
    private UdpPacket packet;

    /**
     * Construct a new packet.
     *
     * @param address
     *          the OSC address
     * @param types
     *          the types of the arguments
     */
    public InteractiveSpacesOscPacket(String address, String types) {
      this(address, types.getBytes());
    }

    public InteractiveSpacesOscPacket(String address, byte... types) {
      packet = udpClientEndpoint.newDynamicUdpPacket();

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
    private void writeArgumentTypes(byte... types) {
      packet.writeByte(COMMA);
      packet.writeBytes(types);
      packet.writeBytes(PADDING, 0, 4 - ((types.length + 1) % 4));
    }

    @Override
    public int getPacketSize() {
      return packet.getPacketSize();
    }
  }
}
