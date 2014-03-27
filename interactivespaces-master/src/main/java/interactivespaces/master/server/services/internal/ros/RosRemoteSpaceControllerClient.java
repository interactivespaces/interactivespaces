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

import interactivespaces.InteractiveSpacesException;
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

import interactivespaces_msgs.ActivityConfigurationParameterRequest;
import interactivespaces_msgs.ActivityConfigurationRequest;
import interactivespaces_msgs.ContainerResourceCommitRequestMessage;
import interactivespaces_msgs.ContainerResourceCommitResponseMessage;
import interactivespaces_msgs.ContainerResourceQueryRequestMessage;
import interactivespaces_msgs.ContainerResourceQueryResponseMessage;
import interactivespaces_msgs.ControllerActivityRuntimeRequest;
import interactivespaces_msgs.ControllerActivityStatus;
import interactivespaces_msgs.ControllerFullStatus;
import interactivespaces_msgs.ControllerRequest;
import interactivespaces_msgs.ControllerStatus;
import interactivespaces_msgs.LiveActivityDeleteRequest;
import interactivespaces_msgs.LiveActivityDeleteStatus;
import interactivespaces_msgs.LiveActivityDeployRequest;
import interactivespaces_msgs.LiveActivityDeployStatus;
import org.apache.commons.logging.Log;
import org.jboss.netty.buffer.ChannelBuffer;
import org.ros.message.MessageDeserializer;
import org.ros.message.MessageFactory;
import org.ros.message.MessageListener;
import org.ros.message.MessageSerializer;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.CountDownPublisherListener;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.Subscriber;

