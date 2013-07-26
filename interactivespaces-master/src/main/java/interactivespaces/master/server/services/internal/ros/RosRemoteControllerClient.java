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

package interactivespaces.master.server.services.internal.ros;

import com.google.common.collect.Lists;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.activity.ActivityState;
import interactivespaces.controller.common.ros.RosSpaceControllerSupport;
import interactivespaces.domain.basic.ActivityConfiguration;
import interactivespaces.domain.basic.ConfigurationParameter;
import interactivespaces.master.server.services.ActiveLiveActivity;
import interactivespaces.master.server.services.ActiveSpaceController;
import interactivespaces.master.server.services.RemoteControllerClient;
import interactivespaces.master.server.services.RemoteSpaceControllerClientListener;
import interactivespaces.master.server.services.internal.LiveActivityDeleteResult;
import interactivespaces.master.server.services.internal.LiveActivityInstallResult;
import interactivespaces.master.server.services.internal.RemoteControllerClientListenerHelper;

import interactivespaces_msgs.ActivityConfigurationParameterRequest;
import interactivespaces_msgs.ActivityConfigurationRequest;
import interactivespaces_msgs.ControllerActivityRuntimeRequest;
import interactivespaces_msgs.ControllerActivityStatus;
import interactivespaces_msgs.ControllerFullStatus;
import interactivespaces_msgs.ControllerRequest;
import interactivespaces_msgs.ControllerStatus;
import interactivespaces_msgs.InteractiveSpacesContainerResource;
import interactivespaces_msgs.LiveActivityDeleteRequest;
import interactivespaces_msgs.LiveActivityDeleteStatus;
import interactivespaces_msgs.LiveActivityDeployRequest;
import interactivespaces_msgs.LiveActivityDeployStatus;
import org.apache.commons.logging.Log;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.ros.message.MessageDeserializer;
import org.ros.message.MessageFactory;
import org.ros.message.MessageListener;
import org.ros.message.MessageSerializer;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.CountDownPublisherListener;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.Subscriber;

import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * A {@link RemoteControllerClient} which uses ROS.
 *
 * @author Keith M. Hughes
 */
public class RosRemoteControllerClient implements RemoteControllerClient {

  /**
   * Default number of milliseconds to wait for a controller connection.
   */
  public static final long CONTROLLER_CONNECTION_TIME_WAIT_DEFAULT = 5000;

  /**
   * Number of milliseconds to wait for a controller connect.
   */
  private long controllerConnectionTimeWait = CONTROLLER_CONNECTION_TIME_WAIT_DEFAULT;

  /**
   * Map of controller communicators keyed by the name of the remote ROS node.
   */
  private Map<String, SpaceControllerCommunicator> controllerCommunicators =
      new HashMap<String, RosRemoteControllerClient.SpaceControllerCommunicator>();

  /**
   * Helps with listeners for activity events.
   */
  private RemoteControllerClientListenerHelper remoteControllerClientListeners;

  /**
   * The ROS environment the client is running in.
   */
  private MasterRosContext masterRosContext;

  /**
   * The main ROS node for the master.
   */
  private ConnectedNode masterNode;

  /**
   * Message factory for ROS messages.
   */
  private MessageFactory rosMessageFactory;

  /**
   * Listener for all controller status message updates.
   */
  private MessageListener<ControllerStatus> controllerStatusListener;

  /**
   * Listener for all activity status message updates.
   */
  private MessageListener<ControllerActivityStatus> activityStatusListener;

  /**
   * Logger for the controller.
   */
  private Log log;

  /**
   * ROS message serializer for a live activity deployment request
   */
  private MessageSerializer<LiveActivityDeployRequest> liveActivityDeployRequestSerializer;

  /**
   * ROS message deserializer for a live activity deployment status
   */
  private MessageDeserializer<LiveActivityDeployStatus> liveActivityDeployStatusDeserializer;

  /**
   * ROS message serializer for a live activity delete request
   */
  private MessageSerializer<LiveActivityDeleteRequest> liveActivityDeleteRequestSerializer;

  /**
   * ROS message deserializer for a live activity delete status
   */
  private MessageDeserializer<LiveActivityDeleteStatus> liveActivityDeleteStatusDeserializer;

