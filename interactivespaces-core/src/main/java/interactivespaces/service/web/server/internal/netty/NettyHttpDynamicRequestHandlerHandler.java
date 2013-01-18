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

package interactivespaces.service.web.server.internal.netty;

import interactivespaces.service.web.server.HttpDynamicRequestHandler;

import java.io.IOException;
import java.util.Map;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;

import com.google.common.collect.Maps;

/**
 * A Netty handler for {@link HttpDynamicRequestHandler}.
 * 
 * @author Keith M. Hughes
 */
public class NettyHttpDynamicRequestHandlerHandler implements
		NettyHttpContentHandler {

	/**
	 * The handler which will handle the requests.
	 */
	private HttpDynamicRequestHandler requestHandler;

	/**
	 * The parent content handler for this handler.
	 */
	private NettyWebServerHandler parentHandler;

	/**
	 * The URI prefix to be handled by this handler.
	 */
	private String uriPrefix;

	/**
	 * Extra headers to add to the response.
	 */
	private Map<String, String> extraHttpContentHeaders = Maps.newHashMap();

	public NettyHttpDynamicRequestHandlerHandler(
			NettyWebServerHandler parentHandler, String up, boolean usePath,
			HttpDynamicRequestHandler requestHandler, Map<String, String> extraHttpContentHeaders) {
		this.parentHandler = parentHandler;

		if (extraHttpContentHeaders != null) {
			this.extraHttpContentHeaders.putAll(extraHttpContentHeaders);
		}

		StringBuilder uriPrefix = new StringBuilder();
		if (!up.startsWith("/")) {
			uriPrefix.append('/');
		}
		uriPrefix.append(up);
		if (usePath && !up.endsWith("/")) {
			uriPrefix.append('/');
		}
		this.uriPrefix = uriPrefix.toString();

		this.requestHandler = requestHandler;
	}

	@Override
	public boolean isHandledBy(HttpRequest req) {
		return req.getUri().startsWith(uriPrefix);
	}

	@Override
	public void handleWebRequest(ChannelHandlerContext ctx, HttpRequest req)
			throws IOException {
		interactivespaces.service.web.server.HttpRequest request = new NettyHttpRequest(
				req, parentHandler.getWebServer().getLog());
		NettyHttpResponse response = new NettyHttpResponse(ctx, extraHttpContentHeaders);

		DefaultHttpResponse res;
		try {
			requestHandler.handle(request, response);

			res = new DefaultHttpResponse(HttpVersion.HTTP_1_1,
					HttpResponseStatus.OK);
			res.setContent(response.getChannelBuffer());

			parentHandler.addHttpResponseHeaders(res, response.getContentHeaders());
			parentHandler.sendHttpResponse(ctx, req, res);
		} catch (Exception e) {
			parentHandler
					.getWebServer()
					.getLog()
					.error(String.format(
							"Error while handling dynamic web server request %s",
							req.getUri()), e);

			parentHandler.sendError(ctx,
					HttpResponseStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
