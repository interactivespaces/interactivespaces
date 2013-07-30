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

package org.ros.osgi.deployment.master.internal;

import static org.jboss.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static org.jboss.netty.handler.codec.http.HttpHeaders.setContentLength;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static org.jboss.netty.handler.codec.http.HttpMethod.GET;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.OK;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import org.apache.commons.logging.Log;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelFutureProgressListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.DefaultFileRegion;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.FileRegion;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.ssl.SslHandler;
import org.jboss.netty.handler.stream.ChunkedFile;
import org.jboss.netty.util.CharsetUtil;
import org.ros.osgi.deployment.master.FeatureRepository;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * A netty web server handler for working with the
 * {@link NettyHttpRemoteRepositoryMaster}.
 *
 * @author Keith M. Hughes
 */
public class NettyWebServerHandler extends SimpleChannelUpstreamHandler {

  /**
   * The web server we are attached to.
   */
  private NettyHttpRemoteRepositoryMaster repositoryMaster;

  /**
   * URI prefix for the server.
   */
  private String uriPrefix;

  /**
   * Repository which contains the ROS features.
   */
  private FeatureRepository featureRepository;

  /**
   * Log to write on.
   */
  private Log log;

  public NettyWebServerHandler(String uriPrefix, NettyHttpRemoteRepositoryMaster repositoryMaster,
      FeatureRepository featureRepository, Log log) {
    this.uriPrefix = uriPrefix;
    this.repositoryMaster = repositoryMaster;
    this.featureRepository = featureRepository;
  }

  @Override
  public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
    Object msg = e.getMessage();
    if (msg instanceof HttpRequest) {
      handleHttpRequest(ctx, (HttpRequest) msg);
    }
  }

  @Override
  public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
    repositoryMaster.channelOpened(e.getChannel());
  }

  @Override
  public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
    // No need to tell web server that channel closed, it handles cleanup
    // itself.
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
    // Allow only GET methods.
    if (req.getMethod() != GET) {
      sendHttpResponse(ctx, req, new DefaultHttpResponse(HTTP_1_1, FORBIDDEN));
    } else if (handleWebRequest(ctx, req)) {
      // The method handled the request if the return value was true.
    } else {
      // Nothing we handle.
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
    String url = req.getUri();
    int pos = url.indexOf('?');
    if (pos != -1)
      url = url.substring(0, pos);

    int luriPrefixLength = uriPrefix.length();
    String bundleName = url.substring(url.indexOf(uriPrefix) + luriPrefixLength);

    File file = featureRepository.getFeatureFile(bundleName);
    if (file == null) {
      return false;
    }

    RandomAccessFile raf;
    try {
      raf = new RandomAccessFile(file, "r");
    } catch (FileNotFoundException fnfe) {
      // sendError(ctx, NOT_FOUND);

      return false;
    }
    long fileLength = raf.length();

    HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
    setContentLength(response, fileLength);

    Channel ch = ctx.getChannel();

    // Write the initial line and the header.
    ch.write(response);

    // Write the content.
    ChannelFuture writeFuture;
    if (ch.getPipeline().get(SslHandler.class) != null) {
      // Cannot use zero-copy with HTTPS.
      writeFuture = ch.write(new ChunkedFile(raf, 0, fileLength, 8192));
    } else {
      // No encryption - use zero-copy.
      final FileRegion region = new DefaultFileRegion(raf.getChannel(), 0, fileLength);
      writeFuture = ch.write(region);
      writeFuture.addListener(new ChannelFutureProgressListener() {
        @Override
        public void operationComplete(ChannelFuture future) {
          region.releaseExternalResources();
        }

        @Override
        public void operationProgressed(ChannelFuture arg0, long arg1, long arg2, long arg3)
            throws Exception {
          // Do nothing
        }

      });
    }

    // Decide whether to close the connection or not.
    if (!isKeepAlive(req)) {
      // Close the connection when the whole content is written out.
      writeFuture.addListener(ChannelFutureListener.CLOSE);
    }

    return true;
  }

  /**
   * Send an HTTP response to the client.
   *
   * @param ctx
   * @param req
   * @param res
   */
  private void sendHttpResponse(ChannelHandlerContext ctx, HttpRequest req, HttpResponse res) {
    // Generate an error page if response status code is not OK (200).
    if (res.getStatus().getCode() != 200) {
      log.error(String.format("Error page for Repository server %s", req.getUri()));
      res.setContent(ChannelBuffers.copiedBuffer(res.getStatus().toString(), CharsetUtil.UTF_8));
      setContentLength(res, res.getContent().readableBytes());
    }

    // Send the response and close the connection if necessary.
    ChannelFuture f = ctx.getChannel().write(res);
    if (!isKeepAlive(req) || res.getStatus().getCode() != 200) {
      f.addListener(ChannelFutureListener.CLOSE);
    }
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
    e.getCause().printStackTrace();
    e.getChannel().close();
  }

  /**
   * Send an error to the remote machine.
   *
   * @param ctx
   * @param status
   */
  void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
    HttpResponse response = new DefaultHttpResponse(HTTP_1_1, status);
    response.setHeader(CONTENT_TYPE, "text/plain; charset=UTF-8");
    response.setContent(ChannelBuffers.copiedBuffer("Failure: " + status.toString() + "\r\n",
        CharsetUtil.UTF_8));

    // Close the connection as soon as the error message is sent.
    ctx.getChannel().write(response).addListener(ChannelFutureListener.CLOSE);
  }
}