  /**
   * ROS message deserializer for the full controller status
   */
  private MessageDeserializer<ControllerFullStatus> controllerFullStatusDeserializer;

  /**
   * ROS message serializer for the full controller status
   */
  private MessageSerializer<ActivityConfigurationRequest> activityConfigurationRequestSerializer;

  @Override
  public void startup() {
    log.info("Starting up ROS remote controller");

    remoteControllerClientListeners = new RemoteControllerClientListenerHelper(log);

    masterNode = masterRosContext.getNode();
    rosMessageFactory = masterNode.getTopicMessageFactory();

    liveActivityDeployRequestSerializer =
        masterNode.getMessageSerializationFactory().newMessageSerializer(
            LiveActivityDeployRequest._TYPE);

    liveActivityDeployStatusDeserializer =
        masterNode.getMessageSerializationFactory().newMessageDeserializer(
            LiveActivityDeployStatus._TYPE);

    liveActivityDeleteRequestSerializer =
        masterNode.getMessageSerializationFactory().newMessageSerializer(
            LiveActivityDeleteRequest._TYPE);

    liveActivityDeleteStatusDeserializer =
        masterNode.getMessageSerializationFactory().newMessageDeserializer(
            LiveActivityDeleteStatus._TYPE);

    controllerFullStatusDeserializer =
        masterNode.getMessageSerializationFactory().newMessageDeserializer(
            ControllerFullStatus._TYPE);

    activityConfigurationRequestSerializer =
        masterNode.getMessageSerializationFactory().newMessageSerializer(
            ActivityConfigurationRequest._TYPE);

    controllerStatusListener = new MessageListener<ControllerStatus>() {
      @Override
      public void onNewMessage(ControllerStatus status) {
        handleRemoteControllerStatusUpdate(status);
      }
    };

    activityStatusListener = new MessageListener<ControllerActivityStatus>() {
      @Override
      public void onNewMessage(ControllerActivityStatus status) {
        handleRemoteActivityStatusUpdate(status);
      }
    };
  }

  @Override
  public void shutdown() {
    for (SpaceControllerCommunicator communicator : controllerCommunicators.values()) {
      communicator.shutdown();
    }
    controllerCommunicators.clear();

    remoteControllerClientListeners.clear();
  }

  @Override
  public void connect(ActiveSpaceController controller) {
    getCommunicator(controller, true);
  }

  @Override
  public void disconnect(ActiveSpaceController controller) {
    shutdownCommunicator(controller);
  }

  @Override
  public void requestShutdown(ActiveSpaceController controller) {
    sendControllerRequest(controller, ControllerRequest.OPERATION_SHUTDOWN_CONTROLLER);

    // Leave attached ROS control topic in place but should shutdown
    // heartbeat alarm (once there is one)
  }

  @Override
  public void requestStatus(ActiveSpaceController controller) {
    sendControllerRequest(controller, ControllerRequest.OPERATION_STATUS);
  }

  @Override
  public void shutdownAllActivities(ActiveSpaceController controller) {
    sendControllerRequest(controller, ControllerRequest.OPERATION_SHUTDOWN_ACTIVITIES);
  }

  @Override
  public void deployActivity(ActiveLiveActivity liveActivity, LiveActivityDeployRequest request) {
    ChannelBuffer serialize = newSerializeBuffer();
    liveActivityDeployRequestSerializer.serialize(request, serialize);
    sendControllerRequest(liveActivity.getActiveController(),
        ControllerRequest.OPERATION_DEPLOY_LIVE_ACTIVITY, serialize);
  }

  @Override
  public LiveActivityDeployRequest newLiveActivityDeployRequest() {
    return rosMessageFactory.newFromType(LiveActivityDeployRequest._TYPE);
  }

  @Override
  public InteractiveSpacesContainerResource newInteractiveSpacesContainerResource() {
    return rosMessageFactory.newFromType(InteractiveSpacesContainerResource._TYPE);
  }

  @Override
  public void deleteActivity(ActiveLiveActivity liveActivity, LiveActivityDeleteRequest request) {
    ChannelBuffer serialize = newSerializeBuffer();
    liveActivityDeleteRequestSerializer.serialize(request, serialize);
    sendControllerRequest(liveActivity.getActiveController(),
        ControllerRequest.OPERATION_DELETE_LIVE_ACTIVITY, serialize);
  }