import java.util.ArrayList;
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
   * Listener for all activity status message updates.
   */
  private MessageListener<ControllerActivityStatus> activityStatusListener;

  /**
   * Logger for the controller.
   */
  private Log log;

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
  private MessageSerializer<ActivityConfigurationRequest> activityConfigurationRequestSerializer;

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

    masterNode = masterRosContext.getNode();
    rosMessageFactory = masterNode.getTopicMessageFactory();

    liveActivityDeployRequestSerializer =
        masterNode.getMessageSerializationFactory().newMessageSerializer(LiveActivityDeployRequest._TYPE);

    liveActivityDeployStatusDeserializer =
        masterNode.getMessageSerializationFactory().newMessageDeserializer(LiveActivityDeployStatus._TYPE);

    liveActivityDeleteRequestSerializer =
        masterNode.getMessageSerializationFactory().newMessageSerializer(LiveActivityDeleteRequest._TYPE);

    liveActivityDeleteStatusDeserializer =
        masterNode.getMessageSerializationFactory().newMessageDeserializer(LiveActivityDeleteStatus._TYPE);

    controllerFullStatusDeserializer =
        masterNode.getMessageSerializationFactory().newMessageDeserializer(ControllerFullStatus._TYPE);

    activityConfigurationRequestSerializer =
        masterNode.getMessageSerializationFactory().newMessageSerializer(ActivityConfigurationRequest._TYPE);

    containerResourceQueryRequestSerializer =
        masterNode.getMessageSerializationFactory().newMessageSerializer(ContainerResourceQueryRequestMessage._TYPE);

    containerResourceQueryResponseDeserializer =
        masterNode.getMessageSerializationFactory().newMessageDeserializer(ContainerResourceQueryResponseMessage._TYPE);

    containerResourceCommitRequestSerializer =
        masterNode.getMessageSerializationFactory().newMessageSerializer(ContainerResourceCommitRequestMessage._TYPE);

    containerResourceCommitResponseDeserializer =
        masterNode.getMessageSerializationFactory()
            .newMessageDeserializer(ContainerResourceCommitResponseMessage._TYPE);

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
  public void deployActivity(ActiveLiveActivity liveActivity, ActivityDeploymentRequest request) {
    LiveActivityDeployRequest rosRequest = rosMessageFactory.newFromType(LiveActivityDeployRequest._TYPE);
    RosDeploymentMessageTranslator.serializeActivityDeploymentRequest(request, rosRequest);
    ChannelBuffer serialize = liveActivityDeployRequestSerializer.serialize(rosRequest);
    sendControllerRequest(liveActivity.getActiveController(), ControllerRequest.OPERATION_DEPLOY_LIVE_ACTIVITY,
        serialize);
  }

  @Override
  public void deleteActivity(ActiveLiveActivity liveActivity, LiveActivityDeleteRequest request) {
    ChannelBuffer serialize = liveActivityDeleteRequestSerializer.serialize(request);
    sendControllerRequest(liveActivity.getActiveController(), ControllerRequest.OPERATION_DELETE_LIVE_ACTIVITY,
        serialize);
  }

  @Override
  public void queryResourceDeployment(ActiveSpaceController controller, ContainerResourceDeploymentQueryRequest query) {
    ContainerResourceQueryRequestMessage rosMessage =
        rosMessageFactory.newFromType(ContainerResourceQueryRequestMessage._TYPE);
    RosDeploymentMessageTranslator.serializeResourceDeploymentQuery(query, rosMessage, rosMessageFactory);
    ChannelBuffer payload = containerResourceQueryRequestSerializer.serialize(rosMessage);
    sendControllerRequest(controller, ControllerRequest.OPERATION_RESOURCE_QUERY, payload);
  }

  @Override
  public void commitResourceDeployment(ActiveSpaceController controller,
      ContainerResourceDeploymentCommitRequest request) {
    ContainerResourceCommitRequestMessage rosMessage =
        rosMessageFactory.newFromType(ContainerResourceCommitRequestMessage._TYPE);
    RosDeploymentMessageTranslator.serializeResourceDeploymentCommit(request, rosMessage, rosMessageFactory);
    ChannelBuffer payload = containerResourceCommitRequestSerializer.serialize(rosMessage);
    sendControllerRequest(controller, ControllerRequest.OPERATION_RESOURCE_COMMIT, payload);
  }

  @Override
  public LiveActivityDeleteRequest newLiveActivityDeleteRequest() {
    return rosMessageFactory.newFromType(LiveActivityDeleteRequest._TYPE);
  }

  @Override
  public void cleanControllerTempData(ActiveSpaceController controller) {
    sendControllerRequest(controller, ControllerRequest.OPERATION_CLEAN_DATA_TMP);
  }

  @Override
  public void cleanControllerPermanentData(ActiveSpaceController controller) {
    sendControllerRequest(controller, ControllerRequest.OPERATION_CLEAN_DATA_PERMANENT);
  }

  @Override
  public void cleanControllerActivitiesTempData(ActiveSpaceController controller) {
    sendControllerRequest(controller, ControllerRequest.OPERATION_CLEAN_DATA_TMP_ACTIVITIES);
  }

  @Override
  public void cleanControllerActivitiesPermanentData(ActiveSpaceController controller) {
    sendControllerRequest(controller, ControllerRequest.OPERATION_CLEAN_DATA_PERMANENT_ACTIVITIES);
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

    ActivityConfigurationRequest request = rosMessageFactory.newFromType(ActivityConfigurationRequest._TYPE);
    request.setParameters(parameterRequests);

    ChannelBuffer serialize = activityConfigurationRequestSerializer.serialize(request);

    sendActivityRuntimeRequest(activity, ControllerActivityRuntimeRequest.OPERATION_CONFIGURE, serialize);
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
    sendActivityRuntimeRequest(activity, ControllerActivityRuntimeRequest.OPERATION_DEACTIVATE, null);
  }

  @Override
  public void shutdownActivity(ActiveLiveActivity activity) {
    sendActivityRuntimeRequest(activity, ControllerActivityRuntimeRequest.OPERATION_SHUTDOWN, null);
  }

  @Override
  public void statusActivity(ActiveLiveActivity activity) {
    sendActivityRuntimeRequest(activity, ControllerActivityRuntimeRequest.OPERATION_STATUS, null);
  }

  @Override
  public void cleanActivityPermanentData(ActiveLiveActivity activity) {
    sendActivityRuntimeRequest(activity, ControllerActivityRuntimeRequest.OPERATION_CLEAN_DATA_PERMANENT, null);
  }

  @Override
  public void cleanActivityTempData(ActiveLiveActivity activity) {
    sendActivityRuntimeRequest(activity, ControllerActivityRuntimeRequest.OPERATION_CLEAN_DATA_TMP, null);
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
   * @param data
   *          the data to send
   */
  private void sendActivityRuntimeRequest(ActiveLiveActivity activity, int operation, ChannelBuffer data) {
    ControllerActivityRuntimeRequest request = rosMessageFactory.newFromType(ControllerActivityRuntimeRequest._TYPE);
    request.setUuid(activity.getLiveActivity().getUuid());

    if (data != null) {
      request.setData(data);
    }

    SpaceControllerCommunicator communicator = getCommunicator(activity.getActiveController(), true);

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

        ControllerFullStatus fullStatus = controllerFullStatusDeserializer.deserialize(status.getData());

        List<ControllerActivityStatus> activities = fullStatus.getActivities();
        if (log.isInfoEnabled()) {
          log.info(String.format("Received controller full status %s, %d activities", status.getUuid(),
              activities.size()));
        }
        for (ControllerActivityStatus activity : activities) {
          if (log.isInfoEnabled()) {
            log.info(String.format("\tActivity %s, %d\n", activity.getUuid(), activity.getStatus()));
          }
          handleRemoteActivityStatusUpdate(activity);
        }

        break;

      case ControllerStatus.STATUS_ACTIVITY_INSTALL:
        LiveActivityDeployStatus deployStatus = liveActivityDeployStatusDeserializer.deserialize(status.getData());

        LiveActivityDeploymentResponse dstatus =
            RosDeploymentMessageTranslator.deserializeDeploymentStatus(deployStatus);
        remoteActivityDeploymentManager.handleLiveDeployResult(dstatus);
        break;

      case ControllerStatus.STATUS_ACTIVITY_DELETE:
        LiveActivityDeleteStatus deleteStatus = liveActivityDeleteStatusDeserializer.deserialize(status.getData());

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

      case ControllerStatus.STATUS_DATA_CAPTURE:
        log.info("Received data capture response " + status.getStatusCode());
        DataBundleState captureState =
            SpaceControllerStatus.isSuccessDescription(status.getStatusCode()) ? DataBundleState.CAPTURE_RECEIVED
                : DataBundleState.CAPTURE_ERROR;
        remoteControllerClientListeners.signalDataBundleState(status.getUuid(), captureState);
        break;

      case ControllerStatus.STATUS_DATA_RESTORE:
        log.info("Received data restore response " + status.getStatusCode());
        DataBundleState restoreState =
            SpaceControllerStatus.isSuccessDescription(status.getStatusCode()) ? DataBundleState.RESTORE_RECEIVED
                : DataBundleState.RESTORE_ERROR;
        remoteControllerClientListeners.signalDataBundleState(status.getUuid(), restoreState);
        break;

      case ControllerStatus.STATUS_CONTAINER_RESOURCE_QUERY:
        handleContainerResourceQueryResponse(containerResourceQueryResponseDeserializer.deserialize(status.getData()));
        break;

      case ControllerStatus.STATUS_CONTAINER_RESOURCE_COMMIT:
        handleContainerResourceCommitResponse(containerResourceCommitResponseDeserializer.deserialize(status.getData()));
        break;

      default:
        log.warn(String.format("Unknown status type %d, for controller %s", status.getStatus(), status.getUuid()));
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
   *          {@code true} is a communicator should be created if there is none
   *          associated with the controller.
   *
   * @return The communicator for the controller. Will be {@code null} if there
   *         is none and creation wasn't specified.
   */
  private SpaceControllerCommunicator getCommunicator(ActiveSpaceController controller, boolean create) {
    String remoteNode = controller.getController().getHostId();
    synchronized (controllerCommunicators) {
      SpaceControllerCommunicator communicator = controllerCommunicators.get(remoteNode);

      if (communicator == null) {
        communicator = new SpaceControllerCommunicator(controller);
        communicator.startup(masterRosContext.getNode(), remoteNode, controllerStatusListener, activityStatusListener);
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
      log.info(String.format("Communicator for controller %s shutdown and removed", controller.getController()
          .getUuid()));
    }
  }

  /**
   * @param masterRosContext
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
   * Set the master's data bundle manager.
   *
   * @param masterDataBundleManager
   *          the data bundle manager to use, can be {@code null}
   */
  public void setMasterDataBundleManager(MasterDataBundleManager masterDataBundleManager) {
    this.masterDataBundleManager = masterDataBundleManager;
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
     * @param activityStatusListener
     *          the listener for activity status messages
     */
    public void startup(ConnectedNode node, String remoteNode,
        MessageListener<ControllerStatus> controllerStatusListener,
        MessageListener<ControllerActivityStatus> activityStatusListener) {
      publisherListener = CountDownPublisherListener.newFromCounts(2, 2, 2, 2, 2);
      controllerStatusSubscriber =
          RosSpaceControllerSupport.getControllerStatusSubscriber(node, remoteNode, controllerStatusListener, null);
      controllerRequestPublisher =
          RosSpaceControllerSupport.getControllerRequestPublisher(node, remoteNode, publisherListener);

      activityStatusSubscriber =
          RosSpaceControllerSupport.getControllerActivityStatusSubscriber(node, remoteNode, activityStatusListener,
              null);
      activityRuntimeRequestPublisher =
          RosSpaceControllerSupport.getControllerActivityRuntimeRequestPublisher(node, remoteNode, publisherListener);

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
     *          the request to send
     */
    public void sendControllerRequest(ControllerRequest request) {
      try {
        if (publisherListener.awaitNewSubscriber(controllerConnectionTimeWait, TimeUnit.MILLISECONDS)) {
          controllerRequestPublisher.publish(request);
        } else {
          throw new InteractiveSpacesException(String.format("No connection to controller in %d milliseconds",
              controllerConnectionTimeWait));
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
        if (publisherListener.awaitNewSubscriber(controllerConnectionTimeWait, TimeUnit.MILLISECONDS)) {
          activityRuntimeRequestPublisher.publish(request);
        } else {
          throw new InteractiveSpacesException(String.format("No connection to controller in %d milliseconds",
              controllerConnectionTimeWait));
        }
      } catch (InterruptedException e) {
        // TODO(keith): Decide what to do.
        log.warn("Controller activity request interrupted");
      }
    }
  }
}
