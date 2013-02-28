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

package org.ros.internal.xmlrpc.webserver;

import static org.jboss.netty.channel.Channels.pipeline;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.commons.logging.Log;
import org.apache.xmlrpc.server.XmlRpcStreamServer;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.ros.exception.RosRuntimeException;

/**
 * An Apache XMLRPC webserver based on Netty.
 * 
 * @author Keith M. Hughes
 */
public class NettyXmlRpcWebServer {

	/**
	 * Port for the web server.
	 */
	private int port;

	/**
	 * Address for the web server.
	 */
	private InetAddress address;

	/**
	 * Server handler for web sockets.
	 */

	private Channel serverChannel;

	/**
	 * All channels we know about in the server.
	 */
	private ChannelGroup allChannels;

	/**
	 * Factory for all channels coming into the server.
	 */
	private NioServerSocketChannelFactory channelFactory;

	/**
	 * Threadpool for all threads.
	 */
	private ScheduledExecutorService threadPool;

	/**
	 * Logger for the web server.
	 */
	private Log log;

	/**
	 * Bootstrap for the server.
	 */
	private ServerBootstrap bootstrap;

	/**
	 * Handler for any requests coming into the server.
	 */
	private NettyXmlRpcWebServerHandler serverHandler;

	/**
	 * The bridge to the Apache XML RPC code.
	 */
	private XmlRpcStreamServer xmlRpcServer = new XmlRpcServerClientConnectionServer();

	public NettyXmlRpcWebServer(int port, InetAddress address,
			ScheduledExecutorService threadPool, Log log) {
		this.address = address;
		this.port = port;
		this.log = log;

		this.threadPool = threadPool;

		serverHandler = new NettyXmlRpcWebServerHandler(this);
	}

	/**
	 * Start the web server up.
	 */
	public void start() {
		allChannels = new DefaultChannelGroup("Apache XML-RPC Netty server");

		channelFactory = new NioServerSocketChannelFactory(threadPool,
				threadPool);

		bootstrap = new ServerBootstrap(channelFactory);

		// Set up the event pipeline factory.
		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			public ChannelPipeline getPipeline() throws Exception {
				// Create a default pipeline implementation.
				ChannelPipeline pipeline = pipeline();
				pipeline.addLast("decoder", new HttpRequestDecoder());
				// pipeline.addLast("aggregator", new
				// HttpChunkAggregator(4615604));
				pipeline.addLast("encoder", new HttpResponseEncoder());
				pipeline.addLast("handler", serverHandler);

				return pipeline;
			}
		});

		serverChannel = bootstrap.bind(new InetSocketAddress(port));
		allChannels.add(serverChannel);
	}

	/**
	 * Shut the web server down.
	 */
	public void shutdown() {
		if (allChannels != null) {
			ChannelGroupFuture future = allChannels.close();
			future.awaitUninterruptibly();

			channelFactory = null;
			allChannels = null;

			bootstrap.shutdown();
			bootstrap = null;
		}
	}

	/**
	 * Get the Apache XML RPC server associated with this web server.
	 * 
	 * @return the Apache XML RPC server associated with this web server
	 */
	public XmlRpcStreamServer getXmlRpcServer() {
		return xmlRpcServer;
	}

	/**
	 * A new channel was opened. Register it so it can be properly shut down.
	 * 
	 * @param channel
	 */
	public void channelOpened(Channel channel) {
		allChannels.add(channel);
	}

	/**
	 * Get the socket port for the web server.
	 * 
	 * @return the socket port
	 */
	public int getPort() {
		if (serverChannel != null) {
			SocketAddress localAddress = serverChannel.getLocalAddress();
			if (localAddress != null) {
				InetSocketAddress addr = (InetSocketAddress) localAddress;
				return addr.getPort();
			}
			SocketAddress remoteAddress = serverChannel.getRemoteAddress();
			if (remoteAddress != null) {
				InetSocketAddress addr = (InetSocketAddress) remoteAddress;
				return addr.getPort();
			}

			throw new RosRuntimeException(
					"No known address type for XMLRPC web server");
		} else {
			throw new RosRuntimeException(
					"XMLRPC server not started up yet, port not available");
		}
	}

	/**
	 * Return the log for the web server.
	 * 
	 * @return the log
	 */
	public Log getLog() {
		return log;
	}
}
