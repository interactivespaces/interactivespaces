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

import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.activity.ActivityState;
import interactivespaces.activity.deployment.ActivityDeploymentRequest;
import interactivespaces.activity.deployment.LiveActivityDeploymentResponse;
import interactivespaces.activity.deployment.ros.RosDeploymentMessageTranslator;
import interactivespaces.container.resource.deployment.ContainerResourceDeploymentCommitRequest;
import interactivespaces.container.resource.deployment.ContainerResourceDeploymentCommitResponse;
import interactivespaces.container.resource.deployment.ContainerResourceDeploymentQueryRequest;
import interactivespaces.container.resource.deployment.ContainerResourceDeploymentQueryResponse;
import interactivespaces.controller.SpaceControllerStatus;
import interactivespaces.controller.client.master.RemoteActivityDeploymentManager;
import interactivespaces.controller.common.ros.RosSpaceControllerSupport;
import interactivespaces.domain.basic.ActivityConfiguration;
import interactivespaces.domain.basic.ConfigurationParameter;
import interactivespaces.domain.basic.SpaceControllerConfiguration;
import interactivespaces.master.server.services.ActiveLiveActivity;
import interactivespaces.master.server.services.ActiveSpaceController;
import interactivespaces.master.server.services.RemoteSpaceControllerClient;
import interactivespaces.master.server.services.RemoteSpaceControllerClientListener;
import interactivespaces.master.server.services.internal.DataBundleState;
import interactivespaces.master.server.services.internal.LiveActivityDeleteResult;
import interactivespaces.master.server.services.internal.MasterDataBundleManager;
import interactivespaces.master.server.services.internal.RemoteSpaceControllerClientListenerHelper;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import interactivespaces_msgs.ConfigurationParameterRequest;
import interactivespaces_msgs.ConfigurationRequest;
import interactivespaces_msgs.ContainerResourceCommitRequestMessage;
import interactivespaces_msgs.ContainerResourceCommitResponseMessage;
import interactivespaces_msgs.ContainerResourceQueryRequestMessage;
import interactivespaces_msgs.ContainerResourceQueryResponseMessage;
import interactivespaces_msgs.ControllerFullStatus;
import interactivespaces_msgs.ControllerRequest;
import interactivespaces_msgs.ControllerStatus;
import interactivespaces_msgs.LiveActivityDeleteRequest;
import interactivespaces_msgs.LiveActivityDeleteStatus;
import interactivespaces_msgs.LiveActivityDeployRequest;
import interactivespaces_msgs.LiveActivityDeployStatus;
import interactivespaces_msgs.LiveActivityRuntimeRequest;
import interactivespaces_msgs.LiveActivityRuntimeStatus;
import org.apache.commons.logging.Log;
import org.jboss.netty.buffer.ChannelBuffer;
import org.ros.message.MessageDeserializer;
import org.ros.message.MessageFactory;
import org.ros.message.MessageListener;
import org.ros.message.MessageSerializationFactory;
import org.ros.message.MessageSerializer;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.CountDownPublisherListener;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.Subscriber;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * A {@link RemoteSpaceControllerClient} which uses ROS.
 *
 * @author Keith M. Hughes
 */
public class RosRemoteSpaceControllerClient implements RemoteSpaceControllerClient {

  /**
   * Default number of milliseconds to wait for a controller connection.
   */
  public static final long CONTROLLER_CONNECTION_TIME_WAIT_DEFAULT = 5000;

  /**
   * Number of milliseconds to wait for a controller connect.
   */
  private final long controllerConnectionTimeWait = CONTROLLER_CONNECTION_TIME_WAIT_DEFAULT;

  /**
   * Map of controller communicators keyed by the name of the remote ROS node.
   */
  private final Map<String, SpaceControllerCommunicator> controllerCommunicators = Maps.newHashMap();

  /**
   * Helps with listeners for activity events.
   */
  private RemoteSpaceControllerClientListenerHelper remoteControllerClientListeners;

  /**
   * The ROS Master context the client is running in.
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
   * Data bundle manager for this controller.
   */
  private MasterDataBundleManager masterDataBundleManager;

  /**
   * The remote activity installation manager.
   */
  private RemoteActivityDeploymentManager remoteActivityDeploymentManager;

  /**
   * Listener for all controller status message updates.
   */
  private MessageListener<ControllerStatus> controllerStatusListener;

  /**
   * Logger for the controller.
   */
  private Log log;

  /**
   * ROS message serializer for a live activity runtime request.
   */
  private MessageSerializer<LiveActivityRuntimeRequest> liveActivityRuntimeRequestSerializer;

  /**
   * ROS message deserializer for a live activity runtime status.
   */
  private MessageDeserializer<LiveActivityRuntimeStatus> liveActivityRuntimeStatusDeserializer;

  /**
   * ROS message serializer for a live activity deployment request.
   */
  private MessageSerializer<LiveActivityDeployRequest> liveActivityDeployRequestSerializer;

  /**
   * ROS message deserializer for a live activity deployment status.
   */
  private MessageDeserializer<LiveActivityDeployStatus> liveActivityDeployStatusDeserializer;

  /**
   * ROS message serializer for a live activity delete request.
   */
  private MessageSerializer<LiveActivityDeleteRequest> liveActivityDeleteRequestSerializer;

  /**
   * ROS message deserializer for a live activity delete status.
   */
  private MessageDeserializer<LiveActivityDeleteStatus> liveActivityDeleteStatusDeserializer;

  /**
   * ROS message deserializer for the full controller status.
   */
  private MessageDeserializer<ControllerFullStatus> controllerFullStatusDeserializer;

  /**
   * ROS message serializer for the full controller status.
   */
  private MessageSerializer<ConfigurationRequest> configurationRequestSerializer;

  /**
   * ROS message serializer for the container deployment query request.
   */
  private MessageSerializer<ContainerResourceQueryRequestMessage> containerResourceQueryRequestSerializer;

  /**
   * ROS message deserializer for the container deployment query response.
   */
  private MessageDeserializer<ContainerResourceQueryResponseMessage> containerResourceQueryResponseDeserializer;

  /**
   * ROS message serializer for the container deployment commit request.
   */
  private MessageSerializer<ContainerResourceCommitRequestMessage> containerResourceCommitRequestSerializer;

  /**
   * ROS message deserializer for the container deployment commit response.
   */
  private MessageDeserializer<ContainerResourceCommitResponseMessage> containerResourceCommitResponseDeserializer;

  @Override
  public void startup() {
    log.info("Starting up ROS remote controller");

    remoteControllerClientListeners = new RemoteSpaceControllerClientListenerHelper(log);

    masterNode = masterRosContext.getMasterNode();
    rosMessageFactory = masterNode.getTopicMessageFactory();

    MessageSerializationFactory messageSerializationFactory = masterNode.getMessageSerializationFactory();
    liveActivityRuntimeRequestSerializer =
        messageSerializationFactory.newMessageSerializer(LiveActivityRuntimeRequest._TYPE);

    liveActivityRuntimeStatusDeserializer =
        messageSerializationFactory.newMessageDeserializer(LiveActivityRuntimeStatus._TYPE);

    liveActivityDeployRequestSerializer =
        messageSerializationFactory.newMessageSerializer(LiveActivityDeployRequest._TYPE);

    liveActivityDeployStatusDeserializer =
        messageSerializationFactory.newMessageDeserializer(LiveActivityDeployStatus._TYPE);

    liveActivityDeleteRequestSerializer =
        messageSerializationFactory.newMessageSerializer(LiveActivityDeleteRequest._TYPE);

    liveActivityDeleteStatusDeserializer =
        messageSerializationFactory.newMessageDeserializer(LiveActivityDeleteStatus._TYPE);

    controllerFullStatusDeserializer = messageSerializationFactory.newMessageDeserializer(ControllerFullStatus._TYPE);

    configurationRequestSerializer = messageSerializationFactory.newMessageSerializer(ConfigurationRequest._TYPE);

    containerResourceQueryRequestSerializer =
        messageSerializationFactory.newMessageSerializer(ContainerResourceQueryRequestMessage._TYPE);

    containerResourceQueryResponseDeserializer =
        messageSerializationFactory.newMessageDeserializer(ContainerResourceQueryResponseMessage._TYPE);

    containerResourceCommitRequestSerializer =
        messageSerializationFactory.newMessageSerializer(ContainerResourceCommitRequestMessage._TYPE);

    containerResourceCommitResponseDeserializer =
        messageSerializationFactory.newMessageDeserializer(ContainerResourceCommitResponseMessage._TYPE);

    controllerStatusListener = new MessageListener<ControllerStatus>() {
      @Override
      public void onNewMessage(ControllerStatus status) {
        handleRemoteControllerStatusUpdate(status);
      }
    };

    masterDataBundleManager.startup();
  }

