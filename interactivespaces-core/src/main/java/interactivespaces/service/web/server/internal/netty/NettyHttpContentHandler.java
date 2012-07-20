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

import java.io.IOException;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.HttpRequest;

/**
 * Handle HTTP content from Netty.
 *
 * @author Keith M. Hughes
 */
public interface NettyHttpContentHandler {

	/**
	 * Is the request handled by this handler?
	 * 
	 * @param req Request which has come in.
	 * 
	 * @return True if the request is handled by this handler, false otherwise.
	 */
	boolean isHandledBy(HttpRequest req);

	/**
	 * @param ctx
	 * @param req
	 * @throws IOException
	 */
	void handleWebRequest(ChannelHandlerContext ctx, HttpRequest req)
			throws IOException;
}