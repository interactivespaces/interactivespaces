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
 * An activity component which supports route messaging.
 *
 * @param <T>
 *          class of messages
 *
 * @author Keith M. Hughes
 */
public interface MessageRouterActivityComponent<T> {

  /**
   * Separator for configuration values which allow multiple values.
   */
  String CONFIGURATION_VALUES_SEPARATOR = ":";

  /**
   * Configuration property for listing of input routes.
   */
  String CONFIGURATION_ROUTES_INPUTS = "space.activity.routes.inputs";

  /**
   * Configuration name prefix for route inputs.
   */
  String CONFIGURATION_ROUTE_INPUT_TOPIC_PREFIX = "space.activity.route.input.";

  /**
   * Configuration property for listing of input routes.
   */
  String CONFIGURATION_ROUTES_OUTPUTS = "space.activity.routes.outputs";

  /**
   * Configuration name prefix for route outputs.
   */
  String CONFIGURATION_ROUTE_OUTPUT_TOPIC_PREFIX = "space.activity.route.output.";

  /**
   * Create a new message to send.
   *
   * <p>
   * This will only work if there are output routes for the topic.
   *
   * @return the new message.
   */
  T newMessage();

  /**
   * Send out a message on one of the output channels.
   *
   * <p>
   * The message is dropped if there is no such channel, though it will be
   * logged
   *
   * @param outputChannelName
   *          name of the output channel
   * @param message
   *          message to send
   */
  void writeOutputMessage(String outputChannelName, T message);
}
