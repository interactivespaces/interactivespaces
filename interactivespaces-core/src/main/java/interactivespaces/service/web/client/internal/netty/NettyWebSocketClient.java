/*
 * Copyright (C) 2012 Google Inc.
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

package interactivespaces.service.web.client.internal.netty;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.service.web.WebSocketHandler;
import interactivespaces.service.web.client.WebSocketClient;
import interactivespaces.util.data.json.JsonMapper;
import interactivespaces.util.data.json.StandardJsonMapper;

import org.apache.commons.logging.Log;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioClientBossPool;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioWorkerPool;
import org.jboss.netty.handler.codec.http.HttpRequestEncoder;
import org.jboss.netty.handler.codec.http.HttpResponseDecoder;
import org.jboss.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketVersion;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.concurrent.Executor;

/**
 * A {@link WebSocketClient} using Netty.
 *
 * @author Keith M. Hughes
 */
public class NettyWebSocketClient implements WebSocketClient {

  /**
   * The Netty Channel Pipeline name for the web socket handler.
   */
  public static final String CHANNEL_PIPELINE_NAME_WEBSOCKET_HANDLER = "ws-handler";

  /**
   * The Netty Channel Pipeline name for the HTTP encoder.
   */
  public static final String CHANNEL_PIPELINE_NAME_ENCODER = "encoder";

  /**
   * The Netty Channel Pipeline name for the HTTP decoder.
   */
  public static final String CHANNEL_PIPELINE_NAME_DECODER = "decoder";

  /**
   * The message sent for a web socket ping message.
   */
  public static final byte[] WEBSOCKET_PING_MESSAGE = new byte[] { 1, 2, 3, 4, 5, 6 };

  /**
   * The JSON mapper.
   */
  private static final JsonMapper MAPPER = StandardJsonMapper.INSTANCE;

  /**
   * The connection URI.
   */
  private final URI uri;

  /**
   * The handler for messages.
   */
  private WebSocketHandler handler;

  /**
   * The threadpool to use for the connection.
   */
  private final Executor threadPool;

  /**
   * Logger for the client.
   */
  private final Log log;

  /**
   * The netty channel for writing web socket data.
   */
  private Channel channel;

  /**
   * Bootstrap for the connections.
   */
  private ClientBootstrap bootstrap;

  /**
   * Construct a Netty websocket client.
   *
   * @param uri
   *          URI for the websocket server connection
   * @param handler
   *          the handler for the client
   * @param threadPool
   *          the threadpool to use
   * @param log
   *          the log to use
   */
  public NettyWebSocketClient(URI uri, WebSocketHandler handler, Executor threadPool, Log log) {
    this.uri = uri;
    this.handler = handler;
    this.threadPool = threadPool;
    this.log = log;
  }

  @Override
  public void startup() {
    if (handler == null) {
      throw new SimpleInteractiveSpacesException("The web socket client does not have a handler at startup");
    }

    bootstrap =
        new ClientBootstrap(new NioClientSocketChannelFactory(new NioClientBossPool(threadPool, 1), new NioWorkerPool(
            threadPool, Runtime.getRuntime().availableProcessors() * 2)));

    try {
      // Make sure the client socket doesn't stick around too long.
      bootstrap.setOption("soLinger", 0);

      String protocol = uri.getScheme();
      if (!protocol.equals("ws")) {
        throw new IllegalArgumentException("Unsupported protocol: " + protocol);
      }

      // Connect with V13 (RFC 6455 aka HyBi-17). You can change it to V08
      // or V00. If you change it to V00, ping is not supported and remember to
      // change HttpResponseDecoder to WebSocketHttpResponseDecoder in the pipeline.
      final WebSocketClientHandshaker handshaker =
          new WebSocketClientHandshakerFactory().newHandshaker(uri, WebSocketVersion.V13, null, false, null);

      bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
        @Override
        public ChannelPipeline getPipeline() throws Exception {
          ChannelPipeline pipeline = Channels.pipeline();

          pipeline.addLast(CHANNEL_PIPELINE_NAME_DECODER, new HttpResponseDecoder());
          pipeline.addLast(CHANNEL_PIPELINE_NAME_ENCODER, new HttpRequestEncoder());
          pipeline.addLast(CHANNEL_PIPELINE_NAME_WEBSOCKET_HANDLER, new NettyWebSocketClientHandler(handshaker,
              handler, log));
          return pipeline;
        }
      });

      ChannelFuture future = bootstrap.connect(new InetSocketAddress(uri.getHost(), uri.getPort()));
      future.syncUninterruptibly();

      channel = future.getChannel();
      ChannelFuture handshake = handshaker.handshake(channel);
      handshake.syncUninterruptibly();
    } catch (Exception e) {
      channel = null;

      throw new InteractiveSpacesException("Could not connect to web socket connection", e);
    }
  }

  @Override
  public void setWebSocketHandler(WebSocketHandler handler) {
    if (bootstrap == null) {
      throw new SimpleInteractiveSpacesException("The web socket client is already running");
    }

    this.handler = handler;
  }

  @Override
  public void writeDataAsJson(Object data) {
    try {
      channel.write(new TextWebSocketFrame(MAPPER.toString(data)));
    } catch (Exception e) {
      throw new InteractiveSpacesException("Could not write web socket client data", e);
    }
  }

  @Override
  public void writeDataAsString(String data) {
    try {
      channel.write(new TextWebSocketFrame(data));
    } catch (Exception e) {
      throw new InteractiveSpacesException("Could not write web socket client data", e);
    }
  }

  @Override
  public void ping() {
    if (channel != null) {
      channel.write(new PingWebSocketFrame(ChannelBuffers.copiedBuffer(WEBSOCKET_PING_MESSAGE)));
    }
  }

  @Override
  public boolean isOpen() {
    return channel != null && channel.isConnected();
  }

  @Override
  public void shutdown() {
    if (channel != null) {
      try {
        channel.write(new CloseWebSocketFrame());

        // WebSocketClientHandler will close the connection when the
        // server responds to the CloseWebSocketFrame.
        channel.getCloseFuture().awaitUninterruptibly();
      } finally {
        channel.close();
        channel = null;
      }

      bootstrap.shutdown();
      bootstrap = null;
    }
  }

  @Override
  public String getUser() {
    return "";
  }

  @Override
  public String toString() {
    return "NettyWebSocketClient [uri=" + uri + "]";
  }
}
