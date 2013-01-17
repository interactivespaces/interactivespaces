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

import interactivespaces.service.web.server.HttpResponse;

import java.io.OutputStream;
import java.util.Map;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferOutputStream;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelHandlerContext;

import com.google.common.collect.Maps;

/**
 * A Netty-based HttpResponse
 * 
 * @author Keith M. Hughes
 */
public class NettyHttpResponse implements HttpResponse {

	/**
	 * The Netty handler context.
	 */
	private ChannelHandlerContext ctx;

	/**
	 * The channel buffer for writing content.
	 */
	private ChannelBuffer channelBuffer;
	
	/**
	 * Content headers to add to the response.
	 */
	private Map<String, String> contentHeaders = Maps.newHashMap();

	public NettyHttpResponse(ChannelHandlerContext ctx,
			Map<String, String> extraHttpContentHeaders) {
		this.ctx = ctx;
		channelBuffer = ChannelBuffers.dynamicBuffer();
		
		if (extraHttpContentHeaders != null) {
			contentHeaders.putAll(extraHttpContentHeaders);
		}
	}

	@Override
	public OutputStream getOutputStream() {
		return new ChannelBufferOutputStream(channelBuffer);
	}

	@Override
	public void addContentHeader(String name, String value) {
		contentHeaders.put(name,  value);
	}

	@Override
	public void addContentHeaders(Map<String, String> headers) {
		contentHeaders.putAll(headers);
	}

	@Override
	public Map<String, String> getContentHeaders() {
		return contentHeaders;
	}

	/**
	 * @return the channelBuffer
	 */
	public ChannelBuffer getChannelBuffer() {
		return channelBuffer;
	}
}
