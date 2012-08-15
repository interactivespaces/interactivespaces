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

import java.net.ConnectException;
import java.net.SocketAddress;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferFactory;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.group.ChannelGroup;
import org.ros.exception.RosRuntimeException;

import com.google.common.base.Preconditions;

/**
 * A connection between a service client and its service or a subscriber and its
 * publisher.
 * 
 * @author damonkohler@google.com (Damon Kohler)
 */
public class TcpClientConnection {

	private static final boolean DEBUG = false;

	private static final int CONNECTION_TIMEOUT_MILLIS = 5000;

	private static final String CONNECTION_REFUSED = "Connection refused";

	// TODO(damonkohler): Allow the TcpClientConnection to alter the
	// reconnect strategy (e.g. binary backoff, faster retries for tests, etc.)
	private static final long RECONNECT_DELAY = 1000;

	/**
	 * The connection manager this connection is part of.
	 */
	private final TcpClientConnectionManager connectionManager;

	private final String name;
	private final SocketAddress remoteAddress;
	private final ClientBootstrap bootstrap;

	private final ChannelGroup channelGroup;

	private final ScheduledExecutorService executorService;
	
	private final TcpClientConnectionListener listener;

	/**
	 * {@code true} if this client connection should reconnect when
	 * disconnected.
	 */
	private boolean persistent;

	/**
	 * {@code true} if this connection is defunct (e.g. received a connection
	 * refused error)
	 */
	private boolean defunct;

	/**
	 * This connection's {@link Channel}. May be {@code null} if we're not
	 * currently connected.
	 */
	private Channel channel;

	private int retryCount = 3;
	private long currentReconnectDelay = RECONNECT_DELAY / 2;

	private final Log log;

	/**
	 * @param remoteAddress
	 *            the {@link SocketAddress} to reconnect to
	 */
	TcpClientConnection(TcpClientConnectionManager connectionManager,
			String name, SocketAddress remoteAddress,
			TcpClientConnectionListener listener,
			ChannelFactory channelFactory,
			ChannelBufferFactory channelBufferFactory,
			ChannelGroup channelGroup,
			ScheduledExecutorService executorService, Log log) {
		this.connectionManager = connectionManager;
		this.name = name;
		this.remoteAddress = remoteAddress;
		this.listener = listener;
		this.channelGroup = channelGroup;
		this.executorService = executorService;
		this.log = log;
		persistent = true;
		defunct = false;

		bootstrap = new ClientBootstrap(channelFactory);
		bootstrap.setOption("bufferFactory", channelBufferFactory);
		bootstrap.setOption("connectionTimeoutMillis",
				CONNECTION_TIMEOUT_MILLIS);
		bootstrap.setOption("keepAlive", true);
	}

	/**
	 * Connects to a server.
	 * 
	 * <p>
	 * This call blocks until the connection is established or fails.
	 * 
	 * @param address
	 * @param handler
	 * @param handlerName
	 */
	public void connect(SocketAddress address, final ChannelHandler handler,
			final String handlerName) {
		TcpClientPipelineFactory pipelineFactory = new TcpClientPipelineFactory(
				channelGroup, this) {
			@Override
			public ChannelPipeline getPipeline() {
				ChannelPipeline pipeline = super.getPipeline();
				pipeline.addLast(handlerName, handler);
				return pipeline;
			}
		};
		bootstrap.setPipelineFactory(pipelineFactory);
		ChannelFuture future = bootstrap.connect(address)
				.awaitUninterruptibly();
		if (future.isSuccess()) {
			channel = future.getChannel();
			if (log.isInfoEnabled()) {
				log.info("Connected to: " + address);
			}
			
			if (listener != null) {
				listener.onTcpClientConnectionMade();
			}
		} else {
			// We expect the first connection to succeed. If not, fail fast.
			throw new RosRuntimeException("Connection exception: " + address,
					future.getCause());
		}
	}

	/**
	 * Reconnect, if possible, to the remote connection.
	 */
	public void reconnect() {
		clear();

		if (DEBUG) {
			if (isDefunct()) {
				log.info("Connection defunct: " + name);
			}
		}
		if (isPersistent() && !isDefunct()) {
			if (retryCount-- > 0) {
				if (DEBUG) {
					log.info("Connection closed, will reconnect: " + name);
				}
				currentReconnectDelay += RECONNECT_DELAY;
				executorService.schedule(new Runnable() {
					@Override
					public void run() {
						attemptReconnect();
					}
				}, currentReconnectDelay, TimeUnit.MILLISECONDS);
			} else {
				connectionDropped();
			}
		} else {
			if (DEBUG) {
				log.info("Connection closed, will not reconnect: " + name);
			}
		}
	}

	/**
	 * The connection has been dropped.
	 */
	private void connectionDropped() {
		setDefunct(true);
		connectionManager.unregisterConnection(this);
		
		if (listener != null) {
			listener.onTcpClientConnectionLost();
		}
	}

	/**
	 * Do an actual reconnection attempt.
	 */
	public void attemptReconnect() {
		if (log.isInfoEnabled()) {
			log.info("Reconnecting: " + name);
		}
		ChannelFuture future = bootstrap.connect(remoteAddress)
				.awaitUninterruptibly();
		if (future.isSuccess()) {
			channel = future.getChannel();
			if (log.isInfoEnabled()) {
				log.info("Reconnect successful: " + name);
			}
		} else {
			log.error("Reconnect failed: " + name);
			// log.error("Reconnect failed: " + name, future.getCause());
			// TODO(damonkohler): Is there a better way to check for connection
			// refused?
			if (future.getCause() instanceof ConnectException
					&& future.getCause().getMessage()
							.equals(CONNECTION_REFUSED)) {
				log.error("Connection refused, marking as defunct: " + name);
				// log.error("Connection refused, marking as defunct: " + name,
				// future.getCause());

				setDefunct(true);
			}
		}
	}

	/**
	 * @return the {@link SocketAddress} to reconnect to
	 */
	public SocketAddress getRemoteAddress() {
		return remoteAddress;
	}

	/**
	 * @param persistent
	 *            {@code true} if this client connection should reconnect when
	 *            disconnected
	 */
	public void setPersistent(boolean persistent) {
		this.persistent = persistent;
	}

	/**
	 * @return {@code true} if this client connection should reconnect when
	 *         disconnected
	 */
	public boolean isPersistent() {
		return persistent;
	}

	/**
	 * @see Channel#write
	 */
	public ChannelFuture write(ChannelBuffer buffer) {
		System.out.println("Writing!");
		Preconditions.checkNotNull(channel, "Not connected.");
		return channel.write(buffer);
	}

	/**
	 * Clear the connection of any internal resources
	 */
	void clear() {
		channel = null;
	}

	/**
	 * @return the name of this connection (e.g. Subscriber</topic/foo>)
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return {@code true} if this connection is defunct (e.g. received a
	 *         connection refused error)
	 */
	public boolean isDefunct() {
		return defunct;
	}

	/**
	 * @param defunct
	 *            {@code true} if this connection is defunct (e.g. received a
	 *            connection refused error)
	 */
	public void setDefunct(boolean defunct) {
		this.defunct = defunct;
	}
}
