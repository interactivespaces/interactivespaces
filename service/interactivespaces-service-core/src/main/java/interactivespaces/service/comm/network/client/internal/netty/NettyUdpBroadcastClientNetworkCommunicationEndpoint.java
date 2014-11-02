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

import interactivespaces.service.comm.network.client.UdpBroadcastClientNetworkCommunicationEndpoint;
import interactivespaces.service.comm.network.client.UdpBroadcastClientNetworkCommunicationEndpointListener;

import com.google.common.collect.Lists;

import org.apache.commons.logging.Log;
import org.jboss.netty.bootstrap.ConnectionlessBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
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

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * A Netty implementation of a
 * {@link UdpBroadcastClientNetworkCommunicationEndpoint}.
 *
 * @author Keith M. Hughes
 */
public class NettyUdpBroadcastClientNetworkCommunicationEndpoint implements
    UdpBroadcastClientNetworkCommunicationEndpoint {

  /**
   * Max size for UDP packets in bytes.
   */
  public static final int POTENTIAL_UDP_PACKET_MAXIMUM = 1024;

  /**
   * The port the client is listening to.
   */
  private final int port;

  /**
   * The bootstrap for the UDP client.
   */
  private ConnectionlessBootstrap bootstrap;

  /**
   * The listeners to endpoint events.
   */
  private final List<UdpBroadcastClientNetworkCommunicationEndpointListener> listeners = Lists
      .newCopyOnWriteArrayList();

  /**
   * Executor service for this endpoint.
   */
  private final ExecutorService executorService;

  /**
   * Logger for this endpoint.
   */
  private final Log log;

  /**
   * The datagram channel for this endpoint.
   */
  private DatagramChannel channel;

  /**
   * Construct a netty UDP broadcast client.
   *
   * @param port
   *          the broadcast port
   * @param executorService
   *          the executor service to use
   * @param log
   *          the logger to use
   */
  public NettyUdpBroadcastClientNetworkCommunicationEndpoint(int port, ExecutorService executorService, Log log) {
    this.port = port;
    this.executorService = executorService;
    this.log = log;
  }

  @Override
  public void startup() {
    DatagramChannelFactory channelFactory = new NioDatagramChannelFactory(executorService);
    bootstrap = new ConnectionlessBootstrap(channelFactory);

    // Configure the pipeline factory.
    bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
      @Override
      public ChannelPipeline getPipeline() throws Exception {
        return Channels.pipeline(new NettyUdpBroadcastHandler());
      }
    });

    // Enable broadcast
    bootstrap.setOption("broadcast", "true");

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
    bootstrap.setOption("receiveBufferSizePredictorFactory", new FixedReceiveBufferSizePredictorFactory(
        POTENTIAL_UDP_PACKET_MAXIMUM));

    channel = (DatagramChannel) bootstrap.bind(new InetSocketAddress(port));
  }

  @Override
  public void shutdown() {
    if (bootstrap != null) {
      bootstrap.shutdown();
      bootstrap = null;
    }
  }

  @Override
  public int getPort() {
    return port;
  }

  @Override
  public void addListener(UdpBroadcastClientNetworkCommunicationEndpointListener listener) {
    listeners.add(listener);
  }

  @Override
  public void removeListener(UdpBroadcastClientNetworkCommunicationEndpointListener listener) {
    listeners.remove(listener);
  }

  @Override
  public void joinGroup(InetAddress multicastAddress) {
    channel.joinGroup(multicastAddress);
  }

  @Override
  public void joinGroup(InetSocketAddress multicastAddress, NetworkInterface networkInterface) {
    channel.joinGroup(multicastAddress, networkInterface);
  }

  @Override
  public void leaveGroup(InetAddress multicastAddress) {
    channel.leaveGroup(multicastAddress);
  }

  @Override
  public void leaveGroup(InetSocketAddress multicastAddress, NetworkInterface networkInterface) {
    channel.leaveGroup(multicastAddress, networkInterface);
  }

  @Override
  public String toString() {
    return "NettyUdpBroadcastClientNetworkCommunicationEndpoint [port=" + port + "]";
  }

  /**
   * Handle the message received by the handler.
   *
   * @param event
   *          the event which happened
   */
  private void handleMessageReceived(MessageEvent event) {
    byte[] message = ((ChannelBuffer) event.getMessage()).array();
    InetSocketAddress remoteAddress = (InetSocketAddress) event.getRemoteAddress();
    for (UdpBroadcastClientNetworkCommunicationEndpointListener listener : listeners) {
      listener.onUdpMessage(this, message, remoteAddress);
    }
  }

  /**
   * Netty handler for incoming UDP broadcast packets.
   *
   * @author Keith M. Hughes
   */
  public class NettyUdpBroadcastHandler extends SimpleChannelUpstreamHandler {

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
      handleMessageReceived(e);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
      log.error("Error during netty UDP broadcast handler processing", e.getCause());
    }
  }
}
