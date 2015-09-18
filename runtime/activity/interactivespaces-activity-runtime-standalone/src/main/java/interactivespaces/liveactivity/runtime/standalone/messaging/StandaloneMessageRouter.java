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

package interactivespaces.liveactivity.runtime.standalone.messaging;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.activity.component.ros.RosActivityComponent;
import interactivespaces.activity.component.route.MessageRouterSupportedMessageTypes;
import interactivespaces.activity.component.route.RoutableInputMessageListener;
import interactivespaces.activity.component.route.ros.BaseRosMessageRouterActivityComponent;
import interactivespaces.activity.component.route.ros.RosMessageRouterActivityComponent;
import interactivespaces.activity.impl.SupportedActivity;
import interactivespaces.configuration.Configuration;
import interactivespaces.liveactivity.runtime.standalone.development.DevelopmentStandaloneLiveActivityRuntime;
import interactivespaces.liveactivity.runtime.standalone.messaging.MessageUtils.MessageMap;
import interactivespaces.liveactivity.runtime.standalone.messaging.MessageUtils.MessageSetList;
import interactivespaces.messaging.route.RouteMessagePublisher;
import interactivespaces.time.TimeProvider;
import interactivespaces.util.data.json.JsonMapper;
import interactivespaces.util.data.json.StandardJsonMapper;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import org.apache.commons.logging.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import interactivespaces_msgs.GenericMessage;

/**
 * A standalone message router that uses multicast.
 *
 * @author Trevor Pering
 */
public class StandaloneMessageRouter extends BaseRosMessageRouterActivityComponent<GenericMessage> {

  /**
   * Json mapper for message conversion.
   */
  private static final JsonMapper MAPPER = StandardJsonMapper.INSTANCE;

  /**
   * Key for message delay field.
   */
  public static final String TIME_DELAY_KEY = "delay";

  /**
   * Key for message source uuid field.
   */
  public static final String SOURCE_UUID_KEY = "sourceUuid";

  /**
   * Key for message segment name.
   */
  public static final String SEGMENT_KEY = "segment";

  /**
   * Key for message content.
   */
  public static final String MESSAGE_KEY = "message";

  /**
   * Router for, well, route messages.
   */
  private StandaloneRouter router;

  /**
   * Array to hold last message timestamps.
   */
  private long[] lastMessageTime = { -1, -1 };

  /**
   * List of whitelisted messages for trace capture.
   */
  private MessageSetList messageWhiteList = new MessageSetList();

  /**
   * The standalone activity runner that owns this message router.
   */
  private final DevelopmentStandaloneLiveActivityRuntime activityRunner;

  /**
   * A route input message listener.
   */
  private RoutableInputMessageListener<GenericMessage> listener;

  /**
   * Output for tracing received messages.
   */
  private PrintWriter receiveTraceWriter;

  /**
   * Output for tracing sent messages.
   */
  private PrintWriter sendTraceWriter;

  /**
   * Runner for checking produced messages.
   */
  private MessageCheckRunner messageCheckRunner;

  /**
   * Runner for checking playback messages.
   */
  private PlaybackRunner playbackRunner;

  /**
   * The activity that this router is supporting.
   */
  private SupportedActivity activity;

  /**
   * Mark time of when to check for completion (failure), in milliseconds.
   */
  private volatile long finishTime;

  /**
   * All topic inputs.
   */
  private final Multimap<String, String> inputRoutesToChannels = ArrayListMultimap.create();

  /**
   * All topic outputs.
   */
  private final Map<String, Set<String>> outputChannelsToRoutes = Maps.newConcurrentMap();

  /**
   * Time provider for message timestamps.
   */
  private TimeProvider timeProvider;

  /**
   * Create a new message router.
   *
   * @param activityRunner
   *          activity runner that utilizes this router
   */
  public StandaloneMessageRouter(DevelopmentStandaloneLiveActivityRuntime activityRunner) {
    this.activityRunner = activityRunner;
  }

  @Override
  public String getNodeName() {
    // TODO(keith): For now only ROS is used for node names as ROS is our only router. Eventually change
    // to a more generic route name config parameter.
    return getComponentContext().getActivity().getConfiguration()
        .getPropertyString(RosActivityComponent.CONFIGURATION_ACTIVITY_ROS_NODE_NAME);
  }

