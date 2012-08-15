package org.ros.internal.node;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.ros.internal.node.client.Registrar;
import org.ros.internal.node.topic.DefaultPublisher;
import org.ros.internal.node.topic.DefaultSubscriber;
import org.ros.internal.node.topic.TopicManager;
import org.ros.namespace.GraphName;
import org.ros.node.parameter.ParameterTree;

public class MasterPinger {
	
	/**
	 * The node the pinger is pinging for.
	 */
	private DefaultNode node;
	
	/**
	 * The topic manager for the node.
	 */
	private TopicManager topicManager;
	
	/**
	 * The registrar for the node.
	 */
	private Registrar registrar;
	
	/**
	 * The threaded repeater.
	 */
	private ScheduledFuture<?> masterPingFuture;
	
	/**
	 * Access to the parameter tree.
	 */
	private ParameterTree masterPingParams;
	
	/**
	 * The name of the parameter being used for the ping.
	 */
	private GraphName masterPingParameterName;
	
	/**
	 * The logger to use.
	 */
	private Log log;

	/**
	 * Start the master ping service for the node.
	 * 
	 * @param scheduledExecutorService
	 */
	public void start(ScheduledExecutorService scheduledExecutorService,
			DefaultNode node, TopicManager topicManager, Registrar registrar) {
		this.node = node;
		this.topicManager = topicManager;
		this.registrar = registrar;

		masterPingParams = node.newParameterTree();
		masterPingParameterName = new GraphName("/rosmasterping"
				+ node.getName().toString() + "/" + "ros___masterping___");
		setupMasterPingResponse();
		masterPingFuture = scheduledExecutorService.scheduleWithFixedDelay(
				new Runnable() {
					@Override
					public void run() {
						pingMasterForRegistrations();
					}
				}, 5, 5, TimeUnit.SECONDS);
	}

	/**
	 * Shut the pinger down.
	 */
	public void shutdown() {
		if (masterPingFuture != null) {
			masterPingFuture.cancel(true);
		}
	}

	/**
	 * Check for master registrations of the node.
	 */
	private void pingMasterForRegistrations() {
		Boolean ping = ping();
		if (ping == null || !ping) {
			System.out.println("Master lost for " + node.getName());

			if (ping != null && setupMasterPingResponse()) {
				handleReregistration();
			} else {
				System.out.println("Unable to reset ping");
			}
		}
	}

	/**
	 * Reregister every topic with the master.
	 */
	private void handleReregistration() {
		for (DefaultPublisher<?> publisher : topicManager.getPublishers()) {
			registrar.registerPublisher(publisher);
		}
		for (DefaultSubscriber<?> subscriber : topicManager.getSubscribers()) {
			registrar.registerSubscriber(subscriber);
		}
	}

	/**
	 * Tell the master that the node is properly registered.
	 */
	private boolean setupMasterPingResponse() {
		try {
			masterPingParams.set(masterPingParameterName, true);

			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Ping the master for the node
	 * 
	 * @return {@code true} if the ping worked, {@code false} if there is a
	 *         master but the node is not recognized, {@code null} otherwise.
	 */
	private Boolean ping() {
		try {
			return masterPingParams.getBoolean(masterPingParameterName, false);
		} catch (Exception e) {
			return null;
		}
	}
}