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

package interactivespaces.controller.client.node.ros;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.activity.Activity;
import interactivespaces.activity.ActivityState;
import interactivespaces.activity.ActivityStatus;
import interactivespaces.activity.deployment.ActivityDeploymentRequest;
import interactivespaces.activity.deployment.LiveActivityDeploymentResponse;
import interactivespaces.activity.deployment.ros.RosDeploymentMessageTranslator;
import interactivespaces.container.resource.deployment.ContainerResourceDeploymentCommitRequest;
import interactivespaces.container.resource.deployment.ContainerResourceDeploymentCommitResponse;
import interactivespaces.container.resource.deployment.ContainerResourceDeploymentQueryRequest;
import interactivespaces.container.resource.deployment.ContainerResourceDeploymentQueryResponse;
import interactivespaces.controller.SpaceControllerStatus;
import interactivespaces.controller.client.node.SpaceControllerActivityInstallationManager;
import interactivespaces.controller.client.node.SpaceControllerCommunicator;
import interactivespaces.controller.client.node.SpaceControllerControl;
import interactivespaces.controller.client.node.SpaceControllerDataOperation;
import interactivespaces.controller.client.node.SpaceControllerHeartbeat;
import interactivespaces.controller.client.node.SpaceControllerLiveActivityDeleteRequest;
import interactivespaces.controller.client.node.SpaceControllerLiveActivityDeleteStatus;
import interactivespaces.controller.common.ros.RosSpaceControllerConstants;
import interactivespaces.controller.domain.InstalledLiveActivity;
import interactivespaces.controller.resource.deployment.ContainerResourceDeploymentManager;
import interactivespaces.domain.basic.pojo.SimpleSpaceController;
import interactivespaces.liveactivity.runtime.LiveActivityRunner;
import interactivespaces.master.server.remote.client.RemoteMasterServerClient;
import interactivespaces.master.server.remote.client.ros.RosRemoteMasterServerClient;
import interactivespaces.system.InteractiveSpacesEnvironment;
import interactivespaces.util.InteractiveSpacesUtilities;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;

import interactivespaces_msgs.ActivityConfigurationParameterRequest;
import interactivespaces_msgs.ActivityConfigurationRequest;
import interactivespaces_msgs.ContainerResourceCommitRequestMessage;
import interactivespaces_msgs.ContainerResourceCommitResponseMessage;
import interactivespaces_msgs.ContainerResourceQueryRequestMessage;
import interactivespaces_msgs.ContainerResourceQueryResponseMessage;
import interactivespaces_msgs.ControllerDataRequest;
import interactivespaces_msgs.ControllerFullStatus;
import interactivespaces_msgs.ControllerRequest;
import interactivespaces_msgs.ControllerStatus;
import interactivespaces_msgs.LiveActivityDeleteRequest;
import interactivespaces_msgs.LiveActivityDeleteStatus;
import interactivespaces_msgs.LiveActivityDeployRequest;
import interactivespaces_msgs.LiveActivityDeployStatus;
import interactivespaces_msgs.LiveActivityRuntimeRequest;
import interactivespaces_msgs.LiveActivityRuntimeStatus;
import org.jboss.netty.buffer.ChannelBuffer;
import org.ros.internal.node.topic.SubscriberIdentifier;
import org.ros.message.MessageDeserializer;
import org.ros.message.MessageFactory;
import org.ros.message.MessageListener;
import org.ros.message.MessageSerializationFactory;
import org.ros.message.MessageSerializer;
import org.ros.node.ConnectedNode;
import org.ros.node.NodeConfiguration;
import org.ros.node.topic.DefaultPublisherListener;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.PublisherListener;
import org.ros.node.topic.Subscriber;
import org.ros.osgi.common.RosEnvironment;

import java.util.Map;

/**
 * An {@link SpaceControllerCommunicator} using ROS for communication.
 *
 * @author Keith M. Hughes
 */
public class RosSpaceControllerCommunicator implements SpaceControllerCommunicator {

  /**
   * Startup delay for space controller startup notification. In milliseconds.
   */
  public static final int STARTUP_NOTIFICATION_DELAY = 1000;

  /**
   * The controller being controlled.
   */
  private SpaceControllerControl controllerControl;

  /**
   * The ROS environment this controller is running in.
   */
  private RosEnvironment rosEnvironment;

  /**
   * The node for the controller.
   */
  private ConnectedNode node;

