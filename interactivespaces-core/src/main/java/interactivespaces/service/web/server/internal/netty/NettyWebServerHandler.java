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

import static org.jboss.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static org.jboss.netty.handler.codec.http.HttpHeaders.setContentLength;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static org.jboss.netty.handler.codec.http.HttpMethod.GET;
import static org.jboss.netty.handler.codec.http.HttpMethod.HEAD;
import static org.jboss.netty.handler.codec.http.HttpMethod.POST;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.FOUND;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import interactivespaces.InteractiveSpacesExceptionUtils;
import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.service.web.HttpConstants;
import interactivespaces.service.web.server.HttpAuthProvider;
import interactivespaces.service.web.server.HttpAuthResponse;
import interactivespaces.service.web.server.HttpFileUploadListener;
import interactivespaces.service.web.server.WebResourceAccessManager;
import interactivespaces.service.web.server.WebServer;
import interactivespaces.service.web.server.WebServerWebSocketHandlerFactory;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import org.apache.commons.logging.Log;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import org.jboss.netty.handler.codec.http.multipart.HttpDataFactory;
import org.jboss.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import org.jboss.netty.util.CharsetUtil;

import java.io.IOException;
import java.net.HttpCookie;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * A web socket server handler for Netty.
 *
 * <p>
 * This handler must be thread safe.
 *
 * @author Keith M. Hughes
 */
public class NettyWebServerHandler extends SimpleChannelUpstreamHandler {

  /**
   * Factory for HTTP data objects. Used for post events.
   *
   * <p>
   * The factory will keep all in memory until it gets too big, then writes to disk.
   */
  private static final HttpDataFactory HTTP_DATA_FACTORY = new DefaultHttpDataFactory(true);

  /**
   * Exception message when WebSocket connections are closed.
   */
  private static final String MESSAGE_WEB_SOCKET_CLOSED_EXCEPTION = "Connection reset by peer";

  /**
   * List of file names that are not logged as errors if they can't be found.
   */
  private static final Set<String> UNWARNED_MISSING_FILE_NAMES = ImmutableSet.of("favicon.ico");

  /**
   * The web socket path used by this handler.
   */
  private String fullWebSocketUriPrefix;

  /**
   * All GET request handlers handled by this instance.
   */
  private List<NettyHttpGetRequestHandler> httpGetRequestHandlers = Lists.newArrayList();

  /**
   * All POST request handlers handled by this instance.
   */
  private List<NettyHttpPostRequestHandler> httpPostRequestHandlers = Lists.newArrayList();

  /**
   * Map of Netty channel IDs to web socket handlers.
   */
  private Map<Integer, NettyWebServerWebSocketConnection> webSocketConnections = Maps.newConcurrentMap();

  /**
   * Map of Netty channel IDs to file uploads.
   */
  private Map<Integer, NettyHttpFileUpload> fileUploadHandlers = Maps.newConcurrentMap();

  /**
   * Map of host names to the handshaker factory for that host.
   *
   * <p>
   * Not concurrent, so needs to be protected.
   */
  private Map<String, WebSocketServerHandshakerFactory> webSocketHandshakerFactories = Maps.newHashMap();

  /**
   * Factory for web socket handlers.
   *
   * <p>
   * Can be null.
   */
  private WebServerWebSocketHandlerFactory webSocketHandlerFactory;

  /**
   * The listener for file uploads.
   *
   * <p>
   * Can be {@code null}.
   */
  private HttpFileUploadListener fileUploadListener;

  /**
   * The web server we are attached to.
   */
  private NettyWebServer webServer;

  /**
   * The authentication provider.
   */
  private HttpAuthProvider authProvider;

  /**
   * The access manager for determining who gets access to a resource.
   */
  private WebResourceAccessManager accessManager;

  /**
   * The lock for protecting access to channel data.
   */
  private final Object channelLock = new Object();

  /**
   * Construct a new handler.
   *
   * @param webServer
   *          the web server for the handler
   */
  public NettyWebServerHandler(NettyWebServer webServer) {
    this.webServer = webServer;
    this.authProvider = null;
    this.accessManager = null;
  }