  @Override
  public LiveActivityDeleteRequest newLiveActivityDeleteRequest() {
    return rosMessageFactory.newFromType(LiveActivityDeleteRequest._TYPE);
  }

  /**
   * Get a properly created serialization buffer.
   *
   * @return a properly created serialization buffer
   */
  public ChannelBuffer newSerializeBuffer() {
    ChannelBuffer serialize = ChannelBuffers.dynamicBuffer(ByteOrder.LITTLE_ENDIAN, 256);
    return serialize;
  }

  @Override
  public void fullConfigureLiveActivity(ActiveLiveActivity activity) {
    ArrayList<ActivityConfigurationParameterRequest> parameterRequests = Lists.newArrayList();
    ActivityConfiguration configuration = activity.getLiveActivity().getConfiguration();
    if (configuration != null) {
      for (ConfigurationParameter parameter : configuration.getParameters()) {
        ActivityConfigurationParameterRequest newParameter =
            rosMessageFactory.newFromType(ActivityConfigurationParameterRequest._TYPE);
        newParameter.setOperation(ActivityConfigurationParameterRequest.OPERATION_ADD);
        newParameter.setName(parameter.getName());
        newParameter.setValue(parameter.getValue());

        parameterRequests.add(newParameter);
      }
    }

    ActivityConfigurationRequest request =
        rosMessageFactory.newFromType(ActivityConfigurationRequest._TYPE);
    request.setParameters(parameterRequests);

    ChannelBuffer serialize = newSerializeBuffer();
    activityConfigurationRequestSerializer.serialize(request, serialize);

    sendActivityRuntimeRequest(activity, ControllerActivityRuntimeRequest.OPERATION_CONFIGURE,
        serialize);
  }

  @Override
  public void startupActivity(ActiveLiveActivity activity) {
    sendActivityRuntimeRequest(activity, ControllerActivityRuntimeRequest.OPERATION_STARTUP, null);
  }

  @Override
  public void activateActivity(ActiveLiveActivity activity) {
    sendActivityRuntimeRequest(activity, ControllerActivityRuntimeRequest.OPERATION_ACTIVATE, null);
  }

  @Override
  public void deactivateActivity(ActiveLiveActivity activity) {
    sendActivityRuntimeRequest(activity, ControllerActivityRuntimeRequest.OPERATION_DEACTIVATE,
        null);
  }

  @Override
  public void shutdownActivity(ActiveLiveActivity activity) {
    sendActivityRuntimeRequest(activity, ControllerActivityRuntimeRequest.OPERATION_SHUTDOWN, null);
  }

  @Override
  public void statusActivity(ActiveLiveActivity activity) {
    sendActivityRuntimeRequest(activity, ControllerActivityRuntimeRequest.OPERATION_STATUS, null);
  }

  /**
   * Send a controller request to a controller.
   *
   * <p>
   * The request is sent asynchronously.
   *
   * @param controller
   *          the controller the request is being sent to
   * @param operation
   *          the operation requested
   */
  private void sendControllerRequest(ActiveSpaceController controller, int operation) {
    sendControllerRequest(controller, operation, null);
  }

  /**
   * Send a controller request to a controller.
   *
   * <p>
   * The request is sent asynchronously.
   *
   * @param controller
   *          the controller the request is being sent to
   * @param operation
   *          the operation requested
   * @param payload
   *          any data to be sent with the request (can be {@code null})
   */
  private void sendControllerRequest(ActiveSpaceController controller, int operation,
      ChannelBuffer payload) {
    ControllerRequest request = rosMessageFactory.newFromType(ControllerRequest._TYPE);
    request.setOperation(operation);

    if (payload != null) {
      request.setPayload(payload);
    }

    SpaceControllerCommunicator communicator = getCommunicator(controller, true);

    communicator.sendControllerRequest(request);
  }