  /**
   * Message factory for creating messages.
   */
  private MessageFactory rosMessageFactory;

  /**
   * Publisher for controller status.
   */
  private Publisher<ControllerStatus> controllerStatusPublisher;

  /**
   * Subscriber for controller requests.
   */
  private Subscriber<ControllerRequest> controllerRequestSubscriber;

  /**
   * A ROS message serialize for controller full status messages.
   */
  private MessageSerializer<ControllerFullStatus> controllerFullStatusMessageSerializer;

  /**
   * ROS message deserializer for live activity runtime requests.
   */
  private MessageDeserializer<LiveActivityRuntimeRequest> liveActivityRuntimeRequestDeserializer;

  /**
   * ROS message serializer for live activity runtime statuses.
   */
  private MessageSerializer<LiveActivityRuntimeStatus> liveActivityRuntimeStatusSerializer;

  /**
   * ROS message deserializer for live activity deployment requests.
   */
  private MessageDeserializer<LiveActivityDeployRequest> liveActivityDeployRequestDeserializer;

  /**
   * ROS message serializer for live activity deployment statuses.
   */
  private MessageSerializer<LiveActivityDeployStatus> liveActivityDeployStatusSerializer;

  /**
   * ROS message deserializer for live activity deletetion requests.
   */
  private MessageDeserializer<LiveActivityDeleteRequest> liveActivityDeleteRequestDeserializer;

  /**
   * ROS message serializer for live activity deletion statuses.
   */
  private MessageSerializer<LiveActivityDeleteStatus> liveActivityDeleteStatusSerializer;

  /**
   * ROS message deserializer for the activity configuration requests.
   */
  private MessageDeserializer<ActivityConfigurationRequest> liveActivityConfigurationRequestDeserializer;

  /**
   * ROS message deserializer for live activity deletion requests.
   */
  private MessageDeserializer<ControllerDataRequest> controllerDataRequestMessageDeserializer;

  /**
   * ROS message deserializer for container resource deployment query requests.
   */
  private MessageDeserializer<ContainerResourceQueryRequestMessage> containerResourceQueryRequestDeserializer;

  /**
   * ROS message serializer for container resource deployment query response.
   */
  private MessageSerializer<ContainerResourceQueryResponseMessage> containerResourceQueryResponseSerializer;

  /**
   * ROS message deserializer for container resource deployment commit requests.
   */
  private MessageDeserializer<ContainerResourceCommitRequestMessage> containerResourceCommitRequestDeserializer;

  /**
   * ROS message serializer for container resource deployment commit response.
   */
  private MessageSerializer<ContainerResourceCommitResponseMessage> containerResourceCommitResponseSerializer;

  /**
   * Activity installer for the controller.
   */
  private SpaceControllerActivityInstallationManager spaceControllerActivityInstallManager;

  /**
   * The container resource deployment manager.
   */
  private final ContainerResourceDeploymentManager containerResourceDeploymentManager;

  /**
   * The space environment for this communicator.
   */
  private InteractiveSpacesEnvironment spaceEnvironment;

  /**
   * Create a new space controller communicator.
   *
   * @param spaceControllerActivityInstaller
   *          activity installer
   * @param containerResourceDeploymentManager
   *          manager for deploying container resources
   * @param rosEnvironment
   *          ROS environment
   * @param spaceEnvironment
   *          space environment
   */
  public RosSpaceControllerCommunicator(SpaceControllerActivityInstallationManager spaceControllerActivityInstaller,
      ContainerResourceDeploymentManager containerResourceDeploymentManager, RosEnvironment rosEnvironment,
      InteractiveSpacesEnvironment spaceEnvironment) {
    this.spaceControllerActivityInstallManager = spaceControllerActivityInstaller;
    this.containerResourceDeploymentManager = containerResourceDeploymentManager;
    this.rosEnvironment = rosEnvironment;
    this.spaceEnvironment = spaceEnvironment;
  }

