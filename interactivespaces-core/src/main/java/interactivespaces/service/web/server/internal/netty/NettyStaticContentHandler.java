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

import static org.jboss.netty.handler.codec.http.HttpHeaders.addHeader;
import static org.jboss.netty.handler.codec.http.HttpHeaders.getHeader;
import static org.jboss.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static org.jboss.netty.handler.codec.http.HttpHeaders.setContentLength;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.service.web.server.HttpStaticContentRequestHandler;
import interactivespaces.util.io.FileSupport;
import interactivespaces.util.io.FileSupportImpl;
import interactivespaces.util.web.MimeResolver;

import com.google.common.collect.Maps;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelFutureProgressListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.DefaultFileRegion;
import org.jboss.netty.channel.FileRegion;
import org.jboss.netty.handler.codec.http.CookieEncoder;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.ssl.SslHandler;
import org.jboss.netty.handler.stream.ChunkedFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpCookie;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handle static web content using Netty.
 *
 * @author Keith M. Hughes
 */
public class NettyStaticContentHandler implements NettyHttpContentHandler, HttpStaticContentRequestHandler {

  /**
   * The first portion of a content range header.
   */
  private static final String CONTENT_RANGE_PREFIX = "bytes ";

  /**
   * The separator between the start and end of the range.
   */
  private static final String CONTENT_RANGE_RANGE_SEPARATOR = "-";

  /**
   * The separator between the range and the file size in a content range header.
   */
  private static final String CONTENT_RANGE_RANGE_SIZE_SEPARATOR = "/";

  /**
   * Chunk size to use for copying content.
   */
  private static final int COPY_CHUNK_SIZE = 8192;

  /**
   * Regex for an HTTP range header.
   */
  private static final Pattern RANGE_HEADER_REGEX = Pattern.compile("bytes=(\\d+)\\-(\\d+)?");

  /**
   * The parent content handler for this handler.
   */
  private NettyWebServerHandler parentHandler;

  /**
   * Fallback handler to use in case of missing target.
   */
  private NettyHttpDynamicRequestHandlerHandler fallbackHandler;

  /**
   * The URI prefix to be handled by this handler.
   */
  private String uriPrefix;

  /**
   * Base directory for content served by this handler.
   */
  private File baseDir;

  /**
   * Extra headers to add to the response.
   */
  private Map<String, String> extraHttpContentHeaders = Maps.newHashMap();

  /**
   * Should this web-server allow links to be accessed? (Wander outside the root filesystem.) Useful for debugging &
   * development.
   */
  private boolean allowLinks;

  /**
   * The MIME resolver to use for responding to requests.
   *
   * <p>
   * Can be {@code null}.
   */
  private MimeResolver mimeResolver;

  /**
   * The file support to use.
   */
  private FileSupport fileSupport = FileSupportImpl.INSTANCE;

  /**
   * Create a new instance.
   *
   * @param parentHandler
   *          parent handler of this handler
   * @param uriPrefix
   *          uri prefix for this handler
   * @param baseDir
   *          base directory for static content
   * @param extraHttpContentHeaders
   *          extra http headers to use, can be {@code null}
   * @param fallbackHandler
   *          fallback handler to use, can be {@code null}
   */
  public NettyStaticContentHandler(NettyWebServerHandler parentHandler, String uriPrefix, File baseDir,
      Map<String, String> extraHttpContentHeaders, NettyHttpDynamicRequestHandlerHandler fallbackHandler) {
    this.parentHandler = parentHandler;
    this.fallbackHandler = fallbackHandler;

    if (extraHttpContentHeaders != null) {
      this.extraHttpContentHeaders.putAll(extraHttpContentHeaders);
    }

    StringBuilder sanitizedUriPrefix = new StringBuilder();
    if (!uriPrefix.startsWith(CONTENT_RANGE_RANGE_SIZE_SEPARATOR)) {
      sanitizedUriPrefix.append('/');
    }
    sanitizedUriPrefix.append(uriPrefix);
    if (!uriPrefix.endsWith(CONTENT_RANGE_RANGE_SIZE_SEPARATOR)) {
      sanitizedUriPrefix.append('/');
    }
    this.uriPrefix = sanitizedUriPrefix.toString();

    this.baseDir = baseDir;
  }

