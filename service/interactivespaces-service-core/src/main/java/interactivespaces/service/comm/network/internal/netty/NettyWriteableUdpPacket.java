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

package interactivespaces.service.comm.network.internal.netty;

import interactivespaces.service.comm.network.WriteableUdpPacket;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.socket.DatagramChannel;

import java.net.InetSocketAddress;

/**
 * A writeable UDP packet using Netty.
 *
 * @author Keith M. Hughes
 */
public class NettyWriteableUdpPacket implements WriteableUdpPacket {

  /**
   * Default number of bytes in a dynamic buffer.
   */
  public static final int DYNAMIC_BUFFER_INITIAL_SIZE = 512;

  /**
   * The channel that data is written on.
   */
  private DatagramChannel outputChannel;

  /**
   * Buffer for the packet.
   */
  private ChannelBuffer buffer;

  /**
   * Construct a new packet.
   *
   * @param outputChannel
   *          the output channel to be written to
   * @param buffer
   *          the buffer for the data
   */
  public NettyWriteableUdpPacket(DatagramChannel outputChannel, ChannelBuffer buffer) {
    this.outputChannel = outputChannel;
    this.buffer = buffer;
  }

  @Override
  public void write(InetSocketAddress remoteAddress) {
    outputChannel.write(buffer, remoteAddress);
  }

  @Override
  public WriteableUdpPacket writeByte(int value) {
    buffer.writeByte(value);

    return this;
  }

  @Override
  public WriteableUdpPacket writeShort(int value) {
    buffer.writeShort(value);

    return this;
  }

  @Override
  public WriteableUdpPacket writeMedium(int value) {
    buffer.writeMedium(value);

    return this;
  }

  @Override
  public WriteableUdpPacket writeInt(int value) {
    buffer.writeInt(value);

    return this;
  }

  @Override
  public WriteableUdpPacket writeLong(long value) {
    buffer.writeLong(value);

    return this;
  }

  @Override
  public WriteableUdpPacket writeChar(int value) {
    buffer.writeChar(value);

    return this;
  }

  @Override
  public WriteableUdpPacket writeFloat(float value) {
    buffer.writeFloat(value);

    return this;
  }

  @Override
  public WriteableUdpPacket writeDouble(double value) {
    buffer.writeDouble(value);

    return this;
  }

  @Override
  public WriteableUdpPacket writeBytes(byte[] src) {
    buffer.writeBytes(src);

    return this;
  }

  @Override
  public WriteableUdpPacket writeBytes(byte[] src, int srcIndex, int length) {
    buffer.writeBytes(src, srcIndex, length);

    return this;
  }

  @Override
  public int getPacketSize() {
    return buffer.readableBytes();
  }
}