  @Override
  public void onStartup() {
    final NodeConfiguration nodeConfiguration = rosEnvironment.getPublicNodeConfigurationWithNodeName();
    nodeConfiguration.setNodeName("interactivespaces/controller");

    node = rosEnvironment.newNode(nodeConfiguration);
    rosMessageFactory = node.getTopicMessageFactory();

    PublisherListener<ControllerStatus> controllerStatusPublisherListener =
        new DefaultPublisherListener<ControllerStatus>() {
          @Override
          public void onNewSubscriber(Publisher<ControllerStatus> publisher, SubscriberIdentifier subscriber) {
            if (spaceEnvironment.getLog().isInfoEnabled()) {
              spaceEnvironment.getLog().info(
                  String.format("New subscriber for controller status %s", subscriber.getNodeIdentifier()));
            }
          }
        };

    controllerStatusPublisher =
        node.newPublisher(RosSpaceControllerConstants.CONTROLLER_STATUS_TOPIC_NAME,
            RosSpaceControllerConstants.CONTROLLER_STATUS_MESSAGE_TYPE);
    controllerStatusPublisher.addListener(controllerStatusPublisherListener);

    controllerRequestSubscriber =
        node.newSubscriber(RosSpaceControllerConstants.CONTROLLER_REQUEST_TOPIC_NAME,
            RosSpaceControllerConstants.CONTROLLER_REQUEST_MESSAGE_TYPE);
    controllerRequestSubscriber.addMessageListener(new MessageListener<ControllerRequest>() {
      @Override
      public void onNewMessage(ControllerRequest request) {
        handleControllerRequest(request);
      }
    });

    MessageSerializationFactory messageSerializationFactory = node.getMessageSerializationFactory();
    controllerFullStatusMessageSerializer =
        messageSerializationFactory.newMessageSerializer(ControllerFullStatus._TYPE);

    liveActivityRuntimeRequestDeserializer =
        messageSerializationFactory.newMessageDeserializer(LiveActivityRuntimeRequest._TYPE);

    liveActivityRuntimeStatusSerializer =
        messageSerializationFactory.newMessageSerializer(LiveActivityRuntimeStatus._TYPE);

    liveActivityConfigurationRequestDeserializer =
        messageSerializationFactory.newMessageDeserializer(ActivityConfigurationRequest._TYPE);

    liveActivityDeployStatusSerializer =
        messageSerializationFactory.newMessageSerializer(LiveActivityDeployStatus._TYPE);

    liveActivityDeployRequestDeserializer =
        messageSerializationFactory.newMessageDeserializer(LiveActivityDeployRequest._TYPE);

    liveActivityDeleteStatusSerializer =
        messageSerializationFactory.newMessageSerializer(LiveActivityDeleteStatus._TYPE);

    liveActivityDeleteRequestDeserializer =
        messageSerializationFactory.newMessageDeserializer(LiveActivityDeleteRequest._TYPE);

    controllerDataRequestMessageDeserializer =
        messageSerializationFactory.newMessageDeserializer(ControllerDataRequest._TYPE);

    containerResourceQueryRequestDeserializer =
        messageSerializationFactory.newMessageDeserializer(ContainerResourceQueryRequestMessage._TYPE);

    containerResourceQueryResponseSerializer =
        messageSerializationFactory.newMessageSerializer(ContainerResourceQueryResponseMessage._TYPE);

    containerResourceCommitRequestDeserializer =
        messageSerializationFactory.newMessageDeserializer(ContainerResourceCommitRequestMessage._TYPE);

    containerResourceCommitResponseSerializer =
        messageSerializationFactory.newMessageSerializer(ContainerResourceCommitResponseMessage._TYPE);
  }

  @Override
  public void notifyRemoteMasterServerAboutStartup(SimpleSpaceController controllerInfo) {
    RemoteMasterServerClient masterServerClient = new RosRemoteMasterServerClient();
    masterServerClient.startup(rosEnvironment);
    InteractiveSpacesUtilities.delay(STARTUP_NOTIFICATION_DELAY);

    masterServerClient.sendControllerDescription(controllerInfo);
    masterServerClient.shutdown();
  }

  @Override
  public void onShutdown() {
    if (node != null) {
      node.shutdown();
      node = null;
    }
  }

  @Override
  public SpaceControllerHeartbeat newSpaceControllerHeartbeat() {
    return new RosControllerHeartbeat();
  }

