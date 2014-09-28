package interactivespaces.controller.standalone.messaging;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.activity.SupportedActivity;
import interactivespaces.activity.component.BaseActivityComponent;
import interactivespaces.activity.component.route.MessageRouterActivityComponent;
import interactivespaces.activity.component.route.MessageRouterActivityComponentListener;
import interactivespaces.activity.component.route.MessageRouterSupportedMessageTypes;
import interactivespaces.activity.component.route.RoutableInputMessageListener;
import interactivespaces.activity.component.route.ros.RosMessageRouterActivityComponent;
import interactivespaces.configuration.Configuration;
import interactivespaces.controller.standalone.StandaloneActivityRunner;
import interactivespaces.time.TimeProvider;
import interactivespaces.util.data.json.JsonMapper;

import com.google.common.collect.Maps;
import interactivespaces_msgs.GenericMessage;
import org.apache.commons.logging.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Map;

/**
 * A standalone message router that uses multicast.
 *
 * @author Trevor Pering
 */
public class StandaloneMessageRouter extends BaseActivityComponent
    implements MessageRouterActivityComponent<GenericMessage> {

  /**
   * Json mapper for message conversion.
   */
  private static final JsonMapper MAPPER = new JsonMapper();

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
   * Router for, well, route messages.
   */
  private StandaloneRouter router;

  /**
   * Array to hold last message timestamps.
   */
  private long[] lastMessageTime = {-1, -1 };

  /**
   * List of whitelisted messages for trace capture.
   */
  private MessageUtils.MessageSetList messageWhiteList = new MessageUtils.MessageSetList();

  /**
   * The standalone activity runner that owns this message router.
   */
  private final StandaloneActivityRunner activityRunner;

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
   * Mark time of when to check for completion (failure).
   */
  private volatile long finishTimeMs;

  /**
   * All topic inputs.
   */
  private final Map<String, String> inputRoutesToChannels = Maps.newConcurrentMap();

  /**
   * All topic outputs.
   */
  private final Map<String, String> outputChannelsToRoutes = Maps.newConcurrentMap();

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
  public StandaloneMessageRouter(StandaloneActivityRunner activityRunner) {
    this.activityRunner = activityRunner;
  }

  @Override
  public GenericMessage newMessage() {
    return new StandaloneGenericMessage();
  }

  @Override
  public void writeOutputMessage(String outputChannelName, GenericMessage message) {
    sendOutputMessage(outputChannelName, message.getType(), message.getMessage());
  }

  @Override
  public void addListener(MessageRouterActivityComponentListener listener) {
    throw new UnsupportedOperationException("not implemented yet");
  }

  @Override
  public void removeListener(MessageRouterActivityComponentListener listener) {
    throw new UnsupportedOperationException("not implemented yet");
  }

  @Override
  public String getName() {
    // This isn't strictly accurate, but there should ideally be a general name for message router components.
    return RosMessageRouterActivityComponent.COMPONENT_NAME;
  }

  @Override
  @SuppressWarnings("unchecked")
  public void startupComponent() {
    timeProvider = getComponentContext().getActivity().getSpaceEnvironment().getTimeProvider();
    activity = getComponentContext().getActivity();
    router = getRouter();
    listener = (RoutableInputMessageListener<GenericMessage>) activity;
    initializeRoutes();
    initializeCommunication();
  }

  /**
   * @return router to use for this standalone instance
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
    return router.isRunning();
  }

  /**
   * Initialize communication.  Essentially opens the multiscast socket and prepare.
   */
  private void initializeCommunication() {
    try {
      router.startup();

      activity.getSpaceEnvironment().getExecutorService().submit(new Runnable() {
        @Override
        public void run() {
          receiveLoop();
        }
      });

      if (!messageWhiteList.isEmpty()) {
        boolean autoFlush = true;
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
      String route = outputChannelsToRoutes.get(channelName);
      if (route == null) {
        getLog().error("Attempt to send on unregistered output channel " + channelName);
        outputChannelsToRoutes.put(channelName, "unknown");
        route = "unknown";
      }

      // This is horribly inefficient but preserves the right semantics. It's more
      // flexible to keep everything as JSON, instead as a string embedded in Json...
      Object baseMessage = (MessageRouterSupportedMessageTypes.JSON_MESSAGE_TYPE.equals(type))
          ? MAPPER.parseObject(message) : message;

      MessageUtils.MessageMap messageObject = new MessageUtils.MessageMap();
      messageObject.put("message", baseMessage);
      messageObject.put("type", type);
      messageObject.put("route", route);
      messageObject.put("channel", channelName);
      messageObject.put(SOURCE_UUID_KEY, activity.getUuid());
      router.send(messageObject);

      if (messageCheckRunner != null) {
        if (messageCheckRunner.checkMessage(messageObject)) {
          messageCheckRunner.verifyFinished();
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
  private void traceMessage(MessageUtils.MessageMap message, boolean isSend) {
    MessageUtils.MessageMap traceMessage = makeTraceMessage(message);
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
  MessageUtils.MessageMap makeTraceMessage(MessageUtils.MessageMap message) {
    return MessageUtils.makeTraceMessage(message, messageWhiteList);
  }

  /**
   * Receive loop for handling incomming messages.
   */
  private void receiveLoop() {
    while (true) {
      receiveMessage();
    }
  }

  /**
   * Receive and process a single message.
   */
  private void receiveMessage() {
    try {
      MessageUtils.MessageMap messageObject = router.receive();
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
  void processMessage(MessageUtils.MessageMap messageObject, boolean sendOnRoute) {
    String route = (String) messageObject.get("route");
    String type = (String) messageObject.get("type");

    if (route == null) {
      throw new IllegalStateException("Missing route in message");
    }

    Object rawMessage = messageObject.get("message");
    String message = (MessageRouterSupportedMessageTypes.JSON_MESSAGE_TYPE.equals(type))
        ? MAPPER.toString(rawMessage) : (String) rawMessage;
    GenericMessage genericMessage = new StandaloneGenericMessage();
    genericMessage.setType(type);
    genericMessage.setMessage(message);

    if (sendOnRoute) {
      String channel = (String) messageObject.get("channel");
      writeOutputMessage(channel, genericMessage);
    } else {
      String channel = inputRoutesToChannels.get(route);
      if (channel != null) {
        listener.onNewRoutableInputMessage(channel, genericMessage);
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
    getComponentContext().getActivity().getSpaceEnvironment().getExecutorService().submit(playbackRunner);
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
   * Verify if the check runner has finished successfully.
   */
  public void verifyFinished() {
    if (messageCheckRunner == null) {
      getLog().info("All messages sent with no checked messages");
      activityRunner.signalCompletion(true);
    } else {
      messageCheckRunner.verifyFinished();
    }
  }

  /**
   * @return logger to use
   */
  public Log getLog() {
    return activity.getLog();
  }

  /**
   * @return activity runner associated with this router
   */
  public StandaloneActivityRunner getActivityRunner() {
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
   * @return delta in ms from current time until a finish check should be performed
   */
  public long getFinishDeltaMs() {
    return finishTimeMs - getCurrentTimestamp();
  }

  /**
   * @param finishDelayMs
   *          delta in ms from current time to next check for send finished
   */
  public void setFinishDeltaMs(long finishDelayMs) {
    this.finishTimeMs = finishDelayMs + getCurrentTimestamp();
  }

  @Override
  public void handleError(String message, Throwable t) {
    if (getComponentContext() != null) {
      super.handleError(message, t);
    }
    activityRunner.handleError(message, t);
  }

  /**
   * Initialize message routes based off of activity configuration.
   */
  private void initializeRoutes() {
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
        outputName = outputName.trim();
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
    if (outputChannelsToRoutes.containsKey(outputName)) {
      throw new SimpleInteractiveSpacesException("Output channel already registered: " + outputName);
    }

    if (latch) {
      throw new UnsupportedOperationException("Latch functionality not supported for output channel " + outputName);
    }

    getComponentContext().getActivity().getLog().warn(
        String.format("Registering output %s --> %s", outputName, topicNames));
    outputChannelsToRoutes.put(outputName, topicNames);
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
    if (inputRoutesToChannels.containsKey(topicNames)) {
      throw new SimpleInteractiveSpacesException("Input route already registered: " + inputName);
    }
    getComponentContext().getActivity().getLog().warn(
        String.format("Registering input %s <-- %s", inputName, topicNames));
    inputRoutesToChannels.put(topicNames, inputName);
  }

  @Override
  public synchronized void clearAllChannelTopics() {
    getComponentContext().getActivity().getLog().warn("Clearing all channel topics");
    inputRoutesToChannels.clear();
    outputChannelsToRoutes.clear();
  }
}
