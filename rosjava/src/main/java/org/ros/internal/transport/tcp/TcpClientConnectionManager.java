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

import java.net.SocketAddress;
import java.nio.ByteOrder;
import java.util.Collection;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.buffer.ChannelBufferFactory;
import org.jboss.netty.buffer.HeapChannelBufferFactory;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

import com.google.common.collect.Lists;

/**
 * Manage TCP connections for subscribers and service clients.
 * 
 * @author damonkohler@google.com (Damon Kohler)
 */
public class TcpClientConnectionManager {

	private static final boolean DEBUG = false;
	private static final Log log = LogFactory
			.getLog(TcpClientConnectionManager.class);

	private final ChannelFactory channelFactory;
	private final ChannelGroup channelGroup;
	private final ChannelBufferFactory channelBufferFactory;
	private final Collection<TcpClientConnection> tcpClientConnections;
	private final ScheduledExecutorService executorService;

	public TcpClientConnectionManager(ScheduledExecutorService executorService) {
		this.executorService = executorService;
		channelFactory = new NioClientSocketChannelFactory(executorService,
				executorService);
		channelGroup = new DefaultChannelGroup();
		channelBufferFactory = new HeapChannelBufferFactory(
				ByteOrder.LITTLE_ENDIAN);
		tcpClientConnections = Lists.newArrayList();
	}

	/**
	 * Connects to a server.
	 * 
	 * <p>
	 * This call blocks until the connection is established or fails.
	 * 
	 * @param connectionName
	 * @param address
	 * @param handler
	 * @param handlerName
	 * @return a new {@link TcpClientConnection}
	 */
	public TcpClientConnection connect(String connectionName,
			SocketAddress address, TcpClientConnectionListener listener,
			ChannelHandler handler, String handlerName) {
		TcpClientConnection connection = new TcpClientConnection(this,
				connectionName, address, listener, channelFactory,
				channelBufferFactory, channelGroup, executorService, log);
		connection.connect(address, handler, handlerName);

		// Will only get stored if the connection happen
		tcpClientConnections.add(connection);

		return connection;
	}

	/**
	 * Sets all {@link TcpClientConnection}s as non-persistent and closes all
	 * open {@link Channel}s.
	 */
	public void shutdown() {
		for (TcpClientConnection tcpClientConnection : tcpClientConnections) {
			tcpClientConnection.setPersistent(false);
			tcpClientConnection.clear();
		}
		channelGroup.close().awaitUninterruptibly();
		tcpClientConnections.clear();
		// Not calling channelFactory.releaseExternalResources() or
		// bootstrap.releaseExternalResources() since only external resources
		// are
		// the ExecutorService and control of that must remain with the overall
		// application.
	}

	/**
	 * Unregister a connection from the manager.
	 * 
	 * @param connection
	 *            the connection to remove
	 */
	public void unregisterConnection(TcpClientConnection connection) {
		tcpClientConnections.remove(connection);
	}
}