  /**
   * Handle a ROS controller control request coming in.
   *
   * @param request
   *          The ROS request.
   */
  @VisibleForTesting
  void handleControllerRequest(interactivespaces_msgs.ControllerRequest request) {
    switch (request.getOperation()) {
      case ControllerRequest.OPERATION_CONTROLLER_STATUS:
        publishControllerFullStatus();

        break;

      case ControllerRequest.OPERATION_CONTROLLER_SHUTDOWN_ACTIVITIES:
        controllerControl.shutdownAllActivities();

        break;

      case ControllerRequest.OPERATION_CONTROLLER_SHUTDOWN_CONTROLLER:
        controllerControl.shutdownControllerContainer();

        break;

      case ControllerRequest.OPERATION_CONTROLLER_DEPLOY_LIVE_ACTIVITY:
        handleLiveActivityDeployment(liveActivityDeployRequestDeserializer.deserialize(request.getPayload()));

        break;

      case ControllerRequest.OPERATION_CONTROLLER_DELETE_LIVE_ACTIVITY:
        handleLiveActivityDeletion(liveActivityDeleteRequestDeserializer.deserialize(request.getPayload()));

        break;

      case ControllerRequest.OPERATION_CONTROLLER_LIVE_ACTIVITY_RUNTIME_REQUEST:
        handleLiveActivityRuntimeRequest(liveActivityRuntimeRequestDeserializer.deserialize(request.getPayload()));

        break;

      case ControllerRequest.OPERATION_CONTROLLER_CLEAN_DATA_TMP:
        controllerControl.cleanControllerTempData();

        break;

      case ControllerRequest.OPERATION_CONTROLLER_CLEAN_DATA_PERMANENT:
        controllerControl.cleanControllerPermanentData();

        break;

      case ControllerRequest.OPERATION_CONTROLLER_CLEAN_DATA_TMP_ACTIVITIES:
        controllerControl.cleanControllerTempDataAll();

        break;

      case ControllerRequest.OPERATION_CONTROLLER_CLEAN_DATA_PERMANENT_ACTIVITIES:
        controllerControl.cleanControllerPermanentDataAll();

        break;

      case ControllerRequest.OPERATION_CONTROLLER_CAPTURE_DATA:
        ControllerDataRequest captureDataRequest =
            controllerDataRequestMessageDeserializer.deserialize(request.getPayload());
        controllerControl.captureControllerDataBundle(captureDataRequest.getTransferUri());
        break;

      case ControllerRequest.OPERATION_CONTROLLER_RESTORE_DATA:
        ControllerDataRequest restoreDataRequest =
            controllerDataRequestMessageDeserializer.deserialize(request.getPayload());
        controllerControl.restoreControllerDataBundle(restoreDataRequest.getTransferUri());
        break;

      case ControllerRequest.OPERATION_CONTROLLER_RESOURCE_QUERY:
        ContainerResourceQueryRequestMessage containerResourceQueryRequest =
            containerResourceQueryRequestDeserializer.deserialize(request.getPayload());
        handleContainerResourceQueryRequest(containerResourceQueryRequest);

        break;

      case ControllerRequest.OPERATION_CONTROLLER_RESOURCE_COMMIT:
        ContainerResourceCommitRequestMessage containerResourceCommitRequest =
            containerResourceCommitRequestDeserializer.deserialize(request.getPayload());
        handleContainerResourceCommitRequest(containerResourceCommitRequest);

        break;

      default:
        spaceEnvironment.getLog().error(String.format("Unknown ROS controller request %d", request.getOperation()));
    }
  }

  /**
   * Handle a container resource deployment query request.
   *
   * @param rosRequest
   *          the ROS request
   */
  private void handleContainerResourceQueryRequest(ContainerResourceQueryRequestMessage rosRequest) {
    switch (rosRequest.getType()) {
      case ContainerResourceQueryRequestMessage.TYPE_SPECIFIC_QUERY:
        spaceEnvironment.getLog().info(
            String.format("Got resource deployment query with transaction ID %s", rosRequest.getTransactionId()));
        ContainerResourceDeploymentQueryRequest query =
            RosDeploymentMessageTranslator.deserializeContainerResourceDeploymentQuery(rosRequest);

        ContainerResourceDeploymentQueryResponse queryResponse =
            containerResourceDeploymentManager.queryResources(query);
        spaceEnvironment.getLog().info(
            String.format("Resource deployment query with transaction ID %s has status %s",
                rosRequest.getTransactionId(), queryResponse.getStatus()));

        ContainerResourceQueryResponseMessage response =
            rosMessageFactory.newFromType(ContainerResourceQueryResponseMessage._TYPE);
        RosDeploymentMessageTranslator.serializeResourceDeploymentQueryResponse(queryResponse, response);

        publishControllerStatus(ControllerStatus.STATUS_CONTROLLER_CONTAINER_RESOURCE_QUERY,
            containerResourceQueryResponseSerializer.serialize(response));
        break;

      default:
        spaceEnvironment.getLog().error(
            String.format("Unknown ContainerResourceDeploymentRequest %d", rosRequest.getType()));
    }
  }

