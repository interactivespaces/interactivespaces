/*
 * Copyright (C) 2013 Google Inc.
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

package interactivespaces.activity.component.route;

/**
 * A listener for events from a {@link MessageRouterActivityComponent}.
 *
 * @author Keith M. Hughes
 */
public interface MessageRouterActivityComponentListener {

  /**
   * A new subscriber has come in for a topic the router is a publisher for.
   *
   * @param topicName
   *          the topic name for the route topic
   * @param subscriberIdentifier
   *          the identifier for the subscriber
   */
  void onNewSubscriber(String topicName, String subscriberIdentifier);

  /**
   * A new publisher has come in for a topic the router is a publisher for.
   *
   * @param topicName
   *          the topic name for the route topic
   * @param publisherIdentifier
   *          the identifier for the subscriber
   */
  void onNewPublisher(String topicName, String publisherIdentifier);
}
