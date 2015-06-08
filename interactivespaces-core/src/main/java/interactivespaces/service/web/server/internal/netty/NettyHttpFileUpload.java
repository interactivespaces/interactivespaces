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

import interactivespaces.InteractiveSpacesException;
import interactivespaces.service.web.server.HttpFileUpload;

import com.google.common.collect.Maps;

import org.apache.commons.logging.Log;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.multipart.Attribute;
import org.jboss.netty.handler.codec.http.multipart.FileUpload;
import org.jboss.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import org.jboss.netty.handler.codec.http.multipart.HttpPostRequestDecoder.EndOfDataDecoderException;
import org.jboss.netty.handler.codec.http.multipart.InterfaceHttpData;
import org.jboss.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpCookie;
import java.util.Map;
import java.util.Set;

/**
 * A Netty-based {@link HttpFileUpload}.
 *
 * @author Keith M. Hughes
 */
public class NettyHttpFileUpload implements HttpFileUpload {

  /**
   * The handler for the POST request. It can be {@code null}.
   */
  private NettyHttpPostRequestHandler handler;

  /**
   * The parameters that were part of the post.
   */
  private Map<String, String> parameters = Maps.newHashMap();

  /**
   * The decoder.
   */
  private HttpPostRequestDecoder decoder;

  /**
   * The request that started everything off.
   */
  private HttpRequest nettyHttpRequest;

  /**
   * The web server handler handling the request.
   */
  private NettyWebServerHandler webServerHandler;

  /**
   * FileUpload container for this particular upload.
   */
  private FileUpload fileUpload;

  /**
   * The cookies to add.
   */
  private Set<HttpCookie> cookies;

  /**
   * Create a new instance.
   *
   * @param nettyHttpRequest
   *          incoming Netty HTTP request
   * @param decoder
   *          decoder to use
   * @param handler
   *          the HTTP POST handler, can be {@code null}
   * @param webServerHandler
   *          underlying web server handler
   * @param cookies
   *          any cookies to add to responses
   */
  public NettyHttpFileUpload(HttpRequest nettyHttpRequest, HttpPostRequestDecoder decoder,
      NettyHttpPostRequestHandler handler, NettyWebServerHandler webServerHandler, Set<HttpCookie> cookies) {
    this.nettyHttpRequest = nettyHttpRequest;
    this.decoder = decoder;
    this.handler = handler;
    this.webServerHandler = webServerHandler;
    this.cookies = cookies;
  }

  /**
   * Get the original Netty HTTP request.
   *
   * @return the original Netty HTTP request
   */
  public HttpRequest getNettyHttpRequest() {
    return nettyHttpRequest;
  }

  /**
   * Add a new chunk of data to the upload.
   *
   * @param ctx
   *          the context for the channel handling
   * @param chunk
   *          the chunked data
   *
   * @throws Exception
   *           problem adding chunk
   *
   */
  public void addChunk(ChannelHandlerContext ctx, HttpChunk chunk) throws Exception {
    if (!chunk.getContent().readable() && !chunk.isLast()) {
      return;
    }

    decoder.offer(chunk);
    try {
      while (decoder.hasNext()) {
        InterfaceHttpData data = decoder.next();
        if (data != null) {
          processHttpData(data);
        }
        if (chunk.isLast()) {
          break;
        }
      }
    } catch (EndOfDataDecoderException e) {
      getLog().error("Error while adding HTTP chunked POST data", e);
    }
  }

  /**
   * Clean up anything from the upload.
   */
  public void clean() {
    decoder.cleanFiles();
  }

  /**
   * Complete processing of the file upload.
   */
  public void completeNonChunked() {
    try {
      for (InterfaceHttpData data : decoder.getBodyHttpDatas()) {
        processHttpData(data);
      }
    } catch (Exception e) {
      getLog().error("Error while completing HTTP chunked POST data", e);
    }
  }

