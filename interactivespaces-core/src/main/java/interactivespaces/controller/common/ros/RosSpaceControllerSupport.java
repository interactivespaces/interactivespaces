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

package interactivespaces.controller.common.ros;

import interactivespaces_msgs.ControllerActivityRuntimeRequest;
import interactivespaces_msgs.ControllerActivityStatus;
import interactivespaces_msgs.ControllerRequest;
import interactivespaces_msgs.ControllerStatus;
import org.ros.message.MessageListener;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.PublisherListener;
import org.ros.node.topic.Subscriber;
import org.ros.node.topic.SubscriberListener;

/**
 * various support routines for working with Interactive Spaces controllers.
 *
 * @author Keith M. Hughes
 */
public class RosSpaceControllerSupport {

  /**
   * Get a subscriber for status update messages for controllers.
   *
   * @param node
   *          The node which will host the subscriber.
   * @param remoteNode
   *          The name of the remote node.
   * @param messageListener
   *          The message listener for the updates.
   * @param subscriberListener
   *          Listener for subscriber events. Can be {@code null}.
   *
   * @return The subscriber.
   */
  static public Subscriber<ControllerStatus> getControllerStatusSubscriber(ConnectedNode node,
      String remoteNode, MessageListener<ControllerStatus> messageListener,
      SubscriberListener<ControllerStatus> subscriberListener) {
    Subscriber<ControllerStatus> newSubscriber =
        node.newSubscriber("/" + remoteNode + "/"
            + RosSpaceControllerConstants.CONTROLLER_STATUS_TOPIC_NAME,
            RosSpaceControllerConstants.CONTROLLER_STATUS_MESSAGE_TYPE);
    newSubscriber.addMessageListener(messageListener);
    return newSubscriber;
  }

  /**
   * Get a publisher for controller requests.
   *
   * @param node
   *          The node which will host the publisher.
   * @param remoteNode
   *          The name of the remote node.
   * @param publisherListener
   *          A publisher listener. Can be {@code null}.
   *
   * @return The publisher.
   */
  static public Publisher<ControllerRequest> getControllerRequestPublisher(ConnectedNode node,
      String remoteNode, PublisherListener<ControllerRequest> publisherListener) {
    Publisher<ControllerRequest> newPublisher =
        node.newPublisher("/" + remoteNode + "/"
            + RosSpaceControllerConstants.CONTROLLER_REQUEST_TOPIC_NAME,
            RosSpaceControllerConstants.CONTROLLER_REQUEST_MESSAGE_TYPE);

    if (publisherListener != null) {
      newPublisher.addListener(publisherListener);
    }
    return newPublisher;
  }

  /**
   * Get a subscriber for status update messages for controller activities.
   *
   * @param node
   *          The node which will host the subscriber.
   * @param remoteNode
   *          The name of the remote node.
   * @param messageListener
   *          The message listener for the updates.
   * @param subscriberListener
   *          Listener for subscriber events. Can be {@code null}.
   *
   * @return The subscriber.
   */
  static public Subscriber<ControllerActivityStatus> getControllerActivityStatusSubscriber(
      ConnectedNode node, String remoteNode,
      MessageListener<ControllerActivityStatus> messageListener,
      SubscriberListener<ControllerActivityStatus> subscriberListener) {
    Subscriber<ControllerActivityStatus> newSubscriber =
        node.newSubscriber("/" + remoteNode + "/"
            + RosSpaceControllerConstants.ACTIVITY_RUNTIME_STATUS_TOPIC_NAME,
            RosSpaceControllerConstants.ACTIVITY_RUNTIME_STATUS_MESSAGE_TYPE);
    newSubscriber.addMessageListener(messageListener);

    if (subscriberListener != null) {
      newSubscriber.addSubscriberListener(subscriberListener);
    }

    return newSubscriber;
  }

  /**
   * Get a publisher for controller activity runtime requests.
   *
   * @param node
   *          the node which will host the subscriber
   * @param remoteNode
   *          the name of the remote node
   * @param publisherListener
   *          a publisher listener, can be {@code null}.
   *
   * @return The publisher.
   */
  static public Publisher<ControllerActivityRuntimeRequest>
      getControllerActivityRuntimeRequestPublisher(ConnectedNode node, String remoteNode,
          PublisherListener publisherListener) {
    Publisher<ControllerActivityRuntimeRequest> newPublisher =
        node.newPublisher("/" + remoteNode + "/"
            + RosSpaceControllerConstants.ACTIVITY_RUNTIME_REQUEST_TOPIC_NAME,
            RosSpaceControllerConstants.ACTIVITY_RUNTIME_REQUEST_MESSAGE_TYPE);

    if (publisherListener != null) {
      newPublisher.addListener(publisherListener);
    }

    return newPublisher;
  }

}