  /**
   * Register a new GET request handler to the server.
   *
   * @param handler
   *          the handler to add
   */
  public void addHttpGetRequestHandler(NettyHttpGetRequestHandler handler) {
    httpGetRequestHandlers.add(handler);
  }

  /**
   * Register a new POST request handler to the server.
   *
   * @param handler
   *          the handler to add
   */
  public void addHttpPostRequestHandler(NettyHttpPostRequestHandler handler) {
    httpPostRequestHandlers.add(handler);
  }

  /**
   * Set the factory for creating web socket handlers.
   *
   * @param webSocketUriPrefix
   *          uri prefix for websocket handler, can be {@code null}
   * @param webSocketHandlerFactory
   *          the factory to use, can be {@code null} if don't want to handle web socket calls
   */
  public void setWebSocketHandlerFactory(String webSocketUriPrefix,
      WebServerWebSocketHandlerFactory webSocketHandlerFactory) {
    if (webSocketUriPrefix != null) {
      webSocketUriPrefix = webSocketUriPrefix.trim();
      if (webSocketUriPrefix.startsWith(HttpConstants.URL_PATH_COMPONENT_SEPARATOR)) {
        fullWebSocketUriPrefix = webSocketUriPrefix;
      } else {
        fullWebSocketUriPrefix = HttpConstants.URL_PATH_COMPONENT_SEPARATOR + webSocketUriPrefix;
      }
    } else {
      fullWebSocketUriPrefix = HttpConstants.URL_PATH_COMPONENT_SEPARATOR + WebServer.WEBSOCKET_URI_PREFIX_DEFAULT;
    }
    this.webSocketHandlerFactory = webSocketHandlerFactory;
  }

  /**
   * Set the file upload listener.
   *
   * @param fileUploadListener
   *          the listener to use (can be {@code null})
   */
  public void setHttpFileUploadListener(HttpFileUploadListener fileUploadListener) {
    this.fileUploadListener = fileUploadListener;
  }

