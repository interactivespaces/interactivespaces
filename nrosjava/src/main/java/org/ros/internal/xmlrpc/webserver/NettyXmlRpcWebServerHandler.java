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

package org.ros.internal.xmlrpc.webserver;

import static org.jboss.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static org.jboss.netty.handler.codec.http.HttpHeaders.setContentLength;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static org.jboss.netty.handler.codec.http.HttpMethod.POST;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

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
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.util.CharsetUtil;

import java.io.IOException;

/**
 * Web server handler for the Netty XMLRPC handler
 *
 * @author Keith M. Hughes
 */
public class NettyXmlRpcWebServerHandler extends SimpleChannelUpstreamHandler {

  /**
   * The web server this handler is attached to
   */
  private NettyXmlRpcWebServer webServer;

  public NettyXmlRpcWebServerHandler(NettyXmlRpcWebServer webServer) {
    this.webServer = webServer;
  }

  @Override
  public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
    Object msg = e.getMessage();
    if (msg instanceof HttpRequest) {
      handleHttpRequest(ctx, (HttpRequest) msg);
    } else {
      webServer.getLog().warn(
          String.format("Web server received unknown frame %s", msg.getClass().getName()));
    }

  }

  @Override
  public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
    webServer.channelOpened(e.getChannel());
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
   */
  private void handleHttpRequest(ChannelHandlerContext ctx, HttpRequest req) throws Exception {

    if (handleWebRequest(ctx, req)) {
      // The method handled the request if the return value was true.
    } else {
      // Nothing we handle.
      webServer.getLog().warn(
          String.format("Web server has no handlers for request %s", req.getUri()));

      sendHttpResponse(ctx, req, new DefaultHttpResponse(HTTP_1_1, FORBIDDEN));
    }
  }

  /**
   * Attempt to handle an HTTP request by scanning through all registered
   * handlers.
   *
   * @param ctx
   *          The context for the request.
   * @param req
   *          The request.
   * @return True if the request was handled, false otherwise.
   */
  private boolean handleWebRequest(ChannelHandlerContext ctx, HttpRequest req) throws IOException {
    if (req.getMethod() != POST) {
      return false;
    }

    XmlRpcServerClientConnection connection =
        new XmlRpcServerClientConnection(ctx, req, webServer.getXmlRpcServer(), this);
    connection.process();

    return true;
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
   */
  public void sendHttpResponse(ChannelHandlerContext ctx, HttpRequest req, HttpResponse res) {
    // Generate an error page if response status code is not OK (200).
    if (res.getStatus().getCode() != HttpResponseStatus.OK.getCode()) {
      res.setContent(ChannelBuffers.copiedBuffer(res.getStatus().toString(), CharsetUtil.UTF_8));
      setContentLength(res, res.getContent().readableBytes());
    }

    Channel channel = ctx.getChannel();
    if (!channel.isOpen()) {
      webServer.getLog().warn("Attempting to send XML RPC response but channel is closed");
      return;
    }

    // Send the response and close the connection if necessary.
    ChannelFuture f = channel.write(res);

    if (!isKeepAlive(req) || res.getStatus().getCode() != HttpResponseStatus.OK.getCode()
        || req.getMethod() == POST) {
      f.addListener(ChannelFutureListener.CLOSE);
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
  private void sendSuccessHttpResponse(ChannelHandlerContext ctx, HttpRequest req) {
    DefaultHttpResponse res = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
    sendHttpResponse(ctx, req, res);
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
    webServer.getLog().error("Exception caught in the web server", e.getCause());
    e.getChannel().close();
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
    response.setHeader(CONTENT_TYPE, "text/plain; charset=UTF-8");
    response.setContent(ChannelBuffers.copiedBuffer("Failure: " + status.toString() + "\r\n",
        CharsetUtil.UTF_8));

    // Close the connection as soon as the error message is sent.
    ctx.getChannel().write(response).addListener(ChannelFutureListener.CLOSE);
  }

  /**
   * Get the webserver for the handler.
   *
   * @return the webserver for the handler
   */
  public NettyXmlRpcWebServer getWebServer() {
    return webServer;
  }
}
