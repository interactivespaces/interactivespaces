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

package interactivespaces.util.ros;

import interactivespaces.InteractiveSpacesException;

import java.util.List;

import org.apache.commons.logging.Log;
import org.ros.internal.node.topic.SubscriberIdentifier;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.PublisherListener;

import com.google.common.collect.Lists;

/**
 * A collection of ROS publishers for a given message type.
 * 
 * @author Keith M. Hughes
 */
public class RosPublishers<T> implements PublisherListener<T> {

	/**
	 * Separator between names or topics.
	 */
	public static final String SEPARATOR = ":";

	/**
	 * All publishers registered.
	 */
	private List<Publisher<T>> publishers = Lists.newArrayList();

	/**
	 * Logger for this collection.
	 */
	private Log log;

	public RosPublishers(Log log) {
		this.log = log;
	}

	/**
	 * Add a series of publishers to a node.
	 * 
	 * <p>
	 * All topic names will be resolved using the ROS name resolver on the node.
	 * 
	 * <p>
	 * The publishers will not be latched.
	 * 
	 * @param node
	 *            the node the publishers will be added to
	 * @param topicNames
	 *            a colon separated list of topics to be published to
	 * @param messageType
	 *            the message type for all of the publishers
	 */
	public void addPublishers(ConnectedNode node, String messageType,
			String topicNames) {
		addPublishers(node, messageType, topicNames, false);
	}

	/**
	 * Add a series of publishers to a node.
	 * 
	 * <p>
	 * All topic names will be resolved using the ROS name resolver on the node.
	 * 
	 * @param node
	 *            the node the publishers will be added to
	 * @param topicNames
	 *            a colon separated list of topics to be published to
	 * @param messageType
	 *            the message type for all of the publishers
	 * @param latch
	 *            {@code true} if the publisher should always send the last
	 *            message sent to any new subscribers
	 */
	public void addPublishers(ConnectedNode node, String messageType,
			String topicNames, boolean latch) {
		if (log.isInfoEnabled()) {
			log.info(String
					.format("Adding publishers for topic names %s with message type %s",
							topicNames, messageType));
		}

		for (String topicName : topicNames.split(SEPARATOR)) {
			topicName = topicName.trim();
			if (!topicName.isEmpty()) {
				if (log.isInfoEnabled()) {
					log.info(String.format("Adding publisher topic %s",
							topicName));
				}
				Publisher<T> publisher = node.newPublisher(topicName,
						messageType);
				if (log.isInfoEnabled()) {
					log.info(String.format("Added publisher topic %s",
							topicName));
				}
				publisher.addListener(this);
				publisher.setLatchMode(latch);
				publishers.add(publisher);
			}
		}
	}

	/**
	 * Publish a message to all registered publishers.
	 * 
	 * @param message
	 *            The message to be published.
	 */
	public void publishMessage(T message) {
		for (Publisher<T> publisher : publishers) {
			publisher.publish(message);
		}
	}

	/**
	 * Create an instance of the message.
	 * 
	 * @return an instance of the message
	 */
	public T newMessage() {
		if (!publishers.isEmpty()) {
			return publishers.get(0).newMessage();
		} else {
			throw new InteractiveSpacesException(
					"No publishers found to create a message");
		}
	}

	/**
	 * Shut down all publishers registered.
	 * 
	 * <p>
	 * This method does not need to be called if the node is shut down.
	 */
	public void shutdown() {
		for (Publisher<T> publisher : publishers) {
			publisher.shutdown();
		}
	}

	@Override
	public void onMasterRegistrationFailure(Publisher<T> publisher) {
		log.info(String
				.format("Publisher for topic %s has failed to register with the master",
						publisher.getTopicName()));
	}

	@Override
	public void onMasterRegistrationSuccess(Publisher<T> publisher) {
		log.info(String
				.format("Publisher for topic %s has successfully registered with the master",
						publisher.getTopicName()));
	}

	@Override
	public void onMasterUnregistrationFailure(Publisher<T> publisher) {
		log.info(String
				.format("Publisher for topic %s has failed to unregister with the master",
						publisher.getTopicName()));
	}

	@Override
	public void onMasterUnregistrationSuccess(Publisher<T> publisher) {
		log.info(String
				.format("Publisher for topic %s has successfully unregistered with the master",
						publisher.getTopicName()));
	}

	@Override
	public void onNewSubscriber(Publisher<T> publisher,
			SubscriberIdentifier subscriberIdentifier) {
		log.info(String.format(
				"Publisher for topic %s has a new subscriber %s",
				publisher.getTopicName(),
				subscriberIdentifier.getNodeIdentifier()));
	}

	@Override
	public void onShutdown(Publisher<T> publisher) {
		log.info(String.format("Publisher for topic %s has shut down",
				publisher.getTopicName()));
	}

}