  /**
   * The file upload is complete. Handle it as needed.
   *
   * @param context
   *          the context for the channel handler
   */
  public void fileUploadComplete(ChannelHandlerContext context) {
    if (handler != null) {
      handleFileUploadCompleteThroughHandler(context);
    } else {
      handleFileUploadCompleteThroughListener(context);
    }
  }

  /**
   * Handle the file load completion through the handler.
   *
   * @param context
   *          the context for the channel handler
   */
  private void handleFileUploadCompleteThroughHandler(ChannelHandlerContext context) {
    try {
      handler.handleWebRequest(context, nettyHttpRequest, this, cookies);
    } catch (Exception e) {
      getLog().error(String.format("Exception when handling web request %s", nettyHttpRequest.getUri()), e);
    }
  }

  /**
   * Handle the file handle completion through the listener.
   *
   * @param context
   *          the channel context
   */
  private void handleFileUploadCompleteThroughListener(ChannelHandlerContext context) {
    try {
      webServerHandler.getHttpFileUploadListener().handleHttpFileUpload(this);

      HttpResponseStatus status = HttpResponseStatus.OK;
      getLog().debug(String.format("HTTP [%s] %s --> (File Upload)", status.getCode(), nettyHttpRequest.getUri()));
      webServerHandler.sendSuccessHttpResponse(context, nettyHttpRequest);
    } catch (Throwable e) {
      getLog().error("Error while calling file upload listener", e);
      webServerHandler.sendError(context, HttpResponseStatus.INTERNAL_SERVER_ERROR);
    } finally {
      clean();
    }
  }

  /**
   * Process a clump of of the HTTP data.
   *
   * @param data
   *          the data
   */
  private void processHttpData(InterfaceHttpData data) {
    if (data.getHttpDataType() == HttpDataType.Attribute) {
      Attribute attribute = (Attribute) data;
      try {
        parameters.put(attribute.getName(), attribute.getValue());
      } catch (IOException e1) {
        // Error while reading data from File, only print name and error
        getLog().error(
            "Form post BODY Attribute: " + attribute.getHttpDataType().name() + ": " + attribute.getName()
                + " Error while reading value:", e1);
      }
    } else if (data.getHttpDataType() == HttpDataType.FileUpload) {
      fileUpload = (FileUpload) data;
      if (fileUpload.isCompleted()) {
        getLog().info(String.format("File %s uploaded", fileUpload.getFilename()));
      } else {
        getLog().error("File to be continued but should not!");
      }
    } else {
      getLog().warn(String.format("Unprocessed form post data type %s", data.getHttpDataType().name()));
    }
  }

  @Override
  public boolean hasFile() {
    return fileUpload != null;
  }

  @Override
  public boolean moveTo(File destination) {
    if (hasFile()) {
      try {
        fileUpload.renameTo(destination);

        return true;
      } catch (Exception e) {
        throw InteractiveSpacesException.newFormattedException(e, "Unable to save uploaded file to %s", destination);
      }
    } else {
      return false;
    }
  }

  @Override
  public boolean copyTo(OutputStream destination) {
    if (hasFile()) {
      try {
        ChannelBuffer channelBuffer = fileUpload.getChannelBuffer();
        channelBuffer.getBytes(0, destination, channelBuffer.readableBytes());

        return true;
      } catch (Exception e) {
        throw InteractiveSpacesException.newFormattedException(e, "Unable to save uploaded file to output stream");
      }
    } else {
      return false;
    }
  }

  @Override
  public String getFormName() {
    return fileUpload.getName();
  }

  @Override
  public String getFilename() {
    if (hasFile()) {
      return fileUpload.getFilename();
    } else {
      return null;
    }
  }

  @Override
  public Map<String, String> getParameters() {
    return parameters;
  }

  /**
   * Get the log for the uploader.
   *
   * @return the log
   */
  private Log getLog() {
    return webServerHandler.getWebServer().getLog();
  }
}