  @Override
  public void shutdown() {
    for (SpaceControllerCommunicator communicator : controllerCommunicators.values()) {
      communicator.shutdown();
    }
    controllerCommunicators.clear();

    remoteControllerClientListeners.clear();

    masterDataBundleManager.shutdown();
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
    sendControllerRequest(controller, ControllerRequest.OPERATION_CONTROLLER_SHUTDOWN_CONTROLLER);

    // Leave attached ROS control topic in place but should shutdown
    // heartbeat alarm (once there is one)
  }

  @Override
  public void requestStatus(ActiveSpaceController controller) {
    sendControllerRequest(controller, ControllerRequest.OPERATION_CONTROLLER_STATUS);
  }

  @Override
  public void shutdownAllActivities(ActiveSpaceController controller) {
    sendControllerRequest(controller, ControllerRequest.OPERATION_CONTROLLER_SHUTDOWN_ACTIVITIES);
  }

  @Override
  public void configureSpaceController(ActiveSpaceController controller) {
    List<ConfigurationParameterRequest> parameterRequests = Lists.newArrayList();
    SpaceControllerConfiguration configuration = controller.getSpaceController().getConfiguration();
    if (configuration != null) {
      for (ConfigurationParameter parameter : configuration.getParameters()) {
        ConfigurationParameterRequest newParameter =
            rosMessageFactory.newFromType(ConfigurationParameterRequest._TYPE);
        newParameter.setOperation(ConfigurationParameterRequest.OPERATION_ADD);
        newParameter.setName(parameter.getName());
        newParameter.setValue(parameter.getValue());

        parameterRequests.add(newParameter);
      }
    }

    ConfigurationRequest request = rosMessageFactory.newFromType(ConfigurationRequest._TYPE);
    request.setParameters(parameterRequests);

    ChannelBuffer serialize = configurationRequestSerializer.serialize(request);

    sendControllerRequest(controller, ControllerRequest.OPERATION_CONTROLLER_CONFIGURE, serialize);
  }

  @Override
  public void deployActivity(ActiveLiveActivity liveActivity, ActivityDeploymentRequest request) {
    LiveActivityDeployRequest rosRequest = rosMessageFactory.newFromType(LiveActivityDeployRequest._TYPE);
    RosDeploymentMessageTranslator.serializeActivityDeploymentRequest(request, rosRequest);
    ChannelBuffer serialize = liveActivityDeployRequestSerializer.serialize(rosRequest);
    sendControllerRequest(liveActivity.getActiveController(),
        ControllerRequest.OPERATION_CONTROLLER_DEPLOY_LIVE_ACTIVITY, serialize);
  }

  @Override
  public void deleteActivity(ActiveLiveActivity liveActivity, LiveActivityDeleteRequest request) {
    ChannelBuffer serialize = liveActivityDeleteRequestSerializer.serialize(request);
    sendControllerRequest(liveActivity.getActiveController(),
        ControllerRequest.OPERATION_CONTROLLER_DELETE_LIVE_ACTIVITY, serialize);
  }

