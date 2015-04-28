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

import org.ros.message.MessageListener;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.SubscriberListener;

import java.util.Set;

/**
 * A collection of ROS subscribers.
 *
 * @param <T>
 *          the message type
 *
 * @author Keith M. Hughes
 */
public interface RosSubscribers<T> extends SubscriberListener<T> {

  /**
   * Add a subscriber listener to the collection.
   *
   * @param listener
   *          the listener to add
   */
  void addSubscriberListener(SubscriberListener<T> listener);

  /**
   * Add a series of subscribers to a node.
   *
   * <p>
   * All topic names will be resolved using the ROS name resolver on the node.
   *
   * @param node
   *          The node the subscribers will be added to.
   * @param messageType
   *          the message type for all of the subscribers
   * @param topicNames
   *          the topics to be subscribed to
   * @param listener
   *          The listener which all subscribers will call when a message is received.
   */
  void addSubscribers(ConnectedNode node, String messageType, Set<String> topicNames, MessageListener<T> listener);

  /**
   * Shut down all subscribers registered.
   *
   * <p>
   * This method does not need to be called if the node is shut down.
   */
  void shutdown();
}
