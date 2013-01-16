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

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferOutputStream;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelHandlerContext;

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
	
	private ChannelBuffer channelBuffer;
	
	public NettyHttpResponse(ChannelHandlerContext ctx) {
		this.ctx = ctx;
		channelBuffer = ChannelBuffers.dynamicBuffer();
	}

	@Override
	public OutputStream getOutputStream() {
		return new ChannelBufferOutputStream(channelBuffer);
	}

	/**
	 * @return the channelBuffer
	 */
	public ChannelBuffer getChannelBuffer() {
		return channelBuffer;
	}
}
