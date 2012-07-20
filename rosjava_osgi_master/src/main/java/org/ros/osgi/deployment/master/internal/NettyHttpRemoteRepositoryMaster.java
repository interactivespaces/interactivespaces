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

package org.ros.osgi.deployment.master.internal;

import static org.jboss.netty.channel.Channels.pipeline;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.commons.logging.Log;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.ros.osgi.common.RosEnvironment;
import org.ros.osgi.deployment.master.FeatureRepository;
import org.ros.osgi.deployment.master.RemoteRepositoryMaster;

/**
 * A ROS repository which uses the OSGi HTTP service
 * 
 * TODO(keith): Change over to netty
 *
 * @author Keith M. Hughes
 */
public class NettyHttpRemoteRepositoryMaster implements RemoteRepositoryMaster {
	
	/**
	 * ROS Environment the repository is running in.
	 */
	private RosEnvironment rosEnvironment;
	
	/**
	 * 
	 */
	private static final String REPOSITORY_BUNDLE_URL_PREFIX = "/ros/feature";
	
	/**
	 * The repository containing all features.
	 */
	private FeatureRepository featureRepository;

	/**
	 * Name of the server.
	 */
	private String serverName;

	/**
	 * Port the server should run on.
	 */
	private int port;

	/**
	 * The server handler for requests.
	 */
	private NettyWebServerHandler serverHandler;

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
	 * Thread pools for Netty.
	 */
	private ScheduledExecutorService executorService;
	
	/**
	 * Logger for the repository.
	 */
	private Log log;

	/**
	 * Socket address for the master.
	 */
	private InetSocketAddress masterSocketAddress;
	
	@Override
	public void startup() {
		serverName = "ROSDeploymentServer";
		port = 8085;
		executorService = rosEnvironment.getExecutorService();
		log = rosEnvironment.getLog();

		allChannels = new DefaultChannelGroup(serverName);

		serverHandler = new NettyWebServerHandler(REPOSITORY_BUNDLE_URL_PREFIX, this, featureRepository, log);
		channelFactory = new NioServerSocketChannelFactory(executorService,
				executorService);

		ServerBootstrap bootstrap = new ServerBootstrap(channelFactory);

		// Set up the event pipeline factory.
		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			public ChannelPipeline getPipeline() throws Exception {
				// Create a default pipeline implementation.
				ChannelPipeline pipeline = pipeline();
				pipeline.addLast("decoder", new HttpRequestDecoder());
				pipeline.addLast("aggregator", new HttpChunkAggregator(65536));
				pipeline.addLast("encoder", new HttpResponseEncoder());
				pipeline.addLast("handler", serverHandler);

				return pipeline;
			}
		});

		masterSocketAddress = new InetSocketAddress(port);
		serverChannel = bootstrap.bind(masterSocketAddress);
		allChannels.add(serverChannel);
	}

	@Override
	public void shutdown() {
		ChannelGroupFuture future = allChannels.close();
		future.awaitUninterruptibly();
	}

	@Override
	public Set<String> getBundleUris(Set<String> bundles) {
		Set<String> result = new HashSet<String>();
		
		for (String bundle : bundles) {
			String uri = String.format("http://%s:%d%s/%s", 
					masterSocketAddress.getHostName(), masterSocketAddress.getPort(), REPOSITORY_BUNDLE_URL_PREFIX, bundle);
			result.add(uri);
		}
			
		return result;
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
	 * Get the web server's logger.
	 * 
	 * @return
	 */
	public Log getLog() {
		return log;
	}    

	/**
	 * Set the Ros Environment the server should run in.
	 * 
	 * @param rosEnvironment
	 */
	public void setRosEnvironment(RosEnvironment rosEnvironment) {
		this.rosEnvironment = rosEnvironment;
	}

	/**
	 * Remove the Ros Environment the server should run in.
	 * 
	 * @param rosEnvironment
	 */
	public void unsetRosEnvironment(RosEnvironment rosEnvironment) {
		this.rosEnvironment = null;
	}

	/**
	 * Set the feature repository the server should run with.
	 * 
	 * @param featureRepository
	 */
	public void setFeatureRepository(FeatureRepository featureRepository) {
		this.featureRepository = featureRepository;
	}

	/**
	 * Remove the feature repository the server should run with.
	 * 
	 * @param featureRepository
	 */
	public void unsetFeatureRepository(FeatureRepository featureRepository) {
		this.featureRepository = null;
	}
}