  @Override
  public void queryResourceDeployment(ActiveSpaceController controller, ContainerResourceDeploymentQueryRequest query) {
    ContainerResourceQueryRequestMessage rosMessage =
        rosMessageFactory.newFromType(ContainerResourceQueryRequestMessage._TYPE);
    RosDeploymentMessageTranslator.serializeResourceDeploymentQuery(query, rosMessage, rosMessageFactory);
    ChannelBuffer payload = containerResourceQueryRequestSerializer.serialize(rosMessage);
    sendControllerRequest(controller, ControllerRequest.OPERATION_CONTROLLER_RESOURCE_QUERY, payload);
  }

  @Override
  public void commitResourceDeployment(ActiveSpaceController controller,
      ContainerResourceDeploymentCommitRequest request) {
    ContainerResourceCommitRequestMessage rosMessage =
        rosMessageFactory.newFromType(ContainerResourceCommitRequestMessage._TYPE);
    RosDeploymentMessageTranslator.serializeResourceDeploymentCommit(request, rosMessage, rosMessageFactory);
    ChannelBuffer payload = containerResourceCommitRequestSerializer.serialize(rosMessage);
    sendControllerRequest(controller, ControllerRequest.OPERATION_CONTROLLER_RESOURCE_COMMIT, payload);
  }

  @Override
  public LiveActivityDeleteRequest newLiveActivityDeleteRequest() {
    return rosMessageFactory.newFromType(LiveActivityDeleteRequest._TYPE);
  }

  @Override
  public void cleanControllerTempData(ActiveSpaceController controller) {
    sendControllerRequest(controller, ControllerRequest.OPERATION_CONTROLLER_CLEAN_DATA_TMP);
  }

  @Override
  public void cleanControllerPermanentData(ActiveSpaceController controller) {
    sendControllerRequest(controller, ControllerRequest.OPERATION_CONTROLLER_CLEAN_DATA_PERMANENT);
  }

  @Override
  public void cleanControllerActivitiesTempData(ActiveSpaceController controller) {
    sendControllerRequest(controller, ControllerRequest.OPERATION_CONTROLLER_CLEAN_DATA_TMP_ACTIVITIES);
  }

  @Override
  public void cleanControllerActivitiesPermanentData(ActiveSpaceController controller) {
    sendControllerRequest(controller, ControllerRequest.OPERATION_CONTROLLER_CLEAN_DATA_PERMANENT_ACTIVITIES);
  }

  @Override
  public void captureControllerDataBundle(ActiveSpaceController controller) {
    masterDataBundleManager.captureControllerDataBundle(controller);
  }

  @Override
  public void restoreControllerDataBundle(ActiveSpaceController controller) {
    masterDataBundleManager.restoreControllerDataBundle(controller);
  }

  @Override
  public void fullConfigureLiveActivity(ActiveLiveActivity activity) {
    List<ConfigurationParameterRequest> parameterRequests = Lists.newArrayList();
    ActivityConfiguration configuration = activity.getLiveActivity().getConfiguration();
    if (configuration != null) {
      for (ConfigurationParameter parameter : configuration.getParameters()) {
        ConfigurationParameterRequest newParameter =
            rosMessageFactory.newFromType(ConfigurationParameterRequest._TYPE);
        newParameter.setOperation(ConfigurationParameterRequest.OPERATION_ADD);
        newParameter.setName(parameter.getName());
        newParameter.setValue(parameter.getValue());

        parameterRequests.add(newParameter);
      }
    }

    ConfigurationRequest request = rosMessageFactory.newFromType(ConfigurationRequest._TYPE);
    request.setParameters(parameterRequests);

    ChannelBuffer serialize = configurationRequestSerializer.serialize(request);

    sendActivityRuntimeRequest(activity, LiveActivityRuntimeRequest.OPERATION_LIVE_ACTIVITY_CONFIGURE, serialize);
  }

  @Override
  public void startupActivity(ActiveLiveActivity activity) {
    sendActivityRuntimeRequest(activity, LiveActivityRuntimeRequest.OPERATION_LIVE_ACTIVITY_STARTUP, null);
  }

  @Override
  public void activateActivity(ActiveLiveActivity activity) {
    sendActivityRuntimeRequest(activity, LiveActivityRuntimeRequest.OPERATION_LIVE_ACTIVITY_ACTIVATE, null);
  }

