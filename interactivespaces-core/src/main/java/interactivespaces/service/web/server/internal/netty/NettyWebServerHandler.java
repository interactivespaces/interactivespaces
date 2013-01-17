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
import static org.jboss.netty.handler.codec.http.HttpMethod.POST;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import interactivespaces.service.web.server.HttpFileUploadListener;
import interactivespaces.service.web.server.WebServer;
import interactivespaces.service.web.server.WebServerWebSocketHandlerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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

import com.google.common.collect.Maps;

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
	 * The factory will keep all in memory until it gets too big, then writes to
	 * disk.
	 */
	private static final HttpDataFactory httpDataFactory = new DefaultHttpDataFactory(
			true);

	/**
	 * The web socket path used by this handler.
	 */
	private String fullWebSocketUriPrefix;

	/**
	 * All content handlers handled by this instance.
	 */
	private List<NettyHttpContentHandler> httpContentHandlers = new ArrayList<NettyHttpContentHandler>();

	/**
	 * Map of Netty channel IDs to web socket handlers.
	 */
	private Map<Integer, NettyWebServerWebSocketConnection> webSocketConnections = Maps
			.newConcurrentMap();

	/**
	 * Map of Netty channel IDs to file uploads.
	 */
	private Map<Integer, NettyHttpFileUpload> fileUploadHandlers = Maps
			.newConcurrentMap();

	/**
	 * Map of host names to the handshaker factory for that host.
	 * 
	 * <p>
	 * Not concurrent, so needs to be prote
	 */
	private Map<String, WebSocketServerHandshakerFactory> webSocketHandshakerFactories = Maps
			.newHashMap();

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
	 * Can be null.
	 */
	private HttpFileUploadListener fileUploadListener;

	/**
	 * The web server we are attached to.
	 */
	private NettyWebServer webServer;

	public NettyWebServerHandler(NettyWebServer webServer) {
		this.webServer = webServer;
	}

	/**
	 * Register a new content handler to the server.
	 * 
	 * @param handler
	 */
	public void addHttpContentHandler(NettyHttpContentHandler handler) {
		httpContentHandlers.add(handler);
	}

	/**
	 * Set the factory for creating web socket handlers.
	 * 
	 * @param webSocketUriPrefix
	 *            uri prefix for websocket handler (can be {@code null})
	 * @param webSocketHandlerFactory
	 *            the factory to use (can be {@code null} if don't want to
	 *            handle web socket calls)
	 */
	public void setWebSocketHandlerFactory(String webSocketUriPrefix,
			WebServerWebSocketHandlerFactory webSocketHandlerFactory) {
		this.fullWebSocketUriPrefix = (webSocketUriPrefix != null) ? "/"
				+ webSocketUriPrefix.trim() : "/"
				+ WebServer.WEBSOCKET_URI_PREFIX_DEFAULT;
		this.webSocketHandlerFactory = webSocketHandlerFactory;
	}

	/**
	 * Set the file upload listener.
	 * 
	 * @param fileUploadListener
	 *            the listener to use (can be {@code null})
	 */
	public void setHttpFileUploadListener(
			HttpFileUploadListener fileUploadListener) {
		this.fileUploadListener = fileUploadListener;
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		Object msg = e.getMessage();
		if (msg instanceof HttpRequest) {
			handleHttpRequest(ctx, (HttpRequest) msg);
		} else if (msg instanceof WebSocketFrame) {
			handleWebSocketFrame(ctx, (WebSocketFrame) msg);
		} else if (msg instanceof HttpChunk) {
			handleHttpChunk(ctx, (HttpChunk) msg);
		} else {
			webServer.getLog().warn(
					String.format("Web server received unknown frame %s", msg
							.getClass().getName()));
		}

	}

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e)
			throws Exception {
		webServer.channelOpened(e.getChannel());
	}

	@Override
	public void channelDisconnected(ChannelHandlerContext ctx,
			ChannelStateEvent e) throws Exception {
		// No need to tell web server that channel closed, it handles cleanup
		// itself.
		webSocketChannelClosing(e.getChannel());
	}

	/**
	 * Handle an HTTP request coming into the server.
	 * 
	 * @param ctx
	 *            The channel context for the request.
	 * @param req
	 *            The HTTP request.
	 * 
	 * @throws Exception
	 */
	private void handleHttpRequest(ChannelHandlerContext ctx, HttpRequest req)
			throws Exception {
		if (handleWebGetRequest(ctx, req)) {
			// The method handled the request if the return value was true.
		} else if (webSocketHandlerFactory != null
				&& tryWebSocketUpgradeRequest(ctx, req)) {
			// The method handled the request if the return value was true.
		} else if (handleWebPutRequest(ctx, req)) {
			// The method handled the request if the return value was true.
		} else {
			// Nothing we handle.
			webServer.getLog().warn(
					String.format("Web server has no handlers for request %s",
							req.getUri()));

			sendHttpResponse(ctx, req, new DefaultHttpResponse(HTTP_1_1,
					FORBIDDEN));
		}
	}

	/**
	 * Attempt to handle an HTTP request by scanning through all registered
	 * handlers.
	 * 
	 * @param ctx
	 *            The context for the request.
	 * @param req
	 *            The request.
	 * @return True if the request was handled, false otherwise.
	 */
	private boolean handleWebGetRequest(ChannelHandlerContext ctx,
			HttpRequest req) throws IOException {
		if (req.getMethod() == GET) {
			for (NettyHttpContentHandler handler : httpContentHandlers) {
				if (handler.isHandledBy(req)) {
					try {
						handler.handleWebRequest(ctx, req);
					} catch (Exception e) {
						webServer
								.getLog()
								.error(String.format(
										"Exception when handling web request %s",
										req.getUri()), e);
					}

					return true;
				}

			}
		}

		return false;
	}

	/**
	 * Attempt to handle an HTTP PUT request
	 * 
	 * @param ctx
	 *            The context for the request.
	 * @param req
	 *            The request.
	 * @return True if the request was handled, false otherwise.
	 */
	private boolean handleWebPutRequest(ChannelHandlerContext ctx,
			HttpRequest req) throws IOException {
		if (req.getMethod() != POST || fileUploadListener == null) {
			return false;
		}

		try {
			NettyHttpFileUpload fileUpload = new NettyHttpFileUpload(req,
					new HttpPostRequestDecoder(httpDataFactory, req), webServer);

			if (req.isChunked()) {
				// Chunked data so more coming.
				fileUploadHandlers.put(ctx.getChannel().getId(), fileUpload);
			} else {
				fileUpload.completeNonChunked();
				fileUploadComplete(fileUpload);
				sendSuccessHttpResponse(ctx, req);
			}
		} catch (Exception e) {
			webServer.getLog().error("Could not start file upload", e);

			sendError(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);
		}

		return true;
	}

	/**
	 * Is the HTTP request requesting a Web Socket protocol upgrade?
	 * 
	 * @param req
	 *            The HTTP request.
	 * 
	 * @return True if a Web Socket protocol upgrade, false otherwise.
	 */
	private boolean tryWebSocketUpgradeRequest(final ChannelHandlerContext ctx,
			HttpRequest req) {
		if (!req.getUri().equals(fullWebSocketUriPrefix)) {
			return false;
		}

		// Handshake
		WebSocketServerHandshakerFactory wsFactory = getWebSocketHandshakerFactory(req);
		final Channel channel = ctx.getChannel();
		final WebSocketServerHandshaker handshaker = wsFactory
				.newHandshaker(req);
		if (handshaker == null) {
			wsFactory.sendUnsupportedWebSocketVersionResponse(channel);
		} else {
			ChannelFuture handshake = handshaker.handshake(channel, req);
			handshake.addListener(WebSocketServerHandshaker.HANDSHAKE_LISTENER);
			handshake.addListener(new ChannelFutureListener() {
				@Override
				public void operationComplete(ChannelFuture arg0)
						throws Exception {
					NettyWebServerWebSocketConnection connection = new NettyWebServerWebSocketConnection(
							channel, handshaker, webSocketHandlerFactory,
							webServer.getLog());
					webSocketConnections.put(channel.getId(), connection);

					connection.getHandler().onConnect();
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
	 *            the request which has come in
	 * 
	 * @return the handshaker factory for the request
	 */
	private WebSocketServerHandshakerFactory getWebSocketHandshakerFactory(
			HttpRequest req) {
		String host = req.getHeader(HttpHeaders.Names.HOST);

		synchronized (webSocketHandshakerFactories) {
			WebSocketServerHandshakerFactory wsFactory = webSocketHandshakerFactories
					.get(host);
			if (wsFactory == null) {
				wsFactory = new WebSocketServerHandshakerFactory(
						getWebSocketLocation(host), null, false);
				webSocketHandshakerFactories.put(host, wsFactory);
			}

			return wsFactory;
		}
	}

	/**
	 * Get the web socket URL.
	 * 
	 * @param host
	 *            the host computer the request was made to
	 * 
	 * @return a full web socket url for the given host
	 */
	private String getWebSocketLocation(String host) {
		return "ws://" + host + fullWebSocketUriPrefix;
	}

	/**
	 * handle a chunk.
	 * 
	 * @param ctx
	 *            the channel event context
	 * @param chunk
	 *            the chunk frame
	 */
	private void handleHttpChunk(ChannelHandlerContext ctx, HttpChunk chunk) {
		NettyHttpFileUpload fileUpload = fileUploadHandlers.get(ctx
				.getChannel().getId());
		if (fileUpload != null) {
			try {
				fileUpload.addChunk(ctx, chunk);

				if (chunk.isLast()) {
					fileUploadHandlers.remove(ctx.getChannel().getId());
					fileUploadComplete(fileUpload);
					sendSuccessHttpResponse(ctx, fileUpload.getRequest());
				}
			} catch (Exception e) {
				// An error, so don't leave handler around.
				fileUploadHandlers.remove(ctx.getChannel().getId());

				webServer.getLog().error(
						"Error while processing a chunk of file upload", e);
				sendError(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);
			}
		} else {
			webServer.getLog().warn("Received HTTP chunk for unknown channel");
		}
	}

	/**
	 * The file upload is complete. Finish processing and alert everyone who
	 * needs to know.
	 * 
	 * @param fileUpload
	 *            the completed file upload object
	 */
	private void fileUploadComplete(NettyHttpFileUpload fileUpload) {
		try {
			fileUploadListener.handleHttpFileUpload(fileUpload);
		} catch (Exception e) {
			webServer.getLog().error(
					"Error while calling file upload listener", e);
		}

		fileUpload.clean();
	}

	/**
	 * Send an HTTP response to the client.
	 * 
	 * @param ctx
	 *            the channel event context
	 * @param req
	 *            the request which has come in
	 * @param res
	 *            the response which is being written
	 */
	public void sendHttpResponse(ChannelHandlerContext ctx, HttpRequest req,
			HttpResponse res) {
		// Generate an error page if response status code is not OK (200).
		if (res.getStatus().getCode() != HttpResponseStatus.OK.getCode()) {
			res.setContent(ChannelBuffers.copiedBuffer(res.getStatus()
					.toString(), CharsetUtil.UTF_8));
			setContentLength(res, res.getContent().readableBytes());
		}

		// Send the response and close the connection if necessary.
		ChannelFuture f = ctx.getChannel().write(res);

		if (!isKeepAlive(req)
				|| res.getStatus().getCode() != HttpResponseStatus.OK.getCode()
				|| req.getMethod() == POST) {
			f.addListener(ChannelFutureListener.CLOSE);
		}
	}

	/**
	 * Add in the global response headers.
	 * 
	 * @param res
	 *            the response to be sent to the client
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
	 *            the response to be sent to the client
	 * @param headers
	 *            the headers to add
	 */
	public void addHttpResponseHeaders(HttpResponse res,
			Map<String, String> headers) {
		// The global headers should go in first in case they are being overridden.
		addGlobalHttpResponseHeaders(res);
		addHttpResponseHeaderMap(res, headers);
	}

	/**
	 * Add in HTTP response headers from the given map.
	 * 
	 * @param res
	 *            the response to be sent to the client
	 * @param headers
	 *            the headers to add
	 */
	private void addHttpResponseHeaderMap(HttpResponse res,
			Map<String, String> headers) {
		for (Entry<String, String> entry : headers.entrySet()) {
			res.addHeader(entry.getKey(), entry.getValue());
		}
	}

	/**
	 * Send a success response to the client.
	 * 
	 * @param ctx
	 *            the channel event context
	 * @param req
	 *            the request which has come in
	 */
	private void sendSuccessHttpResponse(ChannelHandlerContext ctx,
			HttpRequest req) {
		DefaultHttpResponse res = new DefaultHttpResponse(
						HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
		addGlobalHttpResponseHeaders(res);
		sendHttpResponse(ctx, req, res);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
			throws Exception {
		webServer.getLog().error("Exception caught in the web server",
				e.getCause());
		e.getChannel().close();
	}

	/**
	 * handle a web socket request.
	 * 
	 * @param ctx
	 *            The context for the web socket call.
	 * @param frame
	 *            The web socket frame.
	 */
	private void handleWebSocketFrame(ChannelHandlerContext ctx,
			WebSocketFrame frame) {
		Channel channel = ctx.getChannel();

		NettyWebServerWebSocketConnection handler = webSocketConnections
				.get(channel.getId());
		if (handler != null) {
			handler.handleWebSocketFrame(ctx, frame);
		} else {

			throw new RuntimeException(
					"Web socket frame request from unregistered channel");
		}
	}

	/**
	 * A web socket channel is closing. Do any necessary cleanup.
	 * 
	 * @param channel
	 */
	private void webSocketChannelClosing(Channel channel) {
		NettyWebServerWebSocketConnection handler = webSocketConnections
				.get(channel.getId());
		if (handler != null) {
			// Is a web socket handler. Remove it and tell the handler it is
			// done.
			webSocketConnections.remove(channel.getId());
			handler.getHandler().onClose();
		}
	}

	/**
	 * Send an error to the remote machine.
	 * 
	 * @param ctx
	 *            handler context
	 * @param status
	 *            the status to send
	 */
	public void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
		HttpResponse response = new DefaultHttpResponse(HTTP_1_1, status);
		response.setHeader(CONTENT_TYPE, "text/plain; charset=UTF-8");
		response.setContent(ChannelBuffers.copiedBuffer(
				"Failure: " + status.toString() + "\r\n", CharsetUtil.UTF_8));

		// Close the connection as soon as the error message is sent.
		ctx.getChannel().write(response)
				.addListener(ChannelFutureListener.CLOSE);
	}

	/**
	 * Set the URI prefix to be used for web socket connections.
	 * 
	 * @return the webSocketPath
	 */
	public void setWebSocketUriPrefix() {
		// return webSocketPath;
	}

	/**
	 * Get the web server the handler is attached to.
	 * 
	 * @return the web server
	 */
	public NettyWebServer getWebServer() {
		return webServer;
	}
}