  /**
   * Send an activity runtime request to a controller.
   *
   * <p>
   * The request is sent asynchronously.
   *
   * @param activity
   *          The activity the request is being sent to.
   * @param operation
   *          The operation requested.
   */
  private void sendActivityRuntimeRequest(ActiveLiveActivity activity, int operation,
      ChannelBuffer data) {
    ControllerActivityRuntimeRequest request =
        rosMessageFactory.newFromType(ControllerActivityRuntimeRequest._TYPE);
    request.setUuid(activity.getLiveActivity().getUuid());

    if (data != null) {
      request.setData(data);
    }

    SpaceControllerCommunicator communicator =
        getCommunicator(activity.getActiveController(), true);

    request.setOperation(operation);

    communicator.sendActivityRuntimeRequest(request);
  }

  /**
   * Handle controller status updates.
   *
   * @param status
   *          The status update.
   */
  private void handleRemoteControllerStatusUpdate(ControllerStatus status) {
    switch (status.getStatus()) {
      case ControllerStatus.STATUS_HEARTBEAT:
        handleControllerHeartbeat(status);

        break;

      case ControllerStatus.STATUS_FULL:
        // A full status request will also be treated as a heartbeat event
        // since the controller will only respond if it is alive.
        handleControllerHeartbeat(status);

        ControllerFullStatus fullStatus =
            controllerFullStatusDeserializer.deserialize(status.getData());

        List<ControllerActivityStatus> activities = fullStatus.getActivities();
        if (log.isInfoEnabled()) {
          log.info(String.format("Received controller full status %s, %d activities",
              status.getUuid(), activities.size()));
        }
        for (ControllerActivityStatus activity : activities) {
          if (log.isInfoEnabled()) {
            log.info(String.format("\tActivity %s, %d\n", activity.getUuid(), activity.getStatus()));
          }
          handleRemoteActivityStatusUpdate(activity);
        }

        break;

      case ControllerStatus.STATUS_ACTIVITY_INSTALL:
        LiveActivityDeployStatus deployStatus =
            liveActivityDeployStatusDeserializer.deserialize(status.getData());

        LiveActivityInstallResult result;
        switch (deployStatus.getStatus()) {
          case LiveActivityDeployStatus.STATUS_SUCCESS:
            result = LiveActivityInstallResult.SUCCESS;
            break;

          default:
            result = LiveActivityInstallResult.FAIL;
        }

        remoteControllerClientListeners.signalActivityInstall(deployStatus.getUuid(), result);

        break;

      case ControllerStatus.STATUS_ACTIVITY_DELETE:
        LiveActivityDeleteStatus deleteStatus =
            liveActivityDeleteStatusDeserializer.deserialize(status.getData());

        LiveActivityDeleteResult deleteResult;
        switch (deleteStatus.getStatus()) {
          case LiveActivityDeleteStatus.STATUS_SUCCESS:
            deleteResult = LiveActivityDeleteResult.SUCCESS;
            break;

          case LiveActivityDeleteStatus.STATUS_DOESNT_EXIST:
            deleteResult = LiveActivityDeleteResult.DOESNT_EXIST;
            break;

          default:
            deleteResult = LiveActivityDeleteResult.FAIL;
        }

        remoteControllerClientListeners.signalActivityDelete(deleteStatus.getUuid(), deleteResult);

        break;

      default:
        log.warn(String.format("Unknown status type %d, for controller %s", status.getStatus(),
            status.getUuid()));
    }
  }

  /**
   * @param status
   */
  public void handleControllerHeartbeat(ControllerStatus status) {
    long timestamp = System.currentTimeMillis();
    remoteControllerClientListeners.signalSpaceControllerHeartbeat(status.getUuid(), timestamp);
  }