  @Override
  public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
    Object msg = e.getMessage();
    if (msg instanceof HttpRequest) {
      handleHttpRequest(ctx, (HttpRequest) msg);
    } else if (msg instanceof WebSocketFrame) {
      handleWebSocketFrame(ctx, (WebSocketFrame) msg);
    } else if (msg instanceof HttpChunk) {
      handleHttpChunk(ctx, (HttpChunk) msg);
    } else {
      webServer.getLog().warn(String.format("Web server received unknown frame %s", msg.getClass().getName()));
    }

  }

  @Override
  public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
    webServer.channelOpened(e.getChannel());
  }

  @Override
  public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
    // No need to tell web server that channel closed, it handles cleanup
    // itself.
    webSocketChannelClosing(e.getChannel());
  }

  /**
   * Handle an HTTP request coming into the server.
   *
   * @param ctx
   *          The channel context for the request.
   * @param req
   *          The HTTP request.
   *
   * @throws Exception
   *           something bad happened
   */
  private void handleHttpRequest(ChannelHandlerContext ctx, HttpRequest req) throws Exception {
    // Before we actually allow handling of this http request, we will check to
    // see if it is properly authorized, if authorization is requested.
    HttpAuthResponse response = null;
    if (authProvider != null) {
      response = authProvider.authorizeRequest(new NettyHttpRequest(req, getWebServer().getLog()));
      if ((response == null) || !response.authSuccessful()) {
        if ((response == null) || response.redirectUrl() != null) {
          sendHttpResponse(ctx, req, createRedirect(response.redirectUrl()), false, false);
        } else {
          webServer.getLog().warn(String.format("Auth requested and no redict available for %s", req.getUri()));
          sendHttpResponse(ctx, req, new DefaultHttpResponse(HTTP_1_1, FORBIDDEN), false, false);
        }
        return;
      }
    }

    String user = null;
    if (response != null) {
      user = response.getUser();
    }

    if (handleWebGetRequest(ctx, req, response)) {
      // The method handled the request if the return value was true.
    } else if (webSocketHandlerFactory != null && tryWebSocketUpgradeRequest(ctx, req, user)) {
      // The method handled the request if the return value was true.
    } else if (handleWebPostRequest(ctx, req, response)) {
      // The method handled the request if the return value was true.
    } else {
      // Nothing we handle.

      HttpResponseStatus status = FORBIDDEN;
      String message = String.format("HTTP [%d] %s --> (No handlers for request)", status.getCode(), req.getUri());
      if (shouldWarnOnMissingFile(new URI(req.getUri()).getPath())) {
        webServer.getLog().warn(message);
      } else {
        webServer.getLog().debug(message);
      }

      sendHttpResponse(ctx, req, new DefaultHttpResponse(HTTP_1_1, status), false, false);
    }
  }

  /**
   * Attempt to handle an HTTP request by scanning through all registered handlers.
   *
   * @param context
   *          the context for the request
   * @param request
   *          the request
   * @param authResponse
   *          the authentication response
   *
   * @return {@code true} if the request was handled
   *
   * @throws IOException
   *           an IO exception happened
   */
  private boolean
      handleWebGetRequest(ChannelHandlerContext context, HttpRequest request, HttpAuthResponse authResponse)
          throws IOException {
    HttpMethod method = request.getMethod();
    if (method == GET || method == HEAD) {
      if (!canUserAccessResource(authResponse, request.getUri())) {
        return false;
      }

      Set<HttpCookie> cookies = null;
      if (authResponse != null) {
        cookies = authResponse.getCookies();
      }

      for (NettyHttpGetRequestHandler handler : httpGetRequestHandlers) {
        if (handler.isHandledBy(request)) {
          try {
            handler.handleWebRequest(context, request, cookies);
          } catch (Exception e) {
            webServer.getLog().error(String.format("Exception when handling web request %s", request.getUri()), e);
          }

          return true;
        }

      }
    }

    return false;
  }

  /**
   * Attempt to handle an HTTP PUT request.
   *
   * @param context
   *          the context for the request
   * @param request
   *          the request
   * @param authResponse
   *          the authentication response
   *
   * @return {@code true} if the request was handled
   *
   * @throws IOException
   *           an IO exception happened
   */
  private boolean handleWebPostRequest(ChannelHandlerContext context, HttpRequest request,
      HttpAuthResponse authResponse) throws IOException {
    if (request.getMethod() != POST) {
      return false;
    }

    NettyHttpPostRequestHandler postRequestHandler = locatePostRequestHandler(request);
    if (postRequestHandler == null && fileUploadListener == null) {
      return false;
    }

    if (!canUserAccessResource(authResponse, request.getUri())) {
      return false;
    }

    Set<HttpCookie> cookies = null;
    if (authResponse != null) {
      cookies = authResponse.getCookies();
    }

    try {
      NettyHttpFileUpload fileUpload =
          new NettyHttpFileUpload(request, new HttpPostRequestDecoder(HTTP_DATA_FACTORY, request), postRequestHandler,
              this, cookies);

      if (request.isChunked()) {
        // Chunked data so more coming.
        fileUploadHandlers.put(context.getChannel().getId(), fileUpload);
      } else {
        fileUpload.completeNonChunked();

        fileUpload.fileUploadComplete(context);
      }
    } catch (Throwable e) {
      webServer.getLog().error("Could not start file upload", e);

      sendError(context, HttpResponseStatus.INTERNAL_SERVER_ERROR);
    }

    return true;
  }

  /**
   * Locate a registered POST request handler that can handle the given request.
   *
   * @param nettyRequest
   *          the Netty request
   *
   * @return the first handler that handles the request, or {@code null} if none
   */
  private NettyHttpPostRequestHandler locatePostRequestHandler(HttpRequest nettyRequest) {
    for (NettyHttpPostRequestHandler handler : httpPostRequestHandlers) {
      if (handler.isHandledBy(nettyRequest)) {
        return handler;
      }
    }

    return null;
  }

  /**
   * Is the HTTP request requesting a Web Socket protocol upgrade?
   *
   * @param context
   *          the request context
   * @param request
   *          the HTTP request
   * @param user
   *          the user making the request
   *
   * @return {@code true} if a Web Socket protocol upgrade
   */
  private boolean tryWebSocketUpgradeRequest(ChannelHandlerContext context, HttpRequest request, final String user) {
    if (!request.getUri().startsWith(fullWebSocketUriPrefix)) {
      return false;
    }

    if (accessManager != null) {
      if (!accessManager.userHasAccess(user, request.getUri())) {
        return false;
      }
    }

    // Handshake
    WebSocketServerHandshakerFactory wsFactory = getWebSocketHandshakerFactory(request);
    final Channel channel = context.getChannel();
    final WebSocketServerHandshaker handshaker = wsFactory.newHandshaker(request);
    if (handshaker == null) {
      wsFactory.sendUnsupportedWebSocketVersionResponse(channel);
    } else {
      ChannelFuture handshake = handshaker.handshake(channel, request);
      handshake.addListener(WebSocketServerHandshaker.HANDSHAKE_LISTENER);
      handshake.addListener(new ChannelFutureListener() {
        @Override
        public void operationComplete(ChannelFuture future) throws Exception {
          completeWebSocketHandshake(user, channel, handshaker);
        }
      });
    }

    // Handled request.
    return true;
  }

  /**
   * Get a handshaker factory for the incoming request.
   *
   * @param req
   *          the request which has come in
   *
   * @return the handshaker factory for the request
   */
  private WebSocketServerHandshakerFactory getWebSocketHandshakerFactory(HttpRequest req) {
    String host = HttpHeaders.getHeader(req, HttpHeaders.Names.HOST);

    synchronized (webSocketHandshakerFactories) {
      WebSocketServerHandshakerFactory wsFactory = webSocketHandshakerFactories.get(host);
      if (wsFactory == null) {
        wsFactory = new WebSocketServerHandshakerFactory(getWebSocketLocation(host), null, false);
        webSocketHandshakerFactories.put(host, wsFactory);
      }

      return wsFactory;
    }
  }

  /**
   * Get the web socket URL.
   *
   * @param host
   *          the host computer the request was made to
   *
   * @return a full web socket url for the given host
   */
  private String getWebSocketLocation(String host) {
    return "ws://" + host + fullWebSocketUriPrefix;
  }

  /**
   * Complete the handshake for a websocket connection.
   *
   * @param user
   *          the user making the web socket connection
   * @param channel
   *          the Netty channel for the connection
   * @param handshaker
   *          the web socket handshaker
   */
  private void completeWebSocketHandshake(String user, Channel channel, WebSocketServerHandshaker handshaker) {
    NettyWebServerWebSocketConnection connection =
        new NettyWebServerWebSocketConnection(channel, user, handshaker, webSocketHandlerFactory, accessManager,
            webServer.getLog());

    synchronized (channelLock) {
      webSocketConnections.put(channel.getId(), connection);
    }

    try {
      connection.getHandler().onConnect();
    } catch (Throwable e) {
      getWebServer().getLog().error(
          "Could not process a websocket onConnect message: " + InteractiveSpacesExceptionUtils.getExceptionDetail(e));
    }
  }

  /**
   * handle a chunk.
   *
   * @param context
   *          the channel event context
   * @param chunk
   *          the chunk frame
   */
  private void handleHttpChunk(ChannelHandlerContext context, HttpChunk chunk) {
    NettyHttpFileUpload fileUpload = fileUploadHandlers.get(context.getChannel().getId());
    if (fileUpload != null) {
      try {
        fileUpload.addChunk(context, chunk);

        if (chunk.isLast()) {
          fileUploadHandlers.remove(context.getChannel().getId());

          fileUpload.fileUploadComplete(context);
        }
      } catch (Throwable e) {
        // An error, so don't leave handler around.
        fileUploadHandlers.remove(context.getChannel().getId());

        webServer.getLog().error("Error while processing a chunk of file upload", e);
        sendError(context, HttpResponseStatus.INTERNAL_SERVER_ERROR);
      }
    } else {
      webServer.getLog().warn("Received HTTP chunk for unknown channel");
    }
  }

  /**
   * Send an HTTP response to the client.
   *
   * @param ctx
   *          the channel event context
   * @param req
   *          the request which has come in
   * @param res
   *          the response which is being written
   * @param setContentLength
   *          {@code true} if should set content length on result
   * @param ignoreKeepAlive
   *          {@code true} if should ignore the HTTP keepalive aheader and close the connection anyway
   */
  public void sendHttpResponse(ChannelHandlerContext ctx, HttpRequest req, HttpResponse res, boolean setContentLength,
      boolean ignoreKeepAlive) {
    try {
      // Generate an error page if response status code is not OK (200).
      if (res.getStatus().getCode() != HttpResponseStatus.OK.getCode()) {
        res.setContent(ChannelBuffers.copiedBuffer(res.getStatus().toString(), CharsetUtil.UTF_8));
        setContentLength(res, res.getContent().readableBytes());
      }

      if (setContentLength) {
        setContentLength(res, res.getContent().readableBytes());
      }
      ChannelFuture f = ctx.getChannel().write(res);

      if ((ignoreKeepAlive || !isKeepAlive(req)) || res.getStatus().getCode() != HttpResponseStatus.OK.getCode()
          || req.getMethod() == POST) {
        f.addListener(ChannelFutureListener.CLOSE);
      }
    } catch (Exception e) {
      webServer.getLog().error("Error while sending HTTP response", e);
    }
  }

  /**
   * Add in the global response headers.
   *
   * @param res
   *          the response to be sent to the client
   */
  private void addGlobalHttpResponseHeaders(HttpResponse res) {
    addHttpResponseHeaderMap(res, webServer.getGlobalHttpContentHeaders());
  }

  /**
   * Add in HTTP response headers.
   *
   * <p>
   * The global headers will be added as well.
   *
   * @param res
   *          the response to be sent to the client
   * @param headers
   *          the headers to add
   */
  public void addHttpResponseHeaders(HttpResponse res, Map<String, String> headers) {
    // The global headers should go in first in case they are being
    // overridden.
    addGlobalHttpResponseHeaders(res);
    addHttpResponseHeaderMap(res, headers);
  }

  /**
   * Add in a response header if there isn't a header with that name already.
   *
   * @param response
   *          the HTTP response
   * @param name
   *          the name of the header
   * @param value
   *          the value of the header
   */
  public void addHeaderIfNotExists(HttpResponse response, String name, Object value) {

    if (HttpHeaders.getHeader(response, name) == null) {
      HttpHeaders.addHeader(response, name, value);
    }
  }

  /**
   * Add in HTTP response headers.
   *
   * <p>
   * The global headers will be added as well.
   *
   * @param res
   *          the response to be sent to the client
   * @param headers
   *          the headers to add
   */
  public void addHttpResponseHeaders(HttpResponse res, Multimap<String, String> headers) {
    // The global headers should go in first in case they are being
    // overridden.
    addGlobalHttpResponseHeaders(res);
    addHttpResponseHeaderMap(res, headers);
  }

  /**
   * Add in HTTP response headers from the given map.
   *
   * @param res
   *          the response to be sent to the client
   * @param headers
   *          the headers to add
   */
  private void addHttpResponseHeaderMap(HttpResponse res, Map<String, String> headers) {
    for (Entry<String, String> entry : headers.entrySet()) {
      HttpHeaders.addHeader(res, entry.getKey(), entry.getValue());
    }
  }

  /**
   * Add in HTTP response headers from the given multimap.
   *
   * @param res
   *          the response to be sent to the client
   * @param headers
   *          the headers to add
   */
  private void addHttpResponseHeaderMap(HttpResponse res, Multimap<String, String> headers) {
    for (String key : headers.keySet()) {
      for (String value : headers.get(key)) {
        HttpHeaders.addHeader(res, key, value);
      }
    }
  }

  /**
   * Send a success response to the client.
   *
   * @param ctx
   *          the channel event context
   * @param req
   *          the request which has come in
   */
  public void sendSuccessHttpResponse(ChannelHandlerContext ctx, HttpRequest req) {
    DefaultHttpResponse res = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
    addGlobalHttpResponseHeaders(res);
    sendHttpResponse(ctx, req, res, false, false);
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
    Channel channel = e.getChannel();
    boolean isWebsocketChannel;
    synchronized (channelLock) {
      isWebsocketChannel = webSocketConnections.containsKey(channel.getId());
    }

    Log log = webServer.getLog();
    Throwable cause = e.getCause();
    if (isWebsocketChannel) {
      log.error("Exception caught in web server for web socket connections: " + cause.getMessage());
    } else {
      log.error("Exception caught in the web server", cause);
    }

    // Can call close many times without negative effect.
    channel.close();
  }

  /**
   * handle a web socket request.
   *
   * @param ctx
   *          The context for the web socket call.
   * @param frame
   *          The web socket frame.
   */
  private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
    Channel channel = ctx.getChannel();

    NettyWebServerWebSocketConnection handler;
    synchronized (channelLock) {
      handler = webSocketConnections.get(channel.getId());
    }
    if (handler != null) {
      handler.handleWebSocketFrame(ctx, frame);
    } else {
      throw new SimpleInteractiveSpacesException("Web socket frame request from unregistered channel");
    }
  }

  /**
   * A web socket channel is closing. Do any necessary cleanup.
   *
   * @param channel
   *          the channel which is closing
   */
  private void webSocketChannelClosing(Channel channel) {
    NettyWebServerWebSocketConnection handler;
    synchronized (channelLock) {
      handler = webSocketConnections.remove(channel.getId());
    }
    if (handler != null) {
      try {
        handler.getHandler().onClose();
      } catch (Throwable e) {
        getWebServer().getLog().error(
            String.format("Error while closing web socket connection: %s",
                InteractiveSpacesExceptionUtils.getExceptionDetail(e)));
      }
    }
  }

  /**
   * Send an error to the remote machine.
   *
   * @param ctx
   *          handler context
   * @param status
   *          the status to send
   */
  public void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
    HttpResponse response = new DefaultHttpResponse(HTTP_1_1, status);
    HttpHeaders.setHeader(response, CONTENT_TYPE, "text/plain; charset=UTF-8");
    response.setContent(ChannelBuffers.copiedBuffer("Failure: " + status.toString() + "\r\n", CharsetUtil.UTF_8));

    // Close the connection as soon as the error message is sent.
    ctx.getChannel().write(response).addListener(ChannelFutureListener.CLOSE);
  }

  /**
   * Create a HTTP redirect response.
   *
   * @param url
   *          the redirection URL
   *
   * @return the redirection response
   */
  private DefaultHttpResponse createRedirect(String url) {
    DefaultHttpResponse response = new DefaultHttpResponse(HTTP_1_1, FOUND);
    HttpHeaders.addHeader(response, HttpHeaders.Names.LOCATION, url);
    return response;
  }

  /**
   * Can a user access a give resource?
   *
   * @param authResponse
   *          the user
   * @param resource
   *          the resource to be accessed
   *
   * @return {@code true} if the user can access the resource
   */
  private boolean canUserAccessResource(HttpAuthResponse authResponse, String resource) {
    // If no access manager is set on this handler, then all users are
    // assumed to have access to all resources.
    return accessManager == null || accessManager.userHasAccess(authResponse.getUser(), resource);
  }

  /**
   * Get the web server the handler is attached to.
   *
   * @return the web server
   */
  public NettyWebServer getWebServer() {
    return webServer;
  }

  /**
   * Set the authentication provider.
   *
   * @param authProvider
   *          the authentication provider to use
   */
  public void setAuthProvider(HttpAuthProvider authProvider) {
    this.authProvider = authProvider;
  }

  /**
   * Set the access manager.
   *
   * @param accessManager
   *          the access manager to use
   */
  public void setAccessManager(WebResourceAccessManager accessManager) {
    this.accessManager = accessManager;
  }

  /**
   * Determines whether a missing file or path should be reported as an warning or not.
   *
   * @param uriPath
   *          the uriPath fragment to test
   *
   * @return {@code true} if the missing file should be logged as a warning
   */
  private boolean shouldWarnOnMissingFile(String uriPath) {
    int pos = uriPath.lastIndexOf(HttpConstants.URL_PATH_COMPONENT_SEPARATOR);
    String filename =
        pos >= 0 ? uriPath.substring(pos + HttpConstants.URL_PATH_COMPONENT_SEPARATOR.length()) : uriPath;
    return !UNWARNED_MISSING_FILE_NAMES.contains(filename);
  }

  /**
   * Get the file upload listener.
   *
   * @return the file upload listener
   */
  public HttpFileUploadListener getHttpFileUploadListener() {
    return fileUploadListener;
  }
}
