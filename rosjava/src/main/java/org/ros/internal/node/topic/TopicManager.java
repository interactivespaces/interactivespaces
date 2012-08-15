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

package org.ros.internal.node.topic;

import java.util.List;
import java.util.Map;

import org.ros.message.MessageDeserializer;
import org.ros.message.MessageSerializer;
import org.ros.namespace.GraphName;
import org.ros.node.topic.DefaultPublisherListener;
import org.ros.node.topic.DefaultSubscriberListener;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.Subscriber;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

/**
 * Manages a collection of {@link Publisher}s and {@link Subscriber}s.
 * 
 * @author kwc@willowgarage.com (Ken Conley)
 * @author damonkohler@google.com (Damon Kohler)
 */
public class TopicManager {

	/**
	 * A mapping from topic name to {@link Subscriber}.
	 */
	private final Map<GraphName, DefaultSubscriber<?>> subscribers;

	/**
	 * A mapping from topic name to {@link Publisher}.
	 */
	private final Map<GraphName, DefaultPublisher<?>> publishers;

	private PublisherFactory publisherFactory;

	private SubscriberFactory subscriberFactory;

	// TODO(damonkohler): Change to ListenerCollection.
	private TopicManagerListener listener;

	public TopicManager() {
		publishers = Maps.newConcurrentMap();
		subscribers = Maps.newConcurrentMap();
	}

	public void setListener(TopicManagerListener listener) {
		this.listener = listener;
	}

	public boolean hasSubscriber(String topicName) {
		return hasSubscriber(new GraphName(topicName));
	}

	public boolean hasSubscriber(GraphName topicName) {
		return subscribers.containsKey(topicName);
	}

	public boolean hasPublisher(String topicName) {
		return hasPublisher(new GraphName(topicName));
	}

	public boolean hasPublisher(GraphName topicName) {
		return publishers.containsKey(topicName);
	}

	public DefaultPublisher<?> getPublisher(String topicName) {
		return getPublisher(new GraphName(topicName));
	}

	public DefaultPublisher<?> getPublisher(GraphName topicName) {
		return publishers.get(topicName);
	}

	public <T> Publisher<T> newOrExistingPublisher(
			TopicDefinition topicDefinition,
			MessageSerializer<T> messageSerializer) {
		GraphName topicName = topicDefinition.getName();

		@SuppressWarnings("unchecked")
		DefaultPublisher<T> publisher = (DefaultPublisher<T>) getPublisher(topicName);
		if (publisher == null) {
			publisher = publisherFactory.newPublisher(topicDefinition,
					messageSerializer);
			publisher.addListener(new DefaultPublisherListener<T>() {
				@Override
				public void onShutdown(Publisher<T> publisher) {
					removePublisher((DefaultPublisher<T>) publisher);
				}
			});
			putPublisher(publisher);
		}
		return publisher;
	}

	public DefaultSubscriber<?> getSubscriber(String topicName) {
		return subscribers.get(new GraphName(topicName));
	}

	public <T> Subscriber<T> newOrExistingSubscriber(TopicDefinition topicDefinition,
			MessageDeserializer<T> messageDeserializer) {
		String topicName = topicDefinition.getName().toString();

		@SuppressWarnings("unchecked")
		DefaultSubscriber<T> subscriber = (DefaultSubscriber<T>) getSubscriber(topicName);

		if (subscriber == null) {
			subscriber = subscriberFactory.newSubscriber(topicDefinition,
					messageDeserializer);
			subscriber
					.addSubscriberListener(new DefaultSubscriberListener<T>() {
						@Override
						public void onShutdown(Subscriber<T> subscriber) {
							removeSubscriber((DefaultSubscriber<T>) subscriber);
						}
					});
			putSubscriber(subscriber);
		}

		return subscriber;
	}

	public void putPublisher(DefaultPublisher<?> publisher) {
		publishers.put(publisher.getTopicName(), publisher);
		if (listener != null) {
			listener.onPublisherAdded(publisher);
		}
	}

	public void removePublisher(DefaultPublisher<?> publisher) {
		publishers.remove(publisher.getTopicName());
		if (listener != null) {
			listener.onPublisherRemoved(publisher);
		}
	}

	public void putSubscriber(DefaultSubscriber<?> subscriber) {
		subscribers.put(subscriber.getTopicName(), subscriber);
		if (listener != null) {
			listener.onSubscriberAdded(subscriber);
		}
	}

	public void removeSubscriber(DefaultSubscriber<?> subscriber) {
		subscribers.remove(subscriber.getTopicName());
		if (listener != null) {
			listener.onSubscriberRemoved(subscriber);
		}
	}

	public List<DefaultSubscriber<?>> getSubscribers() {
		return ImmutableList.copyOf(subscribers.values());
	}

	public List<DefaultPublisher<?>> getPublishers() {
		return ImmutableList.copyOf(publishers.values());
	}

	/**
	 * @param publisherFactory
	 */
	public void setPublisherFactory(PublisherFactory publisherFactory) {
		this.publisherFactory = publisherFactory;
	}

	/**
	 * @param subscriberFactory
	 */
	public void setSubscriberFactory(SubscriberFactory subscriberFactory) {
		this.subscriberFactory = subscriberFactory;
	}
}
