/*
 * Copyright (C) 2015 Google Inc.
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

import com.google.common.collect.Lists;

import org.apache.commons.logging.Log;
import org.ros.internal.node.topic.PublisherIdentifier;
import org.ros.message.MessageListener;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Subscriber;
import org.ros.node.topic.SubscriberListener;

import java.util.List;
import java.util.Set;

/**
 * A collection of ROS subscribers.
 *
 * @param <T>
 *          the message type
 *
 * @author Keith M. Hughes
 */
public class StandardRosSubscribers<T> implements RosSubscribers<T> {

  /**
   * All subscribers registered.
   */
  private final List<Subscriber<T>> subscribers = Lists.newArrayList();

  /**
   * All publisher listeners registered.
   */
  private final List<SubscriberListener<T>> subscriberListeners = Lists.newArrayList();

  /**
   * Logger for this collection.
   */
  private final Log log;

  /**
   * Construct a new subscribers collection.
   *
   * @param log
   *          the logger to use
   */
  public StandardRosSubscribers(Log log) {
    this.log = log;
  }

  @Override
  public synchronized void addSubscriberListener(SubscriberListener<T> listener) {
    subscriberListeners.add(listener);

    for (Subscriber<T> subscriber : subscribers) {
      subscriber.addSubscriberListener(listener);
    }
  }

  @Override
  public synchronized void addSubscribers(ConnectedNode node, String messageType, Set<String> topicNames,
      MessageListener<T> listener) {
    log.debug("Adding topic subscribers");
    for (String topicName : topicNames) {
      log.debug(String.format("Adding subscriber topic %s", topicName));
      Subscriber<T> newSubscriber = node.newSubscriber(topicName, messageType);
      log.debug(String.format("Added subscriber topic %s", topicName));
      newSubscriber.addSubscriberListener(this);

      for (SubscriberListener<T> subscriberListener : subscriberListeners) {
        newSubscriber.addSubscriberListener(subscriberListener);
      }

      newSubscriber.addMessageListener(listener);
      subscribers.add(newSubscriber);
    }
  }

  @Override
  public synchronized void shutdown() {
    for (Subscriber<T> subscriber : subscribers) {
      subscriber.shutdown();
    }
  }

  @Override
  public void onMasterRegistrationFailure(Subscriber<T> subscriber) {
    log.warn(String.format("Subscriber for topic %s has failed to register with the master", subscriber.getTopicName()));
  }

  @Override
  public void onMasterRegistrationSuccess(Subscriber<T> subscriber) {
    log.debug(String.format("Subscriber for topic %s has successfully registered with the master",
        subscriber.getTopicName()));
  }

  @Override
  public void onMasterUnregistrationFailure(Subscriber<T> subscriber) {
    log.warn(String.format("Subscriber for topic %s has failed to unregister with the master",
        subscriber.getTopicName()));
  }

  @Override
  public void onMasterUnregistrationSuccess(Subscriber<T> subscriber) {
    log.debug(String.format("Subscriber for topic %s has unregistered from the master", subscriber.getTopicName()));
  }

  @Override
  public void onNewPublisher(Subscriber<T> subscriber, PublisherIdentifier publisher) {
    log.debug(String.format("Subscriber for topic %s has a new publisher", subscriber.getTopicName()));
  }

  @Override
  public void onShutdown(Subscriber<T> subscriber) {
    log.debug(String.format("Subscriber for topic %s has shut down", subscriber.getTopicName()));
  }
}