  @Override
  public void setMimeResolver(MimeResolver resolver) {
    mimeResolver = resolver;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends MimeResolver> T getMimeResolver() {
    return (T) mimeResolver;
  }

  @Override
  public boolean isHandledBy(HttpRequest request) {
    if (request.getUri().startsWith(uriPrefix)) {
      HttpMethod method = request.getMethod();
      return method == HttpMethod.GET || method == HttpMethod.HEAD;
    } else {
      return false;
    }
  }

  @Override
  public void handleWebRequest(ChannelHandlerContext ctx, HttpRequest request, Set<HttpCookie> cookiesToAdd)
      throws IOException {
    String url = request.getUri();
    String originalUrl = url;

    // Strip off query parameters, if any, as we don't care.
    int pos = url.indexOf('?');
    if (pos != -1) {
      url = url.substring(0, pos);
    }

    int luriPrefixLength = uriPrefix.length();
    String filepath =
        URLDecoder.decode(url.substring(url.indexOf(uriPrefix) + luriPrefixLength), StandardCharsets.UTF_8.name());

    File file = new File(baseDir, filepath);

    // Refuse to process if the path wanders outside of the base directory.
    if (!allowLinks && !fileSupport.isParent(baseDir, file)) {
      HttpResponseStatus status = HttpResponseStatus.FORBIDDEN;
      parentHandler.getWebServer().getLog().warn(
          String.format("HTTP [%s] %s --> (Path attempts to leave base directory)", status.getCode(), originalUrl));
      parentHandler.sendError(ctx, status);
      return;
    }

    RandomAccessFile raf;
    try {
      raf = new RandomAccessFile(file, "r");
    } catch (FileNotFoundException fnfe) {
      if (fallbackHandler != null) {
        fallbackHandler.handleWebRequest(ctx, request, cookiesToAdd);
      } else {
        HttpResponseStatus status = HttpResponseStatus.NOT_FOUND;
        parentHandler.getWebServer().getLog().warn(
                String.format("HTTP [%s] %s --> (File Not Found)", status.getCode(), originalUrl));
        parentHandler.sendError(ctx, status);
      }
      return;
    }
    long fileLength = raf.length();

    // Start with an initial OK response which will be modified as needed.
    HttpResponse response = new DefaultHttpResponse(HTTP_1_1, HttpResponseStatus.OK);

    setMimeType(filepath, response);

    parentHandler.addHttpResponseHeaders(response, extraHttpContentHeaders);
    parentHandler.addHeaderIfNotExists(response, HttpHeaders.Names.ACCEPT_RANGES, HttpHeaders.Values.BYTES);

    if (cookiesToAdd != null) {
      CookieEncoder encoder = new CookieEncoder(true);
      for (HttpCookie value : cookiesToAdd) {
        encoder.addCookie(NettyHttpResponse.createNettyCookie(value));
        addHeader(response, HttpHeaders.Names.SET_COOKIE, encoder.encode());
      }
    }

    RangeRequest rangeRequest = null;
    try {
      rangeRequest = parseRangeRequest(request, fileLength);
    } catch (Exception e) {
      try {
        HttpResponseStatus status = HttpResponseStatus.REQUESTED_RANGE_NOT_SATISFIABLE;
        parentHandler.getWebServer().getLog().error(String.format("[%s] HTTP %s --> %s",
            status.getCode(), originalUrl, e.getMessage()));
        response.setStatus(status);
        parentHandler.sendError(ctx, status);
      } finally {
        try {
          raf.close();
        } catch (Exception e1) {
          parentHandler.getWebServer().getLog().warn("Unable to close static content file", e1);
        }
      }
      return;
    }

    HttpResponseStatus status = HttpResponseStatus.OK;
    if (rangeRequest == null) {
      setContentLength(response, fileLength);
    } else {
      setContentLength(response, rangeRequest.getRangeLength());
      addHeader(response, HttpHeaders.Names.CONTENT_RANGE, CONTENT_RANGE_PREFIX + rangeRequest.begin
          + CONTENT_RANGE_RANGE_SEPARATOR + rangeRequest.end + CONTENT_RANGE_RANGE_SIZE_SEPARATOR + fileLength);
      status = HttpResponseStatus.PARTIAL_CONTENT;
      response.setStatus(status);
    }

    Channel ch = ctx.getChannel();

    // Write the initial line and the header.
    ChannelFuture writeFuture = ch.write(response);

    // Write the content if there have been no errors and we are a GET request.
    if (HttpMethod.GET == request.getMethod()) {
      if (ch.getPipeline().get(SslHandler.class) != null) {
        // Cannot use zero-copy with HTTPS.
        writeFuture = ch.write(new ChunkedFile(raf, 0, fileLength, COPY_CHUNK_SIZE));
      } else {
        // No encryption - use zero-copy.
        final FileRegion region =
            new DefaultFileRegion(raf.getChannel(), rangeRequest != null ? rangeRequest.begin : 0,
                rangeRequest != null ? rangeRequest.getRangeLength() : fileLength);
        writeFuture = ch.write(region);
        writeFuture.addListener(new ChannelFutureProgressListener() {
          @Override
          public void operationComplete(ChannelFuture future) {
            region.releaseExternalResources();
          }

          @Override
          public void operationProgressed(ChannelFuture arg0, long arg1, long arg2, long arg3) throws Exception {
            // Do nothing
          }
        });
      }
    }

    // Decide whether to close the connection or not.
    if (!isKeepAlive(request)) {
      // Close the connection when the whole content is written out.
      writeFuture.addListener(ChannelFutureListener.CLOSE);
    }

    parentHandler.getWebServer().getLog().debug(String.format("[%s] HTTP %s --> %s",
            status.getCode(), originalUrl, file.getPath()));
  }

  /**
   * Set the MIME type of the content, if we can.
   *
   * @param filepath
   *          the filepath for the content
   * @param response
   *          the HTTP response
   */
  private void setMimeType(String filepath, HttpResponse response) {
    if (mimeResolver != null) {
      String mimeType = mimeResolver.resolve(filepath);
      if (mimeType != null) {
        HttpHeaders.setHeader(response, HttpHeaders.Names.CONTENT_TYPE, mimeType);
      }
    }
  }

  /**
   * Get a range header from the request, if there is one.
   *
   * @param request
   *          the request
   * @param availableLength
   *          the available number of bytes for the file requested
   *
   * @return a parsed range header, or {@code null} if there is no range request header or there was some sort of error
   */
  private RangeRequest parseRangeRequest(HttpRequest request, long availableLength) {
    String rangeHeader = getHeader(request, HttpHeaders.Names.RANGE);
    if (rangeHeader == null || rangeHeader.trim().isEmpty()) {
      return null;
    }

    Matcher m = RANGE_HEADER_REGEX.matcher(rangeHeader);
    if (!m.matches()) {
      throw new SimpleInteractiveSpacesException(String.format("Unsupported HTTP range header, illegal syntax: %s",
          rangeHeader));
    }

    RangeRequest range = new RangeRequest();
    range.begin = Long.parseLong(m.group(1));
    String endMatch = m.group(2);
    if (endMatch != null && !endMatch.trim().isEmpty()) {
      range.end = Long.parseLong(endMatch);
    } else {
      range.end = availableLength - 1;
    }

    if (range.end < range.begin) {
      return null;
    }

    if (range.end >= availableLength) {
      throw new SimpleInteractiveSpacesException(String.format(
          "Unsupported HTTP range header, length requested is more than actual length: %s", rangeHeader));
    }

    return range;
  }

  /**
   * Allow files linked outside the root filesystem to be accessed.
   *
   * @param allowLinks
   *          {@code true} if following links should be allowed
   */
  public void setAllowLinks(boolean allowLinks) {
    this.allowLinks = allowLinks;
  }

  /**
   * An HTTP range request.
   *
   * @author Keith M. Hughes
   */
  public static class RangeRequest {

    /**
     * The position of the first byte in the request.
     */
    private long begin;

    /**
     * The position of the last byte in the request.
     */
    private long end;

    /**
     * Get the number of bytes in the range.
     *
     * @return the number of bytes in the range
     */
    long getRangeLength() {
      return end - begin + 1;
    }
  }
}
