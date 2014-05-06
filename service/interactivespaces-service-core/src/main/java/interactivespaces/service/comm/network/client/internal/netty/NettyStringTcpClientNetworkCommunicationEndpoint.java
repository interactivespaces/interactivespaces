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

import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.service.comm.network.client.TcpClientNetworkCommunicationEndpoint;
import interactivespaces.service.comm.network.client.TcpClientNetworkCommunicationEndpointListener;

import com.google.common.collect.Lists;

import org.apache.commons.logging.Log;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.frame.DelimiterBasedFrameDecoder;
import org.jboss.netty.handler.codec.string.StringDecoder;
import org.jboss.netty.handler.codec.string.StringEncoder;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A Netty-based {@link TcpClientNetworkCommunicationEndpoint}.
 *
 * @author Keith M. Hughes
 */
public class NettyStringTcpClientNetworkCommunicationEndpoint implements TcpClientNetworkCommunicationEndpoint<String> {

  /**
   * Default timeout for connecting to the server, in milliseconds.
   */
  public static final long CONNECTION_TIMEOUT_DEFAULT = 1000;

  /**
   * The delimiters for the incoming string messages.
   */
  private final ChannelBuffer[] delimiters;

  /**
   * Charset for the strings.
   */
  private final Charset charset;

  /**
   * The remote host to attach to.
   */
  private final InetAddress remoteHost;

  /**
   * The remote port.
   */
  private final int remotePort;

  /**
   * The bootstrap for the TCP client.
   */
  private ClientBootstrap bootstrap;

  /**
   * The listeners to endpoint events.
   */
  private final List<TcpClientNetworkCommunicationEndpointListener<String>> listeners = Lists.newCopyOnWriteArrayList();

  /**
   * Executor service for this endpoint.
   */
  private final ExecutorService executorService;

  /**
   * Logger for this endpoint.
   */
  private final Log log;

  /**
   * Timeout for connecting to the server, in milliseconds.
   */
  private long connectionTimeout = CONNECTION_TIMEOUT_DEFAULT;

  /**
   * The channel for communicating with the remote server.
   */
  private Channel remoteChannel;

  /**
   * Construct the endpoint.
   *
   * @param delimiters
   *          the delimiters for the end of messages
   * @param charset
   *          the character set for messages
   * @param remoteHost
   *          the remote host to attach to
   * @param remotePort
   *          the port on the remote host to attach to
   * @param executorService
   *          the executor service for threads
   * @param log
   *          the logger to use
   */
  public NettyStringTcpClientNetworkCommunicationEndpoint(ChannelBuffer[] delimiters, Charset charset,
      InetAddress remoteHost, int remotePort, ExecutorService executorService, Log log) {
    this.delimiters = delimiters;
    this.charset = charset;
    this.remoteHost = remoteHost;
    this.remotePort = remotePort;
    this.executorService = executorService;
    this.log = log;
  }

  @Override
  public void startup() {
    // Configure the server.
    bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(executorService, executorService));

    // Set up the pipeline factory.
    bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
      @Override
      public ChannelPipeline getPipeline() throws Exception {
        ChannelPipeline pipeline = Channels.pipeline();

        pipeline.addLast("frameDecoder", new DelimiterBasedFrameDecoder(Integer.MAX_VALUE, delimiters));
        pipeline.addLast("stringDecoder", new StringDecoder(charset));
        pipeline.addLast("stringEncoder", new StringEncoder(charset));
        pipeline.addLast("handler", new NettyTcpClientHandler());

        return pipeline;
      }
    });

    InetSocketAddress addressToConnectTo = new InetSocketAddress(remoteHost, remotePort);
    ChannelFuture cf = bootstrap.connect(addressToConnectTo);
    try {
      cf.await(connectionTimeout, TimeUnit.MILLISECONDS);
      if (cf.isSuccess()) {
        remoteChannel = cf.getChannel();
      } else {
        throw new SimpleInteractiveSpacesException("Could not connect to remote TCP server", cf.getCause());
      }
    } catch (InterruptedException e) {
      throw new SimpleInteractiveSpacesException("The connection to the remote TCP server was interuppted");
    }
  }

  @Override
  public void shutdown() {
    if (bootstrap != null) {
      bootstrap.shutdown();
      bootstrap = null;
    }
  }

  @Override
  public int getRemotePort() {
    return remotePort;
  }

  @Override
  public void addListener(TcpClientNetworkCommunicationEndpointListener<String> listener) {
    listeners.add(listener);
  }

  @Override
  public void removeListener(TcpClientNetworkCommunicationEndpointListener<String> listener) {
    listeners.remove(listener);
  }

  @Override
  public void write(String message) {
    remoteChannel.write(message);
  }

  @Override
  public void setConnectionTimeout(long connectionTimeout) {
    this.connectionTimeout = connectionTimeout;
  }

  /**
   * Handle the message received by the handler.
   *
   * @param event
   *          the event which happened
   */
  private void handleMessageReceived(MessageEvent event) {
    String message = (String) event.getMessage();

    for (TcpClientNetworkCommunicationEndpointListener<String> listener : listeners) {
      listener.onTcpResponse(this, message);
    }
  }

  /**
   * Netty handler for incoming TCP requests.
   *
   * @author Keith M. Hughes
   */
  public class NettyTcpClientHandler extends SimpleChannelUpstreamHandler {

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
      handleMessageReceived(e);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
      log.error("Error during netty TCP client handler processing", e.getCause());
    }
  }
}
