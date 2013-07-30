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

import com.google.common.collect.Maps;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.service.web.server.HttpFileUpload;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.multipart.Attribute;
import org.jboss.netty.handler.codec.http.multipart.FileUpload;
import org.jboss.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import org.jboss.netty.handler.codec.http.multipart.HttpPostRequestDecoder.EndOfDataDecoderException;
import org.jboss.netty.handler.codec.http.multipart.InterfaceHttpData;
import org.jboss.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/**
 * A Netty-based {@link HttpFileUpload}.
 *
 * @author Keith M. Hughes
 */
public class NettyHttpFileUpload implements HttpFileUpload {

  /**
   * The parameters that were part of the post.
   */
  private Map<String, String> parameters = Maps.newHashMap();

  /**
   * The decoder
   */
  private HttpPostRequestDecoder decoder;

  /**
   * The request that started everything off.
   */
  private HttpRequest request;

  /**
   * web server this is part of
   */
  private NettyWebServer webServer;

  private FileUpload fileUpload;

  public NettyHttpFileUpload(HttpRequest request, HttpPostRequestDecoder decoder,
      NettyWebServer webServer) {
    this.request = request;
    this.decoder = decoder;
    this.webServer = webServer;
  }

  /**
   * @return the originsal request
   */
  public HttpRequest getRequest() {
    return request;
  }

  /**
   * Add a new chunk of data to the upload.
   *
   * @param ctx
   *          the context for the channel handling
   * @param chunk
   *          the chunked data
   */
  public void addChunk(ChannelHandlerContext ctx, HttpChunk chunk) throws Exception {
    decoder.offer(chunk);
    if (!chunk.isLast()) {
      try {
        while (decoder.hasNext()) {
          InterfaceHttpData data = decoder.next();
          if (data != null) {
            processHttpData(data);
          }
        }
      } catch (EndOfDataDecoderException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
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
      e.printStackTrace();
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
        webServer.getLog().error(
            "Form post BODY Attribute: " + attribute.getHttpDataType().name() + ": "
                + attribute.getName() + " Error while reading value:", e1);
      }
    } else if (data.getHttpDataType() == HttpDataType.FileUpload) {
      fileUpload = (FileUpload) data;
      if (fileUpload.isCompleted()) {
        webServer.getLog().info(String.format("File %s uploaded", fileUpload.getFilename()));
        // fileUpload.renameTo(new File("/var/tmp/"
        // + fileUpload.getFilename()));

        // fileUpload.isInMemory();// tells if the file is in Memory
        // or on File
        // fileUpload.renameTo(dest); // enable to move into another
        // File dest
        // decoder.removeFileUploadFromClean(fileUpload); //remove
        // the File of to delete file
      } else {
        webServer.getLog().error("File to be continued but should not!");
      }
    } else {
      webServer.getLog().warn(
          String.format("Unprocessed form post data type %s", data.getHttpDataType().name()));
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
        throw new InteractiveSpacesException(String.format("Unable to save uploaded file to %s",
            destination), e);
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
        throw new InteractiveSpacesException("Unable to save uploaded file to output stream", e);
      }
    } else {
      return false;
    }
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
}
