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

import org.ros.node.ConnectedNode;
import org.ros.node.topic.PublisherListener;

import java.util.Set;

/**
 * A collection of ROS publishers for a given message type.
 *
 * @param <T>
 *      the message type
 *
 * @author Keith M. Hughes
 */
public interface RosPublishers<T> extends PublisherListener<T> {

  /**
   * Add a publisher listener to the collection.
   *
   * @param listener
   *          the listener to add
   */
  void addPublisherListener(PublisherListener<T> listener);

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
   *          the node the publishers will be added to
   * @param topicNames
   *         the topics to be published to
   * @param messageType
   *          the message type for all of the publishers
   */
  void addPublishers(ConnectedNode node, String messageType, Set<String> topicNames);

  /**
   * Add a series of publishers to a node.
   *
   * <p>
   * All topic names will be resolved using the ROS name resolver on the node.
   *
   * @param node
   *          the node the publishers will be added to
   * @param topicNames
   *         the topics to be published to
   * @param messageType
   *          the message type for all of the publishers
   * @param latch
   *          {@code true} if the publisher should always send the last message
   *          sent to any new subscribers
   */
  void addPublishers(ConnectedNode node, String messageType, Set<String> topicNames, boolean latch);

  /**
   * Publish a message to all registered publishers.
   *
   * @param message
   *          The message to be published.
   */
  void publishMessage(T message);

  /**
   * Create an instance of the message.
   *
   * @return an instance of the message
   */
  T newMessage();

  /**
   * Shut down all publishers registered.
   *
   * <p>
   * This method does not need to be called if the node is shut down.
   */
  void shutdown();
}
