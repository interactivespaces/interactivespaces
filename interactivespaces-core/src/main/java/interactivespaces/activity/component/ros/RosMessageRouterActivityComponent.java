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

package interactivespaces.activity.component.ros;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.activity.component.BaseActivityComponent;
import interactivespaces.activity.component.route.MessageRouterActivityComponent;
import interactivespaces.configuration.Configuration;
import interactivespaces.util.ros.RosPublishers;
import interactivespaces.util.ros.RosSubscribers;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.apache.commons.logging.Log;
import org.ros.message.MessageListener;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * An {@link ActivityComponent} instance which supports multiple named message
 * topics which can send and receive a given ROS message.
 *
 * @param <T>
 *          the type of messages handled by the component
 *
 * @author Keith M. Hughes
 */
public class RosMessageRouterActivityComponent<T> extends BaseActivityComponent implements
    MessageRouterActivityComponent<T> {

  /**
   * Name of the component.
   */
  public static final String COMPONENT_NAME = "comm.router.ros";

  /**
   * Dependencies for the component.
   */
  public static final List<String> COMPONENT_DEPENDENCIES = Collections.unmodifiableList(Lists
      .newArrayList(RosActivityComponent.COMPONENT_NAME));

  /**
   * The ROS activity component this component requires.
   */
  private RosActivityComponent rosActivityComponent;

  /**
   * {@code true} if the component is "running", false otherwise.
   */
  private volatile boolean running;

  /**
   * The listener for input messages.
   */
  private final RoutableInputMessageListener<T> messageListener;

  /**
   * The name of the ROS message type.
   */
  private final String rosMessageType;

  /**
   * All topic inputs
   */
  private final Map<String, RosSubscribers<T>> inputs = Maps.newConcurrentMap();

  /**
   * All topic outputs
   */
  private final Map<String, RosPublishers<T>> outputs = Maps.newConcurrentMap();

  /**
   * A publisher collection to be used as a message factory.
   *
   * <p>
   * Null if there are no outputs for this component.
   */
  private RosPublishers<T> messageFactory;

  /**
   * A creator for handler invocation IDs.
   */
  private final AtomicLong handlerInvocationId = new AtomicLong();

  /**
   * Construct a new ROS message router activity component.
   *
   * @param rosMessageType
   *          the ROS message type for the route
   * @param messageListener
   *          the listener for message events
   */
  public RosMessageRouterActivityComponent(String rosMessageType,
      RoutableInputMessageListener<T> messageListener) {
    this.rosMessageType = rosMessageType;
    this.messageListener = messageListener;
  }

  @Override
  public String getName() {
    return COMPONENT_NAME;
  }

  @Override
  public List<String> getDependencies() {
    return COMPONENT_DEPENDENCIES;
  }

  @Override
  public void configureComponent(Configuration configuration) {
    super.configureComponent(configuration);

    rosActivityComponent =
        componentContext.getActivityComponent(RosActivityComponent.COMPONENT_NAME);

    StringBuilder routeErrors = new StringBuilder();

    String inputNames = configuration.getPropertyString(CONFIGURATION_ROUTES_INPUTS);
    if (inputNames != null) {
      inputNames = inputNames.trim();
      for (String inputName : inputNames.split(CONFIGURATION_VALUES_SEPARATOR)) {
        String propertyName = CONFIGURATION_ROUTE_INPUT_TOPIC_PREFIX + inputName;
        if (configuration.getPropertyString(propertyName) == null) {
          handleError(String.format("Input route %s missing topic configuration %s", inputName, propertyName), null);
          routeErrors.append(routeErrors.length() > 0 ? ", " : "").append("missing input route=")
              .append(inputName).append(":").append(propertyName);
        }
      }
    }

    String outputNames = configuration.getPropertyString(CONFIGURATION_ROUTES_OUTPUTS);
    if (outputNames != null) {
      outputNames = outputNames.trim();
      for (String outputName : outputNames.split(CONFIGURATION_VALUES_SEPARATOR)) {
        String propertyName = CONFIGURATION_ROUTE_OUTPUT_TOPIC_PREFIX + outputName;
        if (configuration.getPropertyString(propertyName) == null) {
          handleError(String.format("Output route %s missing topic configuration %s", outputName, propertyName), null);
          routeErrors.append(routeErrors.length() > 0 ? ", " : "").append("missing output route=")
              .append(outputName).append(":").append(propertyName);
        }
      }
    }

    if (routeErrors.length() > 0) {
      throw new InteractiveSpacesException("Router has missing routes: " + routeErrors);
    }

    if ((inputNames == null || inputNames.isEmpty())
        && (outputNames == null || outputNames.isEmpty())) {
      throw new SimpleInteractiveSpacesException(String.format(
          "Router has no routes. Define either %s or %s in your configuration",
          CONFIGURATION_ROUTES_INPUTS, CONFIGURATION_ROUTES_OUTPUTS));
    }
  }

  @Override
  public void startupComponent() {
    Configuration configuration = getComponentContext().getActivity().getConfiguration();

    String inputNames = configuration.getPropertyString(CONFIGURATION_ROUTES_INPUTS);
    if (inputNames != null) {
      for (String inputName : inputNames.split(CONFIGURATION_VALUES_SEPARATOR)) {
        inputName = inputName.trim();
        if (!inputName.isEmpty()) {
          String inputTopicNames =
              configuration.getRequiredPropertyString(CONFIGURATION_ROUTE_INPUT_TOPIC_PREFIX
                  + inputName);
          registerInputChannelTopic(inputName, inputTopicNames);
        }
      }
    }

    String outputNames = configuration.getPropertyString(CONFIGURATION_ROUTES_OUTPUTS);
    if (outputNames != null) {
      for (String outputName : outputNames.split(CONFIGURATION_VALUES_SEPARATOR)) {
        outputName.trim();
        if (!outputName.isEmpty()) {
          String outputTopicNames =
              configuration.getRequiredPropertyString(CONFIGURATION_ROUTE_OUTPUT_TOPIC_PREFIX
                  + outputName);

          boolean latch = false;
          int semiPos = outputTopicNames.indexOf(';');
          if (semiPos != -1) {
            String extra = outputTopicNames.substring(0, semiPos);
            outputTopicNames = outputTopicNames.substring(semiPos + 1);

            String[] pair = extra.split("=");
            if (pair.length > 1) {
              if ("latch".equals(pair[0].trim())) {
                latch = "true".equals(pair[1].trim());
              }
            }
          }

          registerOutputChannelTopic(outputName, outputTopicNames, latch);
        }
      }
    }

    running = true;
  }

  /**
   * Register a new channel output topic route.
   *
   * @param outputName
   *          channel name
   * @param topicNames
   *          output topic names
   * @param latch
   *          should output be latched
   */
  public synchronized void registerOutputChannelTopic(String outputName,
      String topicNames, boolean latch) {
    if (outputs.containsKey(outputName)) {
      throw new SimpleInteractiveSpacesException("Output channel already registered: " + outputName);
    }
    RosPublishers<T> publishers =
        new RosPublishers<T>(getComponentContext().getActivity().getLog());
    outputs.put(outputName, publishers);

    // Only matters that we get one. They are all for the same
    // message type, so will take the last one.
    messageFactory = publishers;

    publishers.addPublishers(rosActivityComponent.getNode(), rosMessageType,
        topicNames, latch);
  }

  /**
   * Register a new input topic channel.
   *
   * @param inputName
   *          input channel name
   * @param topicNames
   *          input topic names
   */
  public synchronized void registerInputChannelTopic(final String inputName, String topicNames) {
    if (inputs.containsKey(inputName)) {
      throw new SimpleInteractiveSpacesException("Input channel already registered: " + inputName);
    }

    RosSubscribers<T> subscribers =
        new RosSubscribers<T>(getComponentContext().getActivity().getLog());
    inputs.put(inputName, subscribers);

    subscribers.addSubscribers(rosActivityComponent.getNode(), rosMessageType,
        topicNames, new MessageListener<T>() {
          @Override
          public void onNewMessage(T message) {
            handleNewMessage(inputName, message);
          }
        });
  }

  @Override
  public void shutdownComponent() {
    running = false;

    Log log = getComponentContext().getActivity().getLog();
    log.info("Shutting down ROS message router activity component");

    long timeStart = System.currentTimeMillis();

    clearAllChannelTopics();

    if (log.isInfoEnabled()) {
      log.info(String.format("ROS message router activity component shut down in %d msecs",
          System.currentTimeMillis() - timeStart));
    }
  }

  /**
   * Shutdown and clear all the input/output message topics.
   */
  public synchronized void clearAllChannelTopics() {
    for (RosSubscribers<T> input : inputs.values()) {
      input.shutdown();
    }
    inputs.clear();

    for (RosPublishers<T> output : outputs.values()) {
      output.shutdown();
    }
    outputs.clear();
  }

  @Override
  public boolean isComponentRunning() {
    return running;
  }

  @Override
  public T newMessage() {
    if (messageFactory != null) {
      return messageFactory.newMessage();
    } else {
      throw new SimpleInteractiveSpacesException(String.format(
          "No output routes declared so could not create a message of type %s", rosMessageType));
    }
  }

  /**
   * Handle a new route message.
   *
   * @param channelName
   *          name of the channel the message came in on
   * @param message
   *          the message that came in
   */
  void handleNewMessage(final String channelName, final T message) {
    if (!getComponentContext().canHandlerRun()) {
      return;
    }

    try {
      getComponentContext().enterHandler();

      long start = System.currentTimeMillis();
      String handlerInvocationId = newHandlerInvocationId();
      Log log = getComponentContext().getActivity().getLog();
      if (log.isDebugEnabled()) {
        log.debug(String.format("Entering ROS route message handler invocation %s",
            handlerInvocationId));
      }

      // Send the message out to the listener.
      messageListener.onNewRoutableInputMessage(channelName, message);

      if (log.isDebugEnabled()) {
        log.debug(String.format("Exiting ROS route message handler invocation %s in %d msecs",
            handlerInvocationId, System.currentTimeMillis() - start));
      }
    } catch (Throwable e) {
      handleError(String.format("Error after receiving routing message for channel %s", channelName), e);
    } finally {
      getComponentContext().exitHandler();
    }
  }

  @Override
  public void writeOutputMessage(final String outputChannelName, final T message) {
    try {
      getComponentContext().enterHandler();

      if (outputChannelName != null) {
        final RosPublishers<T> output = outputs.get(outputChannelName);
        if (output != null) {
          output.publishMessage(message);

        } else {
          handleError(String.format("Unknown route output channel %s. Message dropped.", outputChannelName), null);
        }
      } else {
        handleError("Route output channel has no name. Message dropped.", null);
      }
    } catch (Throwable e) {
      handleError(String.format("Error writing message on channel %s", outputChannelName), e);
    } finally {
      getComponentContext().exitHandler();
    }
  }

  /**
   * Create a new handler invocation ID.
   *
   * @return a unique ID for the handler invocation
   */
  private String newHandlerInvocationId() {
    return Long.toHexString(handlerInvocationId.getAndIncrement());
  }
}