  /**
   * Handle activity status updates.
   *
   * @param status
   *          the status update
   */
  private void handleRemoteActivityStatusUpdate(ControllerActivityStatus status) {
    ActivityState newState;

    switch (status.getStatus()) {
      case ControllerActivityStatus.STATUS_UNKNOWN:
        newState = ActivityState.UNKNOWN;
        break;

      case ControllerActivityStatus.STATUS_DOESNT_EXIST:
        newState = ActivityState.DOESNT_EXIST;
        break;

      case ControllerActivityStatus.STATUS_DEPLOY_FAILURE:
        newState = ActivityState.DEPLOY_FAILURE;
        break;

      case ControllerActivityStatus.STATUS_READY:
        newState = ActivityState.READY;
        break;

      case ControllerActivityStatus.STATUS_STARTUP_FAILURE:
        newState = ActivityState.STARTUP_FAILURE;
        break;

      case ControllerActivityStatus.STATUS_RUNNING:
        newState = ActivityState.RUNNING;
        break;

      case ControllerActivityStatus.STATUS_ACTIVATE_FAILURE:
        newState = ActivityState.ACTIVATE_FAILURE;
        break;

      case ControllerActivityStatus.STATUS_ACTIVE:
        newState = ActivityState.ACTIVE;
        break;

      case ControllerActivityStatus.STATUS_DEACTIVATE_FAILURE:
        newState = ActivityState.DEACTIVATE_FAILURE;
        break;

      case ControllerActivityStatus.STATUS_SHUTDOWN_FAILURE:
        newState = ActivityState.SHUTDOWN_FAILURE;
        break;

      case ControllerActivityStatus.STATUS_CRASH:
        newState = ActivityState.CRASHED;
        break;

      case ControllerActivityStatus.STATUS_STARTUP_ATTEMPT:
        newState = ActivityState.STARTUP_ATTEMPT;
        break;

      case ControllerActivityStatus.STATUS_DEPLOY_ATTEMPT:
        newState = ActivityState.DEPLOY_ATTEMPT;
        break;

      case ControllerActivityStatus.STATUS_ACTIVATE_ATTEMPT:
        newState = ActivityState.ACTIVATE_ATTEMPT;
        break;

      case ControllerActivityStatus.STATUS_DEACTIVATE_ATTEMPT:
        newState = ActivityState.DEACTIVATE_ATTEMPT;
        break;

      case ControllerActivityStatus.STATUS_SHUTDOWN_ATTEMPT:
        newState = ActivityState.SHUTDOWN_ATTEMPT;
        break;

      default:
        newState = ActivityState.UNKNOWN;
    }

    if (log.isInfoEnabled()) {
      log.info(String.format("Remote activity %s has reported state %s", status.getUuid(), newState));
    }

    remoteControllerClientListeners.signalActivityStateChange(status.getUuid(), newState);
  }

  @Override
  public RemoteControllerClientListenerHelper getRemoteControllerClientListeners() {
    return remoteControllerClientListeners;
  }

  @Override
  public void addRemoteSpaceControllerClientListener(RemoteSpaceControllerClientListener listener) {
    remoteControllerClientListeners.addListener(listener);
  }

  @Override
  public void
      removeRemoteSpaceControllerClientListener(RemoteSpaceControllerClientListener listener) {
    remoteControllerClientListeners.removeListener(listener);
  }

  /**
   * Get the communicator for a given controller.
   *
   * @param controller
   *          The controller
   * @param create
   *          {@code true} is a communicator should be created if there is none
   *          associated with the controller.
   *
   * @return The communicator for the controller. Will be {@code null} if there
   *         is none and creation wasn't specified.
   */
  private SpaceControllerCommunicator getCommunicator(ActiveSpaceController controller,
      boolean create) {
    String remoteNode = controller.getController().getHostId();
    synchronized (controllerCommunicators) {
      SpaceControllerCommunicator communicator = controllerCommunicators.get(remoteNode);

      if (communicator == null) {
        communicator = new SpaceControllerCommunicator(controller);
        communicator.startup(masterRosContext.getNode(), remoteNode, controllerStatusListener,
            activityStatusListener);
        controllerCommunicators.put(remoteNode, communicator);

      }

      return communicator;
    }
  }

  /**
   * Shutdown the communicator for a given controller.
   *
   * <p>
   * The communicator is then removed from the communicator map.
   *
   * @param controller
   *          The controller
   */
  private void shutdownCommunicator(ActiveSpaceController controller) {
    String remoteNode = controller.getController().getHostId();
    SpaceControllerCommunicator communicator = null;
    synchronized (controllerCommunicators) {
      communicator = controllerCommunicators.remove(remoteNode);
    }

    if (communicator != null) {
      communicator.shutdown();
      log.info(String.format("Communicator for controller %s shutdown and removed", controller
          .getController().getUuid()));
    }
  }

  /**
   * @param rosEnvironment
   *          the rosEnvironment to set
   */
  public void setMasterRosContext(MasterRosContext masterRosContext) {
    this.masterRosContext = masterRosContext;
  }

