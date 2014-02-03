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

package interactivespaces.service.web.server.internal.netty;

import static org.jboss.netty.channel.Channels.pipeline;

import com.google.common.collect.Maps;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.service.web.server.HttpAuthProvider;
import interactivespaces.service.web.server.HttpDynamicRequestHandler;
import interactivespaces.service.web.server.HttpFileUploadListener;
import interactivespaces.service.web.server.WebResourceAccessManager;
import interactivespaces.service.web.server.WebServer;
import interactivespaces.service.web.server.WebServerWebSocketHandlerFactory;

import org.apache.commons.logging.Log;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

/**
 * A web server based on Netty.
 *
 * @author Keith M. Hughes
 */
public class NettyWebServer implements WebServer {

  /**
   * Name of the server.
   */
  private String serverName;

  /**
   * Port the server should run on.
   */
  private int port;

  /**
   * The server handler for requests.
   */
  private NettyWebServerHandler serverHandler;

  /**
   * Server handler for web sockets.
   */

  private Channel serverChannel;

  /**
   * All channels we know about in the server.
   */
  private ChannelGroup allChannels;

  /**
   * Factory for all channels coming into the server.
   */
  private NioServerSocketChannelFactory channelFactory;

  /**
   * Threadpool for the boss threads.
   */
  private ScheduledExecutorService bossThreadPool;

  /**
   * Threadpool for the worker threads.
   */
  private ScheduledExecutorService workerThreadPool;

  /**
   * HTTP headers to be sent on all responses.
   */
  private Map<String, String> globalHttpContentHeaders = Maps.newHashMap();

  /**
   * Logger for the web server.
   */
  private Log log;

  /**
   * Bootstrap for the server.
   */
  private ServerBootstrap bootstrap;

  /**
   * Create a web server using a singular thread pool.
   *
   * @param serverName
   *          name for the server
   * @param port
   *          port to listen on
   * @param threadPool
   *          thread pool to use
   * @param log
   *          logger
   */
  public NettyWebServer(String serverName, int port, ScheduledExecutorService threadPool, Log log) {
    this(serverName, port, threadPool, threadPool, log);
  }

  /**
   * Create a server with differentiated thread pools.
   *
   * @param serverName
   *          name for the server
   * @param port
   *          port to listen on
   * @param bossThreadPool
   *          thread pool to use for boss threads
   * @param workerThreadPool
   *          thread pool to use for workers
   * @param log
   *          logger
   */
  public NettyWebServer(String serverName, int port, ScheduledExecutorService bossThreadPool,
      ScheduledExecutorService workerThreadPool, Log log) {
    this.serverName = serverName;
    this.port = port;
    this.bossThreadPool = bossThreadPool;
    this.workerThreadPool = workerThreadPool;
    this.log = log;

    serverHandler = new NettyWebServerHandler(this);
  }

  @Override
  public void startup() {

    allChannels = new DefaultChannelGroup(serverName);

    channelFactory = new NioServerSocketChannelFactory(bossThreadPool, workerThreadPool);

    bootstrap = new ServerBootstrap(channelFactory);

    // Set up the event pipeline factory.
    bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
      public ChannelPipeline getPipeline() throws Exception {
        // Create a default pipeline implementation.
        ChannelPipeline pipeline = pipeline();
        pipeline.addLast("decoder", new HttpRequestDecoder());
        // pipeline.addLast("aggregator", new
        // HttpChunkAggregator(4615604));
        pipeline.addLast("encoder", new HttpResponseEncoder());
        pipeline.addLast("handler", serverHandler);

        return pipeline;
      }
    });

    serverChannel = bootstrap.bind(new InetSocketAddress(port));
    allChannels.add(serverChannel);
  }

  @Override
  public void shutdown() {
    if (allChannels != null) {
      ChannelGroupFuture future = allChannels.close();
      future.awaitUninterruptibly();

      channelFactory = null;
      allChannels = null;

      bootstrap.shutdown();
      bootstrap = null;
    }
  }

  @Override
  public void addStaticContentHandler(String uriPrefix, File baseDir) {
    addStaticContentHandler(uriPrefix, baseDir, null);
  }

  @Override
  public void addStaticContentHandler(String uriPrefix, File baseDir,
      Map<String, String> extraHttpContentHeaders) {
    addStaticContentHandler(uriPrefix, baseDir, extraHttpContentHeaders, null);
  }

  @Override
  public void addStaticContentHandler(String uriPrefix, File baseDir, Map<String, String> extraHttpContentHeaders,
      HttpDynamicRequestHandler fallbackHandler) {
    if (!baseDir.exists()) {
      throw new InteractiveSpacesException(String.format("Cannot find web folder %s",
          baseDir.getAbsolutePath()));
    }

    NettyHttpDynamicRequestHandlerHandler fallbackNettyHandler = fallbackHandler == null ? null
        : new NettyHttpDynamicRequestHandlerHandler(serverHandler, uriPrefix, false,
            fallbackHandler, extraHttpContentHeaders);

    serverHandler.addHttpContentHandler(new NettyStaticContentHandler(serverHandler, uriPrefix,
        baseDir, extraHttpContentHeaders, fallbackNettyHandler));
  }

  @Override
  public void addDynamicContentHandler(String uriPrefix, boolean usePath,
      HttpDynamicRequestHandler handler) {
    addDynamicContentHandler(uriPrefix, usePath, handler, null);
  }

  @Override
  public void addDynamicContentHandler(String uriPrefix, boolean usePath,
      HttpDynamicRequestHandler handler, Map<String, String> extraHttpContentHeaders) {
    serverHandler.addHttpContentHandler(new NettyHttpDynamicRequestHandlerHandler(serverHandler,
        uriPrefix, usePath, handler, extraHttpContentHeaders));
  }

  @Override
  public void setWebSocketHandlerFactory(String webSocketUriPrefix,
      WebServerWebSocketHandlerFactory webSocketHandlerFactory) {
    serverHandler.setWebSocketHandlerFactory(webSocketUriPrefix, webSocketHandlerFactory);
  }

  @Override
  public void setHttpFileUploadListener(HttpFileUploadListener listener) {
    serverHandler.setHttpFileUploadListener(listener);
  }

  @Override
  public String getServerName() {
    return serverName;
  }

  @Override
  public int getPort() {
    return port;
  }

  @Override
  public void addContentHeader(String name, String value) {
    globalHttpContentHeaders.put(name, value);
  }

  @Override
  public void addContentHeaders(Map<String, String> headers) {
    globalHttpContentHeaders.putAll(headers);
  }

  /**
   * Get the worker thread pool.
   *
   * @return thread pool to use for worker threads
   */
  public ExecutorService getWorkerThreadPool() {
    return workerThreadPool;
  }

  /**
   * A new channel was opened. Register it so it can be properly shut down.
   *
   * @param channel channel that has been opened
   */
  public void channelOpened(Channel channel) {
    allChannels.add(channel);
  }

  /**
   * Get the content headers which should go onto every HTTP response.
   *
   * @return the globalHttpContentHeaders
   */
  public Map<String, String> getGlobalHttpContentHeaders() {
    return globalHttpContentHeaders;
  }

  /**
   * Get the web server's logger.
   *
   * @return the logger
   */
  public Log getLog() {
    return log;
  }

  @Override
  public void setAuthProvider(HttpAuthProvider authProvider) {
    serverHandler.setAuthProvider(authProvider);

  }

  @Override
  public void setAccessManager(WebResourceAccessManager accessManager) {
    serverHandler.setAccessManager(accessManager);
  }
}
