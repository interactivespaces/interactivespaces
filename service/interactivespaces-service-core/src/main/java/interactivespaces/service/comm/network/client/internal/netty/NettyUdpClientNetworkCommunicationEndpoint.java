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
   * The remote host to write to.
   */
  private String remoteHost;

  /**
   * The remote port to write to.
   */
  private int remotePort;

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

  NettyUdpClientNetworkCommunicationEndpoint(String remoteHost, int remotePort,
      ExecutorService executorService, Log log) {
    this.remoteHost = remoteHost;
    this.remotePort = remotePort;
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
  public void write(byte[] bytes) {
    ChannelBuffer cb = ChannelBuffers.copiedBuffer(bytes);
    outputChannel.write(cb, new InetSocketAddress(remoteHost, remotePort));
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
   */
  private void handleMessageReceived(ChannelBuffer buffer) {
    for (UdpClientNetworkCommunicationEndpointListener listener : listeners) {
      listener.onUdpResponse(this, buffer.array());
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

      handleMessageReceived(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
      log.error("Error while handling UDP client message", e.getCause());
      e.getChannel().close();
    }
  }
}
