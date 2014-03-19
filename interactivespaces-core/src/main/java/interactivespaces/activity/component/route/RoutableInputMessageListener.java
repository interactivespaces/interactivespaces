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

package interactivespaces.activity.component.route;

import interactivespaces.activity.component.route.ros.RosMessageRouterActivityComponent;

/**
 * A listener for input messages from a
 * {@link RosMessageRouterActivityComponent} input topics.
 *
 * @param <T>
 *          the type of the messages being sent over the rdoute
 *
 * @author Keith M. Hughes
 */
public interface RoutableInputMessageListener<T> {

  /**
   * A new message has come in.
   *
   * @param channelName
   *          the name of the channel the message came in on
   * @param message
   *          the message which has come in
   */
  void onNewRoutableInputMessage(String channelName, T message);
}
