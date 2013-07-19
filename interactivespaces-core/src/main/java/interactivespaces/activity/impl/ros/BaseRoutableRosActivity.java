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

package interactivespaces.activity.impl.ros;

import interactivespaces.activity.Activity;
import interactivespaces.activity.component.ros.RosMessageRouterActivityComponent;
import interactivespaces.activity.component.ros.RoutableInputMessageListener;
import interactivespaces.activity.execution.ActivityMethodInvocation;
import interactivespaces.util.data.json.JsonBuilder;
import interactivespaces.util.data.json.JsonMapper;

import interactivespaces_msgs.GenericMessage;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

import java.io.IOException;
import java.util.Map;

/**
 * An {@link Activity} which provides a set of named input ROS topics and a set
 * of named output ROS topics which will communicate via strings or JSON.
 *
 * @author Keith M. Hughes
 */
public class BaseRoutableRosActivity extends BaseRosActivity {

  /**
   * The JSON mapper.
   */
  private static final JsonMapper MAPPER;

  static {
    MAPPER = new JsonMapper();
  }

  /**
   * Router for input and output messages.
   */
  private RosMessageRouterActivityComponent<GenericMessage> router;

  @Override
  public void commonActivitySetup() {
    super.commonActivitySetup();

    router =
        addActivityComponent(new RosMessageRouterActivityComponent<GenericMessage>(
            "interactivespaces_msgs/GenericMessage",
            new RoutableInputMessageListener<GenericMessage>() {

              @SuppressWarnings("unchecked")
              @Override
              public void onNewRoutableInputMessage(String channelName, GenericMessage message) {
                if ("json".equals(message.getType())) {
                  try {
                    callOnNewInputJson(channelName, message);
                  } catch (Exception e) {
                    getLog().error("Could not process input message", e);
                  }
                } else {
                  callOnNewInputString(channelName, message);
                }
              }
            }));
  }

  /**
   * Convert a map to a JSON string.
   *
   * @param chaName
   *          the name of the output channel to send the message on
   * @param message
   *          the message to send
   */
  public String jsonStringify(Map<String, Object> map) {
    return MAPPER.toString(map);
  }

  /**
   * Parse a JSON string and return the map.
   *
   * @param data
   *          the JSON string
   * @return the map for the string
   */
  public Map<String, Object> jsonParse(String data) {
    return MAPPER.parseObject(data);
  }

  /**
   * A new JSON message is coming in.
   *
   * @param channelName
   *          name of the input channel the message came in on
   * @param message
   *          the message that came in
   */
  public void onNewInputJson(String channelName, Map<String, Object> message) {
    // Default is to do nothing.
  }

  /**
   * A new string message is coming in.
   *
   * @param channelName
   *          name of the input channel the message came in on
   * @param message
   *          the message that came in
   */
  public void onNewInputString(String channelName, String message) {
    // Default is to do nothing.
  }

  /**
   * Send an output JSON message.
   *
   * @param channelName
   *          the name of the output channel to send the message on
   * @param message
   *          the message to send
   */
  public void sendOutputJson(String channelName, Map<String, Object> message) {
    GenericMessage outgoing = router.newMessage();

    try {
      outgoing.setType("json");
      outgoing.setMessage(MAPPER.toString(message));

      router.writeOutputMessage(channelName, outgoing);
    } catch (Exception e) {
      getLog().error(
          String.format("Could not write JSON message on output channel %s", channelName), e);
    }
  }

  /**
   * Send an output JSON message from a {@link JsonBuilder}.
   *
   * @param channelName
   *          the name of the output channel to send the message on
   * @param message
   *          the message to send
   */
  public void sendOutputJsonBuilder(String channelName, JsonBuilder message) {
    sendOutputJson(channelName, message.build());
  }

  /**
   * Send an output string message.
   *
   * @param channelName
   *          the name of the output channel to send the message on
   * @param message
   *          the message to send
   */
  public void sendOutputString(String channelName, String message) {
    GenericMessage outgoing = router.newMessage();
    try {
      outgoing.setType("string");
      outgoing.setMessage(message);

      router.writeOutputMessage(channelName, outgoing);
    } catch (Exception e) {
      getLog().error(String.format("Could not write message on output channel %s", channelName), e);
    }
  }

  /**
   * Call the {@link #onNewInputJson(String, Map)} method.
   *
   * @param channelName
   *          the name of the channel
   * @param message
   *          the message in JSON format
   *
   * @throws IOException
   * @throws JsonParseException
   * @throws JsonMappingException
   */
  private void callOnNewInputJson(String channelName, GenericMessage message) throws IOException,
      JsonParseException, JsonMappingException {
    ActivityMethodInvocation invocation = getExecutionContext().enterMethod();

    try {
      String msg = message.getMessage();

      onNewInputJson(channelName, MAPPER.parseObject(msg));
    } finally {
      getExecutionContext().exitMethod(invocation);
    }
  }

  /**
   * Call {@link #onNewInputString(String, String)} in a safe manner.
   *
   * @param channelName
   *          name of the channel
   * @param message
   *          message for the channel
   */
  private void callOnNewInputString(String channelName, GenericMessage message) {
    ActivityMethodInvocation invocation = getExecutionContext().enterMethod();

    try {
      onNewInputString(channelName, message.getMessage());
    } finally {
      getExecutionContext().exitMethod(invocation);
    }
  }
}
