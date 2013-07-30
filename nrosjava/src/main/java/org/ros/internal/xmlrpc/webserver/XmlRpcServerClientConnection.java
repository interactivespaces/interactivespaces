/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ros.internal.xmlrpc.webserver;

import com.google.common.collect.Maps;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.common.ServerStreamConnection;
import org.apache.xmlrpc.server.XmlRpcHttpServerConfig;
import org.apache.xmlrpc.server.XmlRpcStreamServer;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferInputStream;
import org.jboss.netty.buffer.ChannelBufferOutputStream;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.ros.exception.RosRuntimeException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

/**
 * A connection between an XMLRPC server and a client for that server.
 *
 * <p>
 * One of these is created for each request that comes in.
 *
 * <p>
 * This code is derived from the web server which came with the Apache XMLRPC
 * server.
 *
 * @author Apache
 * @author Keith M. Hughes
 */
public class XmlRpcServerClientConnection implements ServerStreamConnection {

  /**
   * The channel buffer for writing content.
   */
  private ChannelBuffer channelBuffer;

  /**
   * The output stream for writing the XMLRPC response.
   */
  private ChannelBufferOutputStream outputStream;

  /**
   * The Netty handler context.
   */
  private ChannelHandlerContext ctx;

  /**
   * The Apache server for handling RPC calls.
   */
  private XmlRpcStreamServer xmlRpcServer;

  /**
   * Data for the request.
   */
  private XmlRpcServerClientRequestData requestData;

  /**
   * The HTTP request for the RPC call.
   */
  private HttpRequest request;

  /**
   * Input stream from the HTTP request for the RPC call.
   */
  private InputStream inputStream;

  /**
   * Netty Web Server handler that processes the HTTP requests.
   */
  private NettyXmlRpcWebServerHandler handler;

  /**
   * HTTP headers for the response.
   */
  private Map<String, String> headers = Maps.newHashMap();

  /**
   * If not {@code null} at the end of processing, something bad happened during
   * processing.
   */
  private Throwable processingThrowable;

  public XmlRpcServerClientConnection(ChannelHandlerContext ctx, HttpRequest request,
      XmlRpcStreamServer xmlRpcServer, NettyXmlRpcWebServerHandler handler) {
    this.ctx = ctx;
    this.request = request;
    this.xmlRpcServer = xmlRpcServer;
    this.handler = handler;

    channelBuffer = ChannelBuffers.dynamicBuffer();
    outputStream = new ChannelBufferOutputStream(channelBuffer);
  }

  /**
   * Process the XMLRPC request from the connection.
   */
  public void process() {
    XmlRpcServerClientRequestData data = getRequestConfig();

    try {
      xmlRpcServer.execute(data, this);

      // TODO(keith): At the moment not paying attention to
      // processingThrowable since it looks like for the most part the
      // responses to be sent back are not important. They will be logged
      // and should be examined over time. The original apache code had
      // special return headers for BadRequestException,
      // BadEncodingException, and XmlRpcNotAuthorizedException, none of
      // which are needed for ROS. Otherwise errors return a 200.

      DefaultHttpResponse res =
          new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);

      // for (Entry<String, String> header : headers.entrySet()) {
      // res.addHeader(header.getKey(), header.getValue());
      // }

      res.setContent(getChannelBuffer());

      handler.sendHttpResponse(ctx, request, res);
    } catch (XmlRpcException e) {
      handler.getWebServer().getLog().error("Error during XMLRPC server request handling", e);
    }
  }

  /**
   * Get the connections request configuration by merging the HTTP request
   * headers and the servers configuration.
   *
   * @return The connections request configuration.
   *
   * @throws IOException
   *           Reading the request headers failed.
   */
  private XmlRpcServerClientRequestData getRequestConfig() {
    requestData = new XmlRpcServerClientRequestData(this);

    XmlRpcHttpServerConfig serverConfig = (XmlRpcHttpServerConfig) xmlRpcServer.getConfig();
    requestData.setBasicEncoding(serverConfig.getBasicEncoding());
    requestData.setContentLengthOptional(serverConfig.isContentLengthOptional());
    requestData.setEnabledForExtensions(serverConfig.isEnabledForExtensions());
    requestData.setEnabledForExceptions(serverConfig.isEnabledForExceptions());

    requestData.setMethod("POST");
    String httpVersion = request.getProtocolVersion().getText();
    requestData.setHttpVersion(httpVersion);
    requestData.setKeepAlive(serverConfig.isKeepAliveEnabled() && request.isKeepAlive());
    requestData.setContentLength((int) request.getContentLength());

    return requestData;
  }

  @Override
  public InputStream newInputStream() throws IOException {
    inputStream = new ChannelBufferInputStream(request.getContent());

    return new BufferedInputStream(inputStream) {

      @Override
      public void close() throws IOException {
        // Block close. apparently the XML stream parser likes to close
        // the stream.
      }
    };
  }

  @Override
  public OutputStream newOutputStream() throws IOException {
    // TODO(keith): Should we check if a ByteArrayOutputStream should
    // be returned here? If so, we would need to store it somewhere
    // and then properly copy it to the outputStream here.
    //
    // It is normally used so we can have a content length in the headers.
    // Probably not necessary as we are allocating a special output stream,
    // not allowing for arbitrary streaming directly on the output channel
    return outputStream;
  }

  @Override
  public void close() throws IOException {
    if (inputStream != null) {
      inputStream.close();
    }
  }

  /**
   * Writes an error responses headers to the output stream.
   *
   * @param requestData
   *          the request data
   * @param throwable
   *          the error being reported
   * @param contentLength
   *          the response length, if known, or {@code -1}
   *
   * @throws IOException
   *           Writing the response failed.
   */
  public void notifyError(XmlRpcServerClientRequestData requestData, Throwable throwable)
      throws IOException {
    handler.getWebServer().getLog().error("Got XMLRPC error!", throwable);

    processingThrowable = throwable;
  }

  /**
   * Get the channel buffer containing the response.
   *
   * <p>
   * Once this is called, the output stream used for writing the response is
   * closed.
   *
   * @return the channel buffer
   */
  public ChannelBuffer getChannelBuffer() {
    try {
      if (outputStream != null) {
        outputStream.flush();
      }
    } catch (IOException e) {
      throw new RosRuntimeException("Could not flush XMLRPC server connection output stream", e);
    } finally {
      if (outputStream != null) {
        try {
          outputStream.close();
        } catch (IOException e) {
          throw new RosRuntimeException("Could not close XMLRPC server connection output stream", e);
        }
      }
    }

    return channelBuffer;
  }

  /**
   * Set a response HTTP header value.
   *
   * @param headerName
   *          the name of the HTTP header
   * @param headerValue
   *          the value of the HTTP header
   */
  public void setResponseHeader(String headerName, String headerValue) {
    headers.put(headerName, headerValue);
  }
}