  /**
   * Handle a container resource deployment query request.
   *
   * @param rosRequest
   *          the ROS request
   */
  private void handleContainerResourceCommitRequest(ContainerResourceCommitRequestMessage rosRequest) {
    spaceEnvironment.getLog().info(
        String.format("Got resource deployment commit with transaction ID %s", rosRequest.getTransactionId()));
    ContainerResourceDeploymentCommitRequest request =
        RosDeploymentMessageTranslator.deserializeResourceDeploymentCommit(rosRequest);
    ContainerResourceDeploymentCommitResponse commitResponse =
        containerResourceDeploymentManager.commitResources(request);
    ContainerResourceCommitResponseMessage rosCommitResponse =
        rosMessageFactory.newFromType(ContainerResourceCommitResponseMessage._TYPE);
    RosDeploymentMessageTranslator.serializeResourceDeploymentCommitResponse(commitResponse, rosCommitResponse);

    publishControllerStatus(ControllerStatus.STATUS_CONTROLLER_CONTAINER_RESOURCE_COMMIT,
        containerResourceCommitResponseSerializer.serialize(rosCommitResponse));
  }

  /**
   * Create and publish controller full status.
   */
  private void publishControllerFullStatus() {
    spaceEnvironment.getLog().info("Getting full controller status");

    SimpleSpaceController controllerInfo = controllerControl.getControllerInfo();

    ControllerFullStatus fullStatus = rosMessageFactory.newFromType(ControllerFullStatus._TYPE);
    fullStatus.setName(controllerInfo.getName());
    fullStatus.setDescription(controllerInfo.getDescription());
    fullStatus.setHostId(controllerInfo.getHostId());

    for (InstalledLiveActivity activity : controllerControl.getAllInstalledLiveActivities()) {
      LiveActivityRuntimeStatus cas = rosMessageFactory.newFromType(LiveActivityRuntimeStatus._TYPE);
      cas.setUuid(activity.getUuid());

      LiveActivityRunner activeActivity = controllerControl.getLiveActivityRunnerByUuid(cas.getUuid());
      if (activeActivity != null) {
        Activity instance = activeActivity.getInstance();
        ActivityState state = null;
        if (instance != null) {
          ActivityStatus activityStatus = instance.getActivityStatus();
          state = activityStatus.getState();
          if (activityStatus.getException() != null) {
            cas.setStatusDetail(InteractiveSpacesException.getStackTrace(activityStatus.getException()));
          }
        } else {
          state = ActivityState.READY;
        }
        cas.setStatus(translateActivityState(state));

        if (spaceEnvironment.getLog().isInfoEnabled()) {
          spaceEnvironment.getLog().info(
              String.format("Full status live activity %s status %s, returning %d", cas.getUuid(), state,
                  cas.getStatus()));
        }
      } else {
        cas.setStatus(LiveActivityRuntimeStatus.STATUS_LIVE_ACTIVITY_READY);
        spaceEnvironment.getLog().warn(
            String.format("Full status live activity %s not found, returning READY", cas.getUuid()));
      }

      fullStatus.getLiveActivityStatuses().add(cas);
    }

    publishControllerStatus(ControllerStatus.STATUS_CONTROLLER_FULL_STATUS,
        controllerFullStatusMessageSerializer.serialize(fullStatus));
  }

  @Override
  public void publishControllerDataStatus(SpaceControllerDataOperation type, SpaceControllerStatus statusCode,
      Exception e) {

    int status =
        SpaceControllerDataOperation.DATA_CAPTURE.equals(type) ? ControllerStatus.STATUS_CONTROLLER_DATA_CAPTURE
            : ControllerStatus.STATUS_CONTROLLER_DATA_RESTORE;

    ControllerStatus statusMsg = newControllerStatus(status);
    statusMsg.setStatusCode(statusCode.getDescription());

    if (e != null) {
      statusMsg.setStatusDetail(InteractiveSpacesException.getStackTrace(e));
    }

    controllerStatusPublisher.publish(statusMsg);
  }

