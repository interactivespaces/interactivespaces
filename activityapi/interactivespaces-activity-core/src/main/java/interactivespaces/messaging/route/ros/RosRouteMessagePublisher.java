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

package interactivespaces.messaging.route.ros;

import interactivespaces.messaging.route.InternalRouteMessagePublisher;
import interactivespaces.util.ros.RosPublishers;

/**
 * A route message publisher for ROS.
 *
 * @param <T>
 *          the message type for the publisher
 *
 * @author Keith M. Hughes
 */
public class RosRouteMessagePublisher<T> implements InternalRouteMessagePublisher<T> {

  /**
   * The channel ID for this publisher.
   */
  private String channelId;

  /**
   * The publishers for this message publisher.
   */
  private RosPublishers<T> publishers;

  /**
   * Construct a new publisher.
   *
   * @param channelId
   *          the channel ID for the route
   * @param publishers
   *          the ROS publishers
   */
  public RosRouteMessagePublisher(String channelId, RosPublishers<T> publishers) {
    this.publishers = publishers;
  }

  @Override
  public String getChannelId() {
    return channelId;
  }

  @Override
  public void writeOutputMessage(T message) {
    publishers.publishMessage(message);
  }

  @Override
  public T newMessage() {
    return publishers.newMessage();
  }

  @Override
  public void shutdown() {
    publishers.shutdown();
  }
}
