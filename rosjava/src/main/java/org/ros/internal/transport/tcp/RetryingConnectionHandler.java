/*
 * Copyright (C) 2011 Google Inc.
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

package org.ros.internal.transport.tcp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

/**
 * Automatically reconnects when a {@link Channel} is closed.
 * 
 * @author damonkohler@google.com (Damon Kohler)
 */
public class RetryingConnectionHandler extends SimpleChannelHandler {

	private static final boolean DEBUG = false;
	private static final Log log = LogFactory
			.getLog(RetryingConnectionHandler.class);

	private final TcpClientConnection tcpClientConnection;

	public RetryingConnectionHandler(TcpClientConnection tcpClientConnection) {
		this.tcpClientConnection = tcpClientConnection;
	}

	@Override
	public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e)
			throws Exception {
		tcpClientConnection.reconnect();

		super.channelClosed(ctx, e);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
			throws Exception {
		if (DEBUG) {
			log.error("Connection exception: " + tcpClientConnection.getName(),
					e.getCause());
		}
		e.getChannel().close();
		super.exceptionCaught(ctx, e);
	}
}
