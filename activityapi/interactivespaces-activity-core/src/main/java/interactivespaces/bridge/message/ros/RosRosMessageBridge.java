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

package interactivespaces.bridge.message.ros;

import interactivespaces.bridge.message.MessageBridge;

import org.apache.commons.logging.Log;
import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.Subscriber;

/**
 * A message bridge which translates from ROS topics to ROS topics.
 *
 * @author Keith M. Hughes
 */
public class RosRosMessageBridge implements MessageBridge {

  /**
   * The ROS node the bridge is attached to.
   *
   * <p>
   * The bridge does not own the node, so should not shut it down.
   */
  private ConnectedNode node;

  /**
   * The bridge specification for the message bridge.
   */
  private RosMessageBridgeSpecification<Object, Object> bridgeSpecification;

  /**
   * The subscriber for the source messages.
   */
  private Subscriber<Object> subscriber;

  /**
   * The publisher for the destination messages.
   */
  private Publisher<Object> publisher;

  /**
   * Logger for messages.
   */
  private Log log;

  /**
   * Construct a bridge.
   *
   * @param node
   *          the node to attach the publishers and subscribers to, the bridge does not own the node
   * @param bridgeSpecification
   *          the specification for the bridge
   * @param log
   *          the logger for the bridge
   */
  public RosRosMessageBridge(ConnectedNode node, RosMessageBridgeSpecification<Object, Object> bridgeSpecification,
      Log log) {
    this.node = node;
    this.bridgeSpecification = bridgeSpecification;
    this.log = log;
  }

  @Override
  public void startup() {
    publisher =
        node.newPublisher(GraphName.of(bridgeSpecification.getDestinationTopicName()),
            bridgeSpecification.getDestinationTopicMessageType());

    subscriber =
        node.newSubscriber(GraphName.of(bridgeSpecification.getSourceTopicName()),
            bridgeSpecification.getSourceTopicMessageType());

    subscriber.addMessageListener(new MessageListener<Object>() {
      @Override
      public void onNewMessage(Object sourceMessage) {
        translateAndPublishMessage(sourceMessage);
      }
    });
  }

  @Override
  public void shutdown() {
    subscriber.shutdown();
    publisher.shutdown();
  }

  /**
   * Translate the source message and publish it to the destination.
   *
   * @param sourceMessage
   *          the message which came in from the source
   */
  private void translateAndPublishMessage(Object sourceMessage) {
    try {
      Object destinationMessage =
          node.getTopicMessageFactory().newFromType(bridgeSpecification.getDestinationTopicMessageType());

      bridgeSpecification.execute(sourceMessage, destinationMessage);

      publisher.publish(destinationMessage);
    } catch (Exception e) {
      log.error("Could not publish bridge message", e);
    }
  }
}