  @Override
  public GenericMessage newMessage() {
    return new StandaloneGenericMessage();
  }

  @Override
  public void writeOutputMessage(String outputChannelId, GenericMessage message) {
    sendOutputMessage(outputChannelId, message.getType(), message.getMessage());
  }

  @Override
  public String getName() {
    // This isn't strictly accurate, but there should ideally be a general name for message router components.
    return RosMessageRouterActivityComponent.COMPONENT_NAME;
  }

  @Override
  @SuppressWarnings("unchecked")
  protected void onPreStartupComponent() {
    timeProvider = getComponentContext().getActivity().getSpaceEnvironment().getTimeProvider();
    activity = getComponentContext().getActivity();
    router = getRouter();
    listener = (RoutableInputMessageListener<GenericMessage>) activity;
  }

  @Override
  protected void onPostStartupComponent() {
    initializeCommunication();
    if (playbackRunner != null) {
      getComponentContext().getActivity().getSpaceEnvironment().getExecutorService().submit(playbackRunner);
    }
  }

  /**
   * Get the router to use for this standalone instance.
   *
   * @return the router
   */
  private StandaloneRouter getRouter() {
    Configuration configuration = activity.getConfiguration();
    boolean useLoopback = configuration.getPropertyBoolean("standalone.router.loopback", false);

    return useLoopback ? new LoopbackRouter(configuration) : new MulticastRouter(configuration);
  }

  @Override
  public void shutdownComponent() {
    try {
      router.shutdown();
      if (playbackRunner != null) {
        playbackRunner.stop();
      }
    } finally {
      activity = null;
      listener = null;
      playbackRunner = null;
    }
  }

  @Override
  public boolean isComponentRunning() {
    return router != null && router.isRunning();
  }

  /**
   * Initialize communication. Essentially opens the multicast socket and prepare.
   */
  private void initializeCommunication() {
    try {
      router.startup();

      activity.getManagedCommands().submit(new Runnable() {
        @Override
        public void run() {
          receiveLoop();
        }
      });

      if (!messageWhiteList.isEmpty()) {
        final boolean autoFlush = true;
        File receiveTraceFile = new File(activity.getActivityFilesystem().getLogDirectory(), "messages.recv");
        receiveTraceWriter = new PrintWriter(new FileOutputStream(receiveTraceFile), autoFlush);
        File sendTraceFile = new File(activity.getActivityFilesystem().getLogDirectory(), "messages.send");
        sendTraceWriter = new PrintWriter(new FileOutputStream(sendTraceFile), autoFlush);
      }
    } catch (Exception e) {
      throw new SimpleInteractiveSpacesException("While creating standalone message route", e);
    }
  }

  /**
   * Send an output message.
   *
   * @param channelName
   *          the channel name on which to send the message
   * @param type
   *          type of message to send
   * @param message
   *          message to send
   */
  private void sendOutputMessage(String channelName, String type, String message) {
    try {
      Set<String> routes = outputChannelsToRoutes.get(channelName);
      if (routes == null) {
        getLog().error("Attempt to send on unregistered output channel " + channelName);
        Set<String> unknown = Sets.newHashSet("unknown");
        outputChannelsToRoutes.put(channelName, unknown);
        routes = unknown;
      }

      for (String route : routes) {
        // This is horribly inefficient but preserves the right semantics. It's more
        // flexible to keep everything as JSON, instead as a string embedded in Json.
        Object baseMessage =
            (MessageRouterSupportedMessageTypes.JSON_MESSAGE_TYPE.equals(type)) ? MAPPER.parseObject(message)
                : message;

        MessageMap messageObject = new MessageMap();
        messageObject.put("message", baseMessage);
        messageObject.put("type", type);
        messageObject.put("route", route);
        messageObject.put("channel", channelName);
        messageObject.put(SOURCE_UUID_KEY, activity.getUuid());
        router.send(messageObject);

        if (messageCheckRunner != null) {
          if (messageCheckRunner.checkMessage(messageObject)) {
            messageCheckRunner.finalizeVerification();
          }
        }
      }
    } catch (Exception e) {
      throw new SimpleInteractiveSpacesException("While sending standalone message", e);
    }
  }

