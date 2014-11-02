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

package interactivespaces.service.comm.network.server.internal.netty;

import interactivespaces.service.comm.network.server.TcpServerNetworkCommunicationEndpoint;
import interactivespaces.service.comm.network.server.TcpServerNetworkCommunicationEndpointListener;
import interactivespaces.service.comm.network.server.TcpServerRequest;

import com.google.common.collect.Lists;

import org.apache.commons.logging.Log;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.frame.DelimiterBasedFrameDecoder;
import org.jboss.netty.handler.codec.string.StringDecoder;
import org.jboss.netty.handler.codec.string.StringEncoder;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * A Netty-based {@link TcpServerNetworkCommunicationEndpoint} using strings for messaging.
 *
 * @author Keith M. Hughes
 */
public class NettyStringTcpServerNetworkCommunicationEndpoint implements TcpServerNetworkCommunicationEndpoint<String> {

  /**
   * The delimiters for the incoming string messages.
   */
  private final ChannelBuffer[] delimiters;

  /**
   * Charset for the strings.
   */
  private final  Charset charset;

  /**
   * The port the server is listening to.
   */
  private final int serverPort;

  /**
   * The bootstrap for the TCP server.
   */
  private ServerBootstrap bootstrap;

  /**
   * The listeners to endpoint events.
   */
  private final List<TcpServerNetworkCommunicationEndpointListener<String>> listeners = Lists.newCopyOnWriteArrayList();

  /**
   * Executor service for this endpoint.
   */
  private final ExecutorService executorService;

  /**
   * Logger for this endpoint.
   */
  private final Log log;

  /**
   * Construct a new endpoint.
   *
   * @param delimiters
   *          the delimiters for messages
   * @param charset
   *          the character set for messages
   * @param serverPort
   *          the server port to listen on
   * @param executorService
   *          the executor service for threads
   * @param log
   *          the logger to use
   */
  public NettyStringTcpServerNetworkCommunicationEndpoint(ChannelBuffer[] delimiters, Charset charset, int serverPort,
      ExecutorService executorService, Log log) {
    this.delimiters = delimiters;
    this.charset = charset;
    this.serverPort = serverPort;
    this.executorService = executorService;
    this.log = log;
  }

  @Override
  public void startup() {
    // Configure the server.
    bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(executorService, executorService));

    // Set up the pipeline factory.
    bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
      @Override
      public ChannelPipeline getPipeline() throws Exception {
        ChannelPipeline pipeline = Channels.pipeline();

        pipeline.addLast("frameDecoder", new DelimiterBasedFrameDecoder(Integer.MAX_VALUE, delimiters));
        pipeline.addLast("stringDecoder", new StringDecoder(charset));
        pipeline.addLast("stringEncoder", new StringEncoder(charset));
        pipeline.addLast("handler", new NettyTcpServerHandler());

        return pipeline;
      }
    });

    // Bind and start to accept incoming connections.
    bootstrap.bind(new InetSocketAddress(serverPort));
  }

  @Override
  public void shutdown() {
    if (bootstrap != null) {
      bootstrap.shutdown();
      bootstrap = null;
    }
  }

  @Override
  public int getServerPort() {
    return serverPort;
  }

  @Override
  public void addListener(TcpServerNetworkCommunicationEndpointListener<String> listener) {
    listeners.add(listener);
  }

  @Override
  public void removeListener(TcpServerNetworkCommunicationEndpointListener<String> listener) {
    listeners.remove(listener);
  }

  @Override
  public String toString() {
    return "NettyStringTcpServerNetworkCommunicationEndpoint [serverPort=" + serverPort + "]";
  }

  /**
   * Handle the message received by the handler.
   *
   * @param event
   *          the event which happened
   */
  private void handleMessageReceived(MessageEvent event) {
    NettyStringTcpServerRequest request = new NettyStringTcpServerRequest(event);

    for (TcpServerNetworkCommunicationEndpointListener<String> listener : listeners) {
      listener.onTcpRequest(this, request);
    }
  }

  /**
   * Netty handler for incoming TCP requests.
   *
   * @author Keith M. Hughes
   */
  public class NettyTcpServerHandler extends SimpleChannelUpstreamHandler {

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
      handleMessageReceived(e);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
      log.error("Error during netty TCP server handler processing", e.getCause());
    }
  }

  /**
   * Netty-based version of the {@link TcpServerRequest}.
   *
   * @author Keith M. Hughes
   */
  private static class NettyStringTcpServerRequest implements TcpServerRequest<String> {

    /**
     * The message event from the request.
     */
    private final MessageEvent event;

    /**
     * Construct a new request.
     *
     * @param event
     *          the netty message event
     */
    public NettyStringTcpServerRequest(MessageEvent event) {
      this.event = event;
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
      return (InetSocketAddress) event.getRemoteAddress();
    }

    @Override
    public String getMessage() {
      return (String) event.getMessage();
    }

    @Override
    public void writeMessage(String response) {
      event.getChannel().write(response);
    }
  }
}