  @Override
  public void deactivateActivity(ActiveLiveActivity activity) {
    sendActivityRuntimeRequest(activity, LiveActivityRuntimeRequest.OPERATION_LIVE_ACTIVITY_DEACTIVATE, null);
  }

  @Override
  public void shutdownActivity(ActiveLiveActivity activity) {
    sendActivityRuntimeRequest(activity, LiveActivityRuntimeRequest.OPERATION_LIVE_ACTIVITY_SHUTDOWN, null);
  }

  @Override
  public void statusActivity(ActiveLiveActivity activity) {
    sendActivityRuntimeRequest(activity, LiveActivityRuntimeRequest.OPERATION_LIVE_ACTIVITY_STATUS, null);
  }

  @Override
  public void cleanActivityPermanentData(ActiveLiveActivity activity) {
    sendActivityRuntimeRequest(activity, LiveActivityRuntimeRequest.OPERATION_LIVE_ACTIVITY_CLEAN_DATA_PERMANENT, null);
  }

  @Override
  public void cleanActivityTempData(ActiveLiveActivity activity) {
    sendActivityRuntimeRequest(activity, LiveActivityRuntimeRequest.OPERATION_LIVE_ACTIVITY_CLEAN_DATA_TMP, null);
  }

  @Override
  public RemoteSpaceControllerClientListenerHelper registerRemoteActivityDeploymentManager(
      RemoteActivityDeploymentManager remoteActivityDeploymentManager) {
    this.remoteActivityDeploymentManager = remoteActivityDeploymentManager;

    return remoteControllerClientListeners;
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
  void sendControllerRequest(ActiveSpaceController controller, int operation, ChannelBuffer payload) {
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
   *          the activity the request is being sent to
   * @param operation
   *          the operation requested
   * @param payload
   *          the data to send
   */
  private void sendActivityRuntimeRequest(ActiveLiveActivity activity, int operation, ChannelBuffer payload) {
    LiveActivityRuntimeRequest request = rosMessageFactory.newFromType(LiveActivityRuntimeRequest._TYPE);
    request.setLiveActivityUuid(activity.getLiveActivity().getUuid());

    if (payload != null) {
      request.setPayload(payload);
    }

    request.setOperation(operation);

    sendControllerRequest(activity.getActiveController(),
        ControllerRequest.OPERATION_CONTROLLER_LIVE_ACTIVITY_RUNTIME_REQUEST,
        liveActivityRuntimeRequestSerializer.serialize(request));
  }

  /**
   * Handle controller status updates.
   *
   * @param status
   *          The status update.
   */
  private void handleRemoteControllerStatusUpdate(ControllerStatus status) {
    switch (status.getStatus()) {
      case ControllerStatus.STATUS_CONTROLLER_HEARTBEAT:
        handleControllerHeartbeat(status);

        break;

      case ControllerStatus.STATUS_CONTROLLER_FULL_STATUS:
        // A full status request will also be treated as a heartbeat event
        // since the controller will only respond if it is alive.
        handleControllerHeartbeat(status);

        ControllerFullStatus fullStatus = controllerFullStatusDeserializer.deserialize(status.getPayload());

        List<LiveActivityRuntimeStatus> liveActivityStatuses = fullStatus.getLiveActivityStatuses();
        if (log.isInfoEnabled()) {
          log.info(String.format("Received controller full status %s, %d activities", status.getControllerUuid(),
              liveActivityStatuses.size()));
        }
        for (LiveActivityRuntimeStatus liveActivityStatus : liveActivityStatuses) {
          if (log.isInfoEnabled()) {
            log.info(String.format("\tActivity %s, %d\n", liveActivityStatus.getUuid(), liveActivityStatus.getStatus()));
          }
          handleRemoteLiveActivityStatusUpdate(liveActivityStatus);
        }

        break;

      case ControllerStatus.STATUS_CONTROLLER_LIVE_ACTIVITY_RUNTIME_STATUS:
        LiveActivityRuntimeStatus liveActivityStatus =
            liveActivityRuntimeStatusDeserializer.deserialize(status.getPayload());
        if (log.isInfoEnabled()) {
          log.info(String.format("Activity status %s, %d\n", liveActivityStatus.getUuid(),
              liveActivityStatus.getStatus()));
        }
        handleRemoteLiveActivityStatusUpdate(liveActivityStatus);

        break;

      case ControllerStatus.STATUS_CONTROLLER_ACTIVITY_INSTALL:
        LiveActivityDeployStatus deployStatus = liveActivityDeployStatusDeserializer.deserialize(status.getPayload());

        LiveActivityDeploymentResponse dstatus =
            RosDeploymentMessageTranslator.deserializeDeploymentStatus(deployStatus);
        remoteActivityDeploymentManager.handleLiveDeployResult(dstatus);
        break;

      case ControllerStatus.STATUS_CONTROLLER_ACTIVITY_DELETE:
        LiveActivityDeleteStatus deleteStatus = liveActivityDeleteStatusDeserializer.deserialize(status.getPayload());

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

      case ControllerStatus.STATUS_CONTROLLER_DATA_CAPTURE:
        log.info("Received data capture response " + status.getStatusCode());
        DataBundleState captureState =
            SpaceControllerStatus.isSuccessDescription(status.getStatusCode()) ? DataBundleState.CAPTURE_RECEIVED
                : DataBundleState.CAPTURE_ERROR;
        remoteControllerClientListeners.signalDataBundleState(status.getControllerUuid(), captureState);
        break;

      case ControllerStatus.STATUS_CONTROLLER_DATA_RESTORE:
        log.info("Received data restore response " + status.getStatusCode());
        DataBundleState restoreState =
            SpaceControllerStatus.isSuccessDescription(status.getStatusCode()) ? DataBundleState.RESTORE_RECEIVED
                : DataBundleState.RESTORE_ERROR;
        remoteControllerClientListeners.signalDataBundleState(status.getControllerUuid(), restoreState);
        break;

      case ControllerStatus.STATUS_CONTROLLER_CONTAINER_RESOURCE_QUERY:
        handleContainerResourceQueryResponse(containerResourceQueryResponseDeserializer.deserialize(status
            .getPayload()));
        break;

      case ControllerStatus.STATUS_CONTROLLER_CONTAINER_RESOURCE_COMMIT:
        handleContainerResourceCommitResponse(containerResourceCommitResponseDeserializer.deserialize(status
            .getPayload()));
        break;

      default:
        log.warn(String.format("Unknown status type %d, for controller %s", status.getStatus(),
            status.getControllerUuid()));
    }
  }

  /**
   * Handle a controller heartbeat message.
   *
   * @param status
   *          the status message for the heartbeat
   */
  private void handleControllerHeartbeat(ControllerStatus status) {
    long timestamp = System.currentTimeMillis();
    remoteControllerClientListeners.signalSpaceControllerHeartbeat(status.getControllerUuid(), timestamp);
  }

  /**
   * Handle activity status updates.
   *
   * @param status
   *          the status update
   */
  private void handleRemoteLiveActivityStatusUpdate(LiveActivityRuntimeStatus status) {
    ActivityState newState;

    switch (status.getStatus()) {
      case LiveActivityRuntimeStatus.STATUS_LIVE_ACTIVITY_UNKNOWN:
        newState = ActivityState.UNKNOWN;
        break;

      case LiveActivityRuntimeStatus.STATUS_LIVE_ACTIVITY_DOESNT_EXIST:
        newState = ActivityState.DOESNT_EXIST;
        break;

      case LiveActivityRuntimeStatus.STATUS_LIVE_ACTIVITY_DEPLOY_FAILURE:
        newState = ActivityState.DEPLOY_FAILURE;
        break;

      case LiveActivityRuntimeStatus.STATUS_LIVE_ACTIVITY_READY:
        newState = ActivityState.READY;
        break;

      case LiveActivityRuntimeStatus.STATUS_LIVE_ACTIVITY_STARTUP_FAILURE:
        newState = ActivityState.STARTUP_FAILURE;
        break;

      case LiveActivityRuntimeStatus.STATUS_LIVE_ACTIVITY_RUNNING:
        newState = ActivityState.RUNNING;
        break;

      case LiveActivityRuntimeStatus.STATUS_LIVE_ACTIVITY_ACTIVATE_FAILURE:
        newState = ActivityState.ACTIVATE_FAILURE;
        break;

      case LiveActivityRuntimeStatus.STATUS_LIVE_ACTIVITY_ACTIVE:
        newState = ActivityState.ACTIVE;
        break;

      case LiveActivityRuntimeStatus.STATUS_LIVE_ACTIVITY_DEACTIVATE_FAILURE:
        newState = ActivityState.DEACTIVATE_FAILURE;
        break;

      case LiveActivityRuntimeStatus.STATUS_LIVE_ACTIVITY_SHUTDOWN_FAILURE:
        newState = ActivityState.SHUTDOWN_FAILURE;
        break;

      case LiveActivityRuntimeStatus.STATUS_LIVE_ACTIVITY_CRASH:
        newState = ActivityState.CRASHED;
        break;

      case LiveActivityRuntimeStatus.STATUS_LIVE_ACTIVITY_STARTUP_ATTEMPT:
        newState = ActivityState.STARTUP_ATTEMPT;
        break;

      case LiveActivityRuntimeStatus.STATUS_LIVE_ACTIVITY_DEPLOY_ATTEMPT:
        newState = ActivityState.DEPLOY_ATTEMPT;
        break;

      case LiveActivityRuntimeStatus.STATUS_LIVE_ACTIVITY_ACTIVATE_ATTEMPT:
        newState = ActivityState.ACTIVATE_ATTEMPT;
        break;

      case LiveActivityRuntimeStatus.STATUS_LIVE_ACTIVITY_DEACTIVATE_ATTEMPT:
        newState = ActivityState.DEACTIVATE_ATTEMPT;
        break;

      case LiveActivityRuntimeStatus.STATUS_LIVE_ACTIVITY_SHUTDOWN_ATTEMPT:
        newState = ActivityState.SHUTDOWN_ATTEMPT;
        break;

      default:
        newState = ActivityState.UNKNOWN;
    }

    if (log.isInfoEnabled()) {
      log.info(String.format("Remote activity %s has reported state %s", status.getUuid(), newState));
    }

    remoteControllerClientListeners.signalActivityStateChange(status.getUuid(), newState, status.getStatusDetail());
  }

  /**
   * Handle a container resource deployment query response.
   *
   * @param rosResponse
   *          the ROS response
   */
  private void handleContainerResourceQueryResponse(ContainerResourceQueryResponseMessage rosResponse) {
    ContainerResourceDeploymentQueryResponse response =
        RosDeploymentMessageTranslator.deserializeResourceDeploymentQueryResponse(rosResponse);
    log.info(String.format("Got resource deployment query response for transaction ID %s with status %s",
        response.getTransactionId(), response.getStatus()));

    remoteActivityDeploymentManager.handleResourceDeploymentQueryResponse(response);
  }

  /**
   * Handle a container resource deployment commit response.
   *
   * @param rosResponse
   *          the ROS response
   */
  private void handleContainerResourceCommitResponse(ContainerResourceCommitResponseMessage rosResponse) {
    ContainerResourceDeploymentCommitResponse response =
        RosDeploymentMessageTranslator.deserializeResourceDeploymentCommitResponse(rosResponse);
    log.info(String.format("Got resource deployment commit response for transaction ID %s with status %s",
        response.getTransactionId(), response.getStatus()));

    remoteActivityDeploymentManager.handleResourceDeploymentCommitResponse(response);
  }

  @Override
  public RemoteSpaceControllerClientListenerHelper getRemoteControllerClientListeners() {
    return remoteControllerClientListeners;
  }

  @Override
  public void addRemoteSpaceControllerClientListener(RemoteSpaceControllerClientListener listener) {
    remoteControllerClientListeners.addListener(listener);
  }

  @Override
  public void removeRemoteSpaceControllerClientListener(RemoteSpaceControllerClientListener listener) {
    remoteControllerClientListeners.removeListener(listener);
  }

  /**
   * Get the communicator for a given controller.
   *
   * @param controller
   *          The controller
   * @param create
   *          {@code true} is a communicator should be created if there is none associated with the controller.
   *
   * @return The communicator for the controller. Will be {@code null} if there is none and creation wasn't specified.
   */
  private SpaceControllerCommunicator getCommunicator(ActiveSpaceController controller, boolean create) {
    String remoteNode = controller.getSpaceController().getHostId();
    synchronized (controllerCommunicators) {
      SpaceControllerCommunicator communicator = controllerCommunicators.get(remoteNode);

      if (communicator == null) {
        communicator = new SpaceControllerCommunicator(controller);
        communicator.startup(masterRosContext.getMasterNode(), remoteNode, controllerStatusListener);
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
    String remoteNode = controller.getSpaceController().getHostId();
    SpaceControllerCommunicator communicator = null;
    synchronized (controllerCommunicators) {
      communicator = controllerCommunicators.remove(remoteNode);
    }

    if (communicator != null) {
      communicator.shutdown();
      log.info(String.format("Communicator for controller %s shutdown and removed", controller.getSpaceController()
          .getUuid()));
    }
  }

  /**
   * Set the Master ROS context.
   *
   * @param masterRosContext
   *          the master ROS context
   */
  public void setMasterRosContext(MasterRosContext masterRosContext) {
    this.masterRosContext = masterRosContext;
  }

  /**
   * Set the logger.
   *
   * @param log
   *          the log to set
   */
  public void setLog(Log log) {
    this.log = log;
  }

  /**
   * Set the master's data bundle manager.
   *
   * @param masterDataBundleManager
   *          the data bundle manager to use, can be {@code null}
   */
  public void setMasterDataBundleManager(MasterDataBundleManager masterDataBundleManager) {
    this.masterDataBundleManager = masterDataBundleManager;
  }

  /**
   * Bundles the subscribers and publishers for communication with a space controller.
   *
   * @author Keith M. Hughes
   */
  public class SpaceControllerCommunicator {

    /**
     * The space controller we are the communicator for.
     */
    private final ActiveSpaceController spaceController;

    /**
     * The publisher for activity runtime requests.
     */
    private Publisher<ControllerRequest> controllerRequestPublisher;

    /**
     * The subscriber for controller status updates.
     */
    private Subscriber<ControllerStatus> controllerStatusSubscriber;

    /**
     * Publisher listener for publisher events.
     */
    private CountDownPublisherListener<ControllerRequest> publisherListener;

    /**
     * Construct a communicator.
     *
     * @param spaceController
     *          the space controller being communicated with
     */
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
     * @param controllerStatusListener
     *          the listener for controller status messages
     */
    public void startup(ConnectedNode node, String remoteNode,
        MessageListener<ControllerStatus> controllerStatusListener) {
      publisherListener = CountDownPublisherListener.newFromCounts(1, 1, 1, 1, 1);
      controllerStatusSubscriber =
          RosSpaceControllerSupport.getControllerStatusSubscriber(node, remoteNode, controllerStatusListener, null);
      controllerRequestPublisher =
          RosSpaceControllerSupport.getControllerRequestPublisher(node, remoteNode, publisherListener);

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

      remoteControllerClientListeners.signalSpaceControllerDisconnectAttempt(spaceController);
    }

    /**
     * Send a request to a controller.
     *
     * @param request
     *          the request to send
     */
    public void sendControllerRequest(ControllerRequest request) {
      try {
        if (publisherListener.awaitNewSubscriber(controllerConnectionTimeWait, TimeUnit.MILLISECONDS)) {
          controllerRequestPublisher.publish(request);
        } else {
          SimpleInteractiveSpacesException.throwFormattedException("No connection to controller in %d milliseconds",
              controllerConnectionTimeWait);
        }
      } catch (InterruptedException e) {
        // TODO(keith): Decide what to do.
        log.warn("Controller request interrupted");
      }
    }
  }
}