  /**
   * Trace a message.
   *
   * @param message
   *          message to trace
   * @param isSend
   *          {@code true} if this is a trace send, else receive
   */
  private void traceMessage(MessageMap message, boolean isSend) {
    MessageMap traceMessage = makeTraceMessage(message);
    if (traceMessage == null) {
      return;
    }

    traceMessage.remove(SOURCE_UUID_KEY);
    traceMessage.remove(SEGMENT_KEY);

    PrintWriter traceWriter = isSend ? sendTraceWriter : receiveTraceWriter;
    long now = getCurrentTimestamp();
    int timeIndex = isSend ? 0 : 1;
    if (lastMessageTime[timeIndex] < 0) {
      lastMessageTime[timeIndex] = now;
    }
    traceMessage.put(TIME_DELAY_KEY, (now - lastMessageTime[timeIndex]));
    lastMessageTime[timeIndex] = now;
    traceWriter.println(MAPPER.toString(traceMessage));
  }

  /**
   * Make a trace message.
   *
   * @param message
   *          message to trace
   *
   * @return message for tracing, or {@code null} for untraced message
   */
  MessageMap makeTraceMessage(MessageMap message) {
    return MessageUtils.makeTraceMessage(message, messageWhiteList);
  }

  /**
   * Receive loop for handling incoming messages.
   */
  private void receiveLoop() {
    while (true) {
      try {
        receiveMessage();
      } catch (Exception e) {
        if (messageCheckRunner != null && !messageCheckRunner.isActive()) {
          getLog().info("Receive loop exiting, verification complete");
          return;
        } else {
          throw e;
        }
      }
    }
  }

  /**
   * Receive and process a single message.
   */
  private void receiveMessage() {
    try {
      MessageMap messageObject = router.receive();
      if (messageObject == null) {
        return;
      }
      processMessage(messageObject, false);

      boolean isSelf = activity.getUuid().equals(messageObject.get(SOURCE_UUID_KEY));
      traceMessage(messageObject, isSelf);

    } catch (MessageWarning w) {
      getLog().warn(w.getMessage());
    } catch (Exception e) {
      throw new SimpleInteractiveSpacesException("Error handling receive message", e);
    }
  }

  /**
   * Process a message.
   *
   * @param messageObject
   *          message to process
   *
   * @param sendOnRoute
   *          {@code true} if the result should be sent out on a route
   */
  void processMessage(MessageMap messageObject, boolean sendOnRoute) {
    String route = (String) messageObject.get("route");
    String type = (String) messageObject.get("type");

    if (route == null) {
      throw new IllegalStateException("Missing route in message");
    }

    Object rawMessage = messageObject.get("message");
    String message =
        (MessageRouterSupportedMessageTypes.JSON_MESSAGE_TYPE.equals(type)) ? MAPPER.toString(rawMessage)
            : (String) rawMessage;
    GenericMessage genericMessage = new StandaloneGenericMessage();
    genericMessage.setType(type);
    genericMessage.setMessage(message);

    if (sendOnRoute) {
      String channel = (String) messageObject.get("channel");
      writeOutputMessage(channel, genericMessage);
    } else {
      Collection<String> channels = inputRoutesToChannels.get(route);
      if (channels != null) {
        for (String channel : channels) {
          listener.onNewRoutableInputMessage(channel, genericMessage);
        }
      }
    }
  }

  /**
   * Setup message playback.
   *
   * @param playbackPath
   *          path of file containing playback messages
   * @param onRoute
   *          {@code true} if messages should be sent out on a route
   */
  public void playback(String playbackPath, boolean onRoute) {
    if (playbackRunner != null) {
      throw new InteractiveSpacesException("Multiple playback runners activated");
    }
    playbackRunner = new PlaybackRunner(this, new File(playbackPath), onRoute);
  }

  /**
   * Start a message check runner.
   *
   * @param checkPath
   *          path to file indicating messages to check
   */
  public void checkStart(String checkPath) {
    messageCheckRunner = new MessageCheckRunner(this, checkPath);
  }

  /**
   * Signal that injection is completed (check final state).
   */
  public void injectionFinished() {
    if (messageCheckRunner == null) {
      getLog().info("All messages sent with no message checking.");
      activityRunner.signalCompletion(true);
    } else {
      messageCheckRunner.finalizeVerification();
    }
  }