  /**
   * Handle a live activity deployment request.
   *
   * @param deployRequest
   *          the deployment request
   */
  private void handleLiveActivityDeployment(LiveActivityDeployRequest deployRequest) {
    ActivityDeploymentRequest activityDeployRequest = getControllerDeploymentRequest(deployRequest);
    LiveActivityDeploymentResponse activityDeployStatus =
        spaceControllerActivityInstallManager.handleDeploymentRequest(activityDeployRequest);
    LiveActivityDeployStatus deployStatus = rosMessageFactory.newFromType(LiveActivityDeployStatus._TYPE);

    RosDeploymentMessageTranslator.serializeDeploymentStatus(activityDeployStatus, deployStatus);

    publishControllerStatus(ControllerStatus.STATUS_CONTROLLER_ACTIVITY_INSTALL,
        liveActivityDeployStatusSerializer.serialize(deployStatus));
  }

  /**
   * Get the controller deployment request for a communicator deployment
   * request.
   *
   * @param deployRequest
   *          the communicator deployment request
   *
   * @return the controller deployment request
   */
  private ActivityDeploymentRequest getControllerDeploymentRequest(LiveActivityDeployRequest deployRequest) {
    ActivityDeploymentRequest controllerDeployRequest =
        RosDeploymentMessageTranslator.deserializeActivityDeploymentRequest(deployRequest);

    return controllerDeployRequest;
  }

  /**
   * Handle a live activity deletion request.
   *
   * @param deleteRequest
   *          the deletion request
   */
  private void handleLiveActivityDeletion(LiveActivityDeleteRequest deleteRequest) {
    SpaceControllerLiveActivityDeleteRequest controllerDeleteRequest =
        getSpaceControllerLiveActivityDeleteRequest(deleteRequest);
    SpaceControllerLiveActivityDeleteStatus controllerDeleteStatus =
        spaceControllerActivityInstallManager.handleDeleteRequest(controllerDeleteRequest);
    LiveActivityDeleteStatus deleteStatus = getLiveActivityDeleteStatus(controllerDeleteStatus);

    ChannelBuffer payload = liveActivityDeleteStatusSerializer.serialize(deleteStatus);

    publishControllerStatus(ControllerStatus.STATUS_CONTROLLER_ACTIVITY_DELETE, payload);
  }

  /**
   * Get the controller live activity delete request from the communicator
   * delete request.
   *
   * @param deleteRequest
   *          the communicator delete request
   *
   * @return the controller delete request
   */
  private SpaceControllerLiveActivityDeleteRequest getSpaceControllerLiveActivityDeleteRequest(
      LiveActivityDeleteRequest deleteRequest) {
    SpaceControllerLiveActivityDeleteRequest controllerDeleteRequest =
        new SpaceControllerLiveActivityDeleteRequest(deleteRequest.getUuid(), deleteRequest.getIdentifyingName(),
            deleteRequest.getVersion());
    return controllerDeleteRequest;
  }

  /**
   * Get the communicator live activity delete status from the controller delete
   * status.
   *
   * @param controllerDeleteStatus
   *          the controller delete status
   *
   * @return the communicator live activity delete status
   */
  private LiveActivityDeleteStatus getLiveActivityDeleteStatus(
      SpaceControllerLiveActivityDeleteStatus controllerDeleteStatus) {
    LiveActivityDeleteStatus deleteStatus = rosMessageFactory.newFromType(LiveActivityDeleteStatus._TYPE);
    deleteStatus.setUuid(controllerDeleteStatus.getUuid());
    deleteStatus.setTimeDeleted(controllerDeleteStatus.getTimeDeleted());

    switch (controllerDeleteStatus.getStatus()) {
      case SpaceControllerLiveActivityDeleteStatus.STATUS_SUCCESS:
        deleteStatus.setStatus(LiveActivityDeleteStatus.STATUS_SUCCESS);
        break;

      case SpaceControllerLiveActivityDeleteStatus.STATUS_FAILURE:
        deleteStatus.setStatus(LiveActivityDeleteStatus.STATUS_FAILURE);
        break;

      default:
        spaceEnvironment.getLog().warn(
            String.format("Unknown space controller activity delete status %d", controllerDeleteStatus.getStatus()));
    }
    return deleteStatus;
  }

