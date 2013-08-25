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

package interactivespaces.service.comm.network.client.internal.netty;

import com.google.common.collect.Lists;

import interactivespaces.service.comm.network.client.UdpClientNetworkCommunicationEndpoint;
import interactivespaces.service.comm.network.client.UdpClientNetworkCommunicationEndpointListener;
import interactivespaces.service.comm.network.client.UdpPacket;

import org.apache.commons.logging.Log;
import org.jboss.netty.bootstrap.ConnectionlessBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.FixedReceiveBufferSizePredictorFactory;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.socket.DatagramChannel;
import org.jboss.netty.channel.socket.DatagramChannelFactory;
import org.jboss.netty.channel.socket.nio.NioDatagramChannelFactory;

import java.net.InetSocketAddress;
import java.nio.ByteOrder;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * A Netty-based {@link UdpClientNetworkCommunicationEndpoint}.
 *
 * @author Keith M. Hughes
 */
public class NettyUdpClientNetworkCommunicationEndpoint implements
    UdpClientNetworkCommunicationEndpoint {

  /**
   * Default number of bytes in a dynamic buffer
   */
  public static final int DYNAMIC_BUFFER_INITIAL_SIZE = 512;

  /**
   * Byte order for endpoint packets.
   */
  private ByteOrder byteOrder;

  /**
   * The bootstrap for the UDP client.
   */
  private ConnectionlessBootstrap bootstrap;

  /**
   * The channel that data is written on.
   */
  private DatagramChannel outputChannel;

  private List<UdpClientNetworkCommunicationEndpointListener> listeners = Lists
      .newCopyOnWriteArrayList();

  /**
   * Executor service for this endpoint
   */
  private ExecutorService executorService;

  /**
   * Logger for this endpoint.
   */
  private Log log;

  NettyUdpClientNetworkCommunicationEndpoint(ByteOrder byteOrder, ExecutorService executorService,
      Log log) {
    this.byteOrder = byteOrder;
    this.executorService = executorService;
    this.log = log;
  }

  @Override
  public void startup() {
    DatagramChannelFactory channelFactory = new NioDatagramChannelFactory(executorService);

    bootstrap = new ConnectionlessBootstrap(channelFactory);

    // Configure the pipeline factory.
    bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
      public ChannelPipeline getPipeline() throws Exception {
        return Channels.pipeline(new NettyUdpClientNetworkHandler());
      }
    });

    // Enable broadcast
    // bootstrap.setOption("broadcast", "true");

    // Allow packets as large as up to 1024 bytes (default is 768).
    // You could increase or decrease this value to avoid truncated packets
    // or to improve memory footprint respectively.
    //
    // Please also note that a large UDP packet might be truncated or
    // dropped by your router no matter how you configured this option.
    // In UDP, a packet is truncated or dropped if it is larger than a
    // certain size, depending on router configuration. IPv4 routers
    // truncate and IPv6 routers drop a large packet. That's why it is
    // safe to send small packets in UDP.
    bootstrap.setOption("receiveBufferSizePredictorFactory",
        new FixedReceiveBufferSizePredictorFactory(1024));

    outputChannel = (DatagramChannel) bootstrap.bind(new InetSocketAddress(0));
  }

  @Override
  public void shutdown() {
    if (bootstrap != null) {
      bootstrap.shutdown();
      bootstrap = null;
    }
  }

  @Override
  public void write(InetSocketAddress remoteAddress, byte[] bytes) {
    write(remoteAddress, bytes, bytes.length);
  }

  @Override
  public void write(InetSocketAddress remoteAddress, byte[] bytes, int length) {
    write(remoteAddress, bytes, 0, length);
  }

  @Override
  public void write(InetSocketAddress remoteAddress, byte[] bytes, int offset, int length) {
    ChannelBuffer cb = ChannelBuffers.copiedBuffer(bytes, offset, length);
    outputChannel.write(cb, remoteAddress);
  }

  @Override
  public UdpPacket newDynamicUdpPacket() {
    return new NettyUdpPacket(ChannelBuffers.dynamicBuffer(byteOrder, DYNAMIC_BUFFER_INITIAL_SIZE));
  }

  @Override
  public UdpPacket newUdpPacket(int size) {
    return new NettyUdpPacket(ChannelBuffers.buffer(byteOrder, size));
  }

  @Override
  public void addListener(UdpClientNetworkCommunicationEndpointListener listener) {
    listeners.add(listener);
  }

  @Override
  public void removeListener(UdpClientNetworkCommunicationEndpointListener listener) {
    listeners.remove(listener);
  }

  /**
   * Handle a response which has come back from the remote server.
   *
   * @param buffer
   *          the channel buffer for the packet
   * @param remoteAddress
   *          address of the remote response
   */
  private void handleMessageReceived(ChannelBuffer buffer, InetSocketAddress remoteAddress) {
    for (UdpClientNetworkCommunicationEndpointListener listener : listeners) {
      listener.onUdpResponse(this, buffer.array(), remoteAddress);
    }
  }

  /**
   * The handler for the connection.
   *
   * @author Keith M. Hughes
   */
  public class NettyUdpClientNetworkHandler extends SimpleChannelUpstreamHandler {

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
      ChannelBuffer msg = (ChannelBuffer) e.getMessage();

      handleMessageReceived(msg, (InetSocketAddress) e.getRemoteAddress());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
      log.error("Error while handling UDP client message", e.getCause());
      e.getChannel().close();
    }
  }

  class NettyUdpPacket implements UdpPacket {
    /**
     * Buffer for the packet.
     */
    private ChannelBuffer buffer;

    public NettyUdpPacket(ChannelBuffer buffer) {
      this.buffer = buffer;
    }

    @Override
    public void write(InetSocketAddress remoteAddress) {
      outputChannel.write(buffer, remoteAddress);
    }

    @Override
    public void writeByte(int value) {
      buffer.writeByte(value);
    }

    @Override
    public void writeShort(int value) {
      buffer.writeShort(value);
    }

    @Override
    public void writeMedium(int value) {
      buffer.writeMedium(value);
    }

    @Override
    public void writeInt(int value) {
      buffer.writeInt(value);
    }

    @Override
    public void writeLong(long value) {
      buffer.writeLong(value);
    }

    @Override
    public void writeChar(int value) {
      buffer.writeChar(value);
    }

    @Override
    public void writeFloat(float value) {
      buffer.writeFloat(value);
    }

    @Override
    public void writeDouble(double value) {
      buffer.writeDouble(value);
    }

    @Override
    public void writeBytes(byte[] src) {
      buffer.writeBytes(src);
    }

    @Override
    public void writeBytes(byte[] src, int srcIndex, int length) {
      buffer.writeBytes(src, srcIndex, length);
    }

    @Override
    public int getPacketSize() {
      return buffer.readableBytes();
    }
  }
}