  /**
   * @return logger to use
   */
  public Log getLog() {
    return activityRunner.getLog();
  }

  /**
   * @return activity runner associated with this router
   */
  public DevelopmentStandaloneLiveActivityRuntime getActivityRunner() {
    return activityRunner;
  }

  /**
   * @return current timestamp to use for message processing
   */
  long getCurrentTimestamp() {
    return timeProvider.getCurrentTime();
  }

  /**
   * Set trace filter for messages.
   *
   * @param traceFilter
   *          path to trace filter file to use
   */
  public void setTraceFilter(String traceFilter) {
    File traceFilterFile = new File(traceFilter);
    messageWhiteList = MessageUtils.readMessageList(traceFilterFile);
  }

  /**
   * Get the finish time delta from current time until a finish check should be performed.
   *
   * @return delta in ms
   */
  public long getFinishDelta() {
    return finishTime - getCurrentTimestamp();
  }

  /**
   * Set the finish time delta from current time to next check for send finished.
   *
   * @param finishDelay
   *          delta in ms
   */
  public void setFinishDelta(long finishDelay) {
    this.finishTime = finishDelay + getCurrentTimestamp();
  }

  @Override
  public void handleError(String message, Throwable t) {
    if (getComponentContext() != null) {
      super.handleError(message, t);
    }
    activityRunner.handleError(message, t);
  }

  @Override
  public synchronized RouteMessagePublisher<GenericMessage> registerOutputChannelTopic(String outputChannelId,
      Set<String> topicNames, boolean latch) {
    if (outputChannelsToRoutes.containsKey(outputChannelId)) {
      throw new SimpleInteractiveSpacesException("Output channel already registered: " + outputChannelId);
    }

    if (latch) {
      throw new UnsupportedOperationException("Latch functionality not supported for output channel "
          + outputChannelId);
    }

    getComponentContext().getActivity().getLog()
        .warn(String.format("Registering output %s --> %s", outputChannelId, topicNames));
    outputChannelsToRoutes.put(outputChannelId, topicNames);

    return new StandaloneRouteMessagePublisher(outputChannelId);
  }

  @Override
  public synchronized void registerInputChannelTopic(String inputChannelId, Set<String> topicNames) {
    if (inputRoutesToChannels.values().contains(inputChannelId)) {
      SimpleInteractiveSpacesException.throwFormattedException("Duplicate route entry for channel %s", inputChannelId);
    }
    for (String topicName : topicNames) {
      getComponentContext().getActivity().getLog()
          .warn(String.format("Registering input %s <-- %s", inputChannelId, topicNames));
      inputRoutesToChannels.put(topicName, inputChannelId);
    }
  }

  @Override
  public synchronized void clearAllChannelTopics() {
    getComponentContext().getActivity().getLog().warn("Clearing all channel topics");
    inputRoutesToChannels.clear();
    outputChannelsToRoutes.clear();
  }

  @Override
  public Set<String> getOutputChannelIds() {
    return Sets.newHashSet(outputChannelsToRoutes.keySet());
  }

  @Override
  public RouteMessagePublisher<GenericMessage> getMessagePublisher(String outputChannelId) {
    return new StandaloneRouteMessagePublisher(outputChannelId);
  }

  @Override
  public Set<String> getInputChannelIds() {
    return Sets.newHashSet(inputRoutesToChannels.keySet());
  }

  /**
   * A route message publisher for the standalone publisher.
   *
   * @author Keith M. Hughes
   */
  private class StandaloneRouteMessagePublisher implements RouteMessagePublisher<GenericMessage> {

    /**
     * The channel ID for the output channel.
     */
    private String channelId;

    /**
     * Construct a new publisher.
     *
     * @param channelId
     *          channel ID for the publisher
     */
    public StandaloneRouteMessagePublisher(String channelId) {
      this.channelId = channelId;
    }

    @Override
    public String getChannelId() {
      return channelId;
    }

    @Override
    public void writeOutputMessage(GenericMessage message) {
      StandaloneMessageRouter.this.writeOutputMessage(channelId, message);
    }

    @Override
    public GenericMessage newMessage() {
      return newMessage();
    }
  }
}