  /**
   * Handle a ROS activity control request coming in.
   *
   * @param request
   *          The ROS request.
   */
  @VisibleForTesting
  void handleLiveActivityRuntimeRequest(final LiveActivityRuntimeRequest request) {
    spaceEnvironment.getExecutorService().submit(new Runnable() {
      @Override
      public void run() {
        String uuid = request.getLiveActivityUuid();
        switch (request.getOperation()) {
          case LiveActivityRuntimeRequest.OPERATION_LIVE_ACTIVITY_STARTUP:
            controllerControl.startupActivity(uuid);

            break;

          case LiveActivityRuntimeRequest.OPERATION_LIVE_ACTIVITY_ACTIVATE:
            controllerControl.activateActivity(uuid);

            break;

          case LiveActivityRuntimeRequest.OPERATION_LIVE_ACTIVITY_DEACTIVATE:
            controllerControl.deactivateActivity(uuid);

            break;

          case LiveActivityRuntimeRequest.OPERATION_LIVE_ACTIVITY_SHUTDOWN:
            controllerControl.shutdownActivity(uuid);

            break;

          case LiveActivityRuntimeRequest.OPERATION_LIVE_ACTIVITY_STATUS:
            controllerControl.statusLiveActivity(uuid);

            break;

          case LiveActivityRuntimeRequest.OPERATION_LIVE_ACTIVITY_CONFIGURE:
            handleLiveActivityConfigurationRequest(uuid,
                liveActivityConfigurationRequestDeserializer.deserialize(request.getPayload()));

            break;

          case LiveActivityRuntimeRequest.OPERATION_LIVE_ACTIVITY_CLEAN_DATA_PERMANENT:
            controllerControl.cleanLiveActivityPermanentData(uuid);

            break;

          case LiveActivityRuntimeRequest.OPERATION_LIVE_ACTIVITY_CLEAN_DATA_TMP:
            controllerControl.cleanLiveActivityTmpData(uuid);

            break;

          default:
            spaceEnvironment.getLog().error(
                String.format("Unknown ROS activity runtime request %d", request.getOperation()));
        }
      }
    });
  }

  /**
   * Handle a configuration request.
   *
   * @param uuid
   *          uuid of the activity
   * @param configurationRequest
   *          the configuration request
   */
  private void handleLiveActivityConfigurationRequest(String uuid, ActivityConfigurationRequest configurationRequest) {
    Map<String, Object> values = Maps.newHashMap();

    for (ActivityConfigurationParameterRequest parameterRequest : configurationRequest.getParameters()) {
      if (parameterRequest.getOperation() == ActivityConfigurationParameterRequest.OPERATION_ADD) {
        values.put(parameterRequest.getName(), parameterRequest.getValue());
      }
    }

    controllerControl.configureActivity(uuid, values);
  }

  @Override
  public void publishActivityStatus(String uuid, ActivityStatus astatus) {
    try {
      LiveActivityRuntimeStatus status = rosMessageFactory.newFromType(LiveActivityRuntimeStatus._TYPE);
      status.setUuid(uuid);
      status.setStatus(translateActivityState(astatus.getState()));
      if (astatus.getException() != null) {
        status.setStatusDetail(InteractiveSpacesException.getStackTrace(astatus.getException()));
      }

      String description = astatus.getDescription();
      if (description != null) {
        status.setStatusDetail(astatus.getDescription());
      }

      publishControllerStatus(ControllerStatus.STATUS_CONTROLLER_LIVE_ACTIVITY_RUNTIME_STATUS,
          liveActivityRuntimeStatusSerializer.serialize(status));
    } catch (Exception e) {
      spaceEnvironment.getLog().error(
          String.format("Could not publish Status change %s for Live Activity %s\n", uuid, astatus), e);
    }
  }

  /**
   * Publish a controller status update with a payload.
   *
   * @param statusCode
   *          the status code
   * @param payload
   *          the payload, can be {@code null}
   */
  private void publishControllerStatus(int statusCode, ChannelBuffer payload) {
    ControllerStatus status = newControllerStatus(statusCode);

    if (payload != null) {
      status.setPayload(payload);
    }

    controllerStatusPublisher.publish(status);
  }

  /**
   * Create a new {@link ControllerStatus} object with most fields filled in.
   *
   * @param statusCode
   *          the status code
   *
   * @return a newly created status message
   */
  private ControllerStatus newControllerStatus(int statusCode) {
    ControllerStatus status = rosMessageFactory.newFromType(ControllerStatus._TYPE);
    status.setControllerUuid(controllerControl.getControllerInfo().getUuid());
    status.setStatus(statusCode);
    return status;
  }