  /**
   * @param log
   *          the log to set
   */
  public void setLog(Log log) {
    this.log = log;
  }

  /**
   * Bundles the subscribers and publishers for communication with a space
   * controller.
   *
   * @author Keith M. Hughes
   */
  public class SpaceControllerCommunicator {

    /**
     * The space controller we are the communicator for.
     */
    private ActiveSpaceController spaceController;

    /**
     * The publisher for activity runtime requests.
     */
    private Publisher<ControllerRequest> controllerRequestPublisher;

    /**
     * The subscriber for controller status updates.
     */
    private Subscriber<ControllerStatus> controllerStatusSubscriber;

    /**
     * activity The publisher for activity runtime requests.
     */
    private Publisher<ControllerActivityRuntimeRequest> activityRuntimeRequestPublisher;

    /**
     * The subscriber for activity status updates.
     */
    private Subscriber<ControllerActivityStatus> activityStatusSubscriber;

    /**
     * Publisher listener for publisher events.
     */
    private CountDownPublisherListener<ControllerRequest> publisherListener;

    public SpaceControllerCommunicator(ActiveSpaceController spaceController) {
      this.spaceController = spaceController;
    }

    /**
     * Start the communicator up.
     *
     * @param node
     *          the node which is running the communicator
     * @param remoteNode
     *          the remote node
     * @param activityStatusListener
     *          the listener for activity status messages
     */
    public void startup(ConnectedNode node, String remoteNode,
        MessageListener<ControllerStatus> controllerStatusListener,
        MessageListener<ControllerActivityStatus> activityStatusListener) {
      publisherListener = CountDownPublisherListener.newFromCounts(2, 2, 2, 2, 2);
      controllerStatusSubscriber =
          RosSpaceControllerSupport.getControllerStatusSubscriber(node, remoteNode,
              controllerStatusListener, null);
      controllerRequestPublisher =
          RosSpaceControllerSupport.getControllerRequestPublisher(node, remoteNode,
              publisherListener);

      activityStatusSubscriber =
          RosSpaceControllerSupport.getControllerActivityStatusSubscriber(node, remoteNode,
              activityStatusListener, null);
      activityRuntimeRequestPublisher =
          RosSpaceControllerSupport.getControllerActivityRuntimeRequestPublisher(node, remoteNode,
              publisherListener);

      remoteControllerClientListeners.signalSpaceControllerConnectAttempt(spaceController);
    }

    /**
     * Shut the communicator down.
     */
    public void shutdown() {
      controllerRequestPublisher.shutdown();
      controllerRequestPublisher = null;
      controllerStatusSubscriber.shutdown();
      controllerStatusSubscriber = null;
      activityRuntimeRequestPublisher.shutdown();
      activityRuntimeRequestPublisher = null;
      activityStatusSubscriber.shutdown();
      activityStatusSubscriber = null;

      remoteControllerClientListeners.signalSpaceControllerDisconnectAttempt(spaceController);
    }

    /**
     * Send a request to a controller.
     *
     * @param request
     *          The request to send.
     */
    public void sendControllerRequest(ControllerRequest request) {
      try {
        if (publisherListener.awaitNewSubscriber(controllerConnectionTimeWait,
            TimeUnit.MILLISECONDS)) {
          controllerRequestPublisher.publish(request);
        } else {
          throw new InteractiveSpacesException(String.format(
              "No connection to controller in %d milliseconds", controllerConnectionTimeWait));
        }
      } catch (InterruptedException e) {
        // TODO(keith): Decide what to do.
        log.warn("Controller request interrupted");
      }
    }

    /**
     * Send a runtime request for an activity.
     *
     * @param request
     *          The request to send.
     */
    public void sendActivityRuntimeRequest(ControllerActivityRuntimeRequest request) {
      try {
        if (publisherListener.awaitNewSubscriber(controllerConnectionTimeWait,
            TimeUnit.MILLISECONDS)) {
          activityRuntimeRequestPublisher.publish(request);
        } else {
          throw new InteractiveSpacesException(String.format(
              "No connection to controller in %d milliseconds", controllerConnectionTimeWait));
        }
      } catch (InterruptedException e) {
        // TODO(keith): Decide what to do.
        log.warn("Controller activity request interrupted");
      }
    }
  }
}