  /**
   * Translate an Interactive Spaces activity status to its ROS message
   * equivalent.
   *
   * @param state
   *          object
   * @return status code
   */
  private int translateActivityState(ActivityState state) {
    switch (state) {
      case UNKNOWN:
        return LiveActivityRuntimeStatus.STATUS_LIVE_ACTIVITY_UNKNOWN;

      case READY:
        return LiveActivityRuntimeStatus.STATUS_LIVE_ACTIVITY_READY;

      case RUNNING:
        return LiveActivityRuntimeStatus.STATUS_LIVE_ACTIVITY_RUNNING;

      case ACTIVE:
        return LiveActivityRuntimeStatus.STATUS_LIVE_ACTIVITY_ACTIVE;

      case CRASHED:
        return LiveActivityRuntimeStatus.STATUS_LIVE_ACTIVITY_CRASH;

      case STARTUP_ATTEMPT:
        return LiveActivityRuntimeStatus.STATUS_LIVE_ACTIVITY_STARTUP_ATTEMPT;

      case STARTUP_FAILURE:
        return LiveActivityRuntimeStatus.STATUS_LIVE_ACTIVITY_STARTUP_FAILURE;

      case ACTIVATE_ATTEMPT:
        return LiveActivityRuntimeStatus.STATUS_LIVE_ACTIVITY_ACTIVATE_ATTEMPT;

      case ACTIVATE_FAILURE:
        return LiveActivityRuntimeStatus.STATUS_LIVE_ACTIVITY_ACTIVATE_FAILURE;

      case DEACTIVATE_ATTEMPT:
        return LiveActivityRuntimeStatus.STATUS_LIVE_ACTIVITY_DEACTIVATE_ATTEMPT;

      case DEACTIVATE_FAILURE:
        return LiveActivityRuntimeStatus.STATUS_LIVE_ACTIVITY_DEACTIVATE_FAILURE;

      case SHUTDOWN_ATTEMPT:
        return LiveActivityRuntimeStatus.STATUS_LIVE_ACTIVITY_SHUTDOWN_ATTEMPT;

      case SHUTDOWN_FAILURE:
        return LiveActivityRuntimeStatus.STATUS_LIVE_ACTIVITY_SHUTDOWN_FAILURE;

      case DEPLOY_ATTEMPT:
        return LiveActivityRuntimeStatus.STATUS_LIVE_ACTIVITY_DEPLOY_ATTEMPT;

      case DEPLOY_FAILURE:
        return LiveActivityRuntimeStatus.STATUS_LIVE_ACTIVITY_DEPLOY_FAILURE;

      case DOESNT_EXIST:
        return LiveActivityRuntimeStatus.STATUS_LIVE_ACTIVITY_DOESNT_EXIST;

      default:
        return LiveActivityRuntimeStatus.STATUS_LIVE_ACTIVITY_UNKNOWN;
    }
  }

  /**
   * Set the Ros Environment the controller should run in.
   *
   * @param rosEnvironment
   *          the ros environment for this communicator
   */
  public void setRosEnvironment(RosEnvironment rosEnvironment) {
    this.rosEnvironment = rosEnvironment;
  }

  /**
   * @param spaceControllerActivityInstaller
   *          the spaceControllerActivityInstallManager to set
   */
  public void setSpaceControllerActivityInstallManager(
      SpaceControllerActivityInstallationManager spaceControllerActivityInstaller) {
    this.spaceControllerActivityInstallManager = spaceControllerActivityInstaller;
  }

  /**
   * @param controllerControl
   *          the controllerControl to set
   */
  @Override
  public void setControllerControl(SpaceControllerControl controllerControl) {
    this.controllerControl = controllerControl;
  }

  /**
   * @param spaceEnvironment
   *          the spaceEnvironment to set
   */
  public void setSpaceEnvironment(InteractiveSpacesEnvironment spaceEnvironment) {
    this.spaceEnvironment = spaceEnvironment;
  }

  /**
   * Give heartbeats from controller using ROS.
   *
   * @author Keith M. Hughes
   */
  private class RosControllerHeartbeat implements SpaceControllerHeartbeat {

    /**
     * heartbeatLoop status is always the same, so create once.
     */
    private final ControllerStatus status;

    /**
     * Construct a heartbeat object.
     */
    public RosControllerHeartbeat() {
      status = rosMessageFactory.newFromType(ControllerStatus._TYPE);
      status.setStatus(ControllerStatus.STATUS_CONTROLLER_HEARTBEAT);
    }

    @Override
    public void sendHeartbeat() {
      // In case the UUID changed.
      status.setControllerUuid(controllerControl.getControllerInfo().getUuid());
      controllerStatusPublisher.publish(status);
    }
  }
}
