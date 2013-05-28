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

import interactivespaces.activity.Activity;
import interactivespaces.activity.ActivityState;
import interactivespaces.activity.ActivityStatus;
import interactivespaces.controller.client.node.ActiveControllerActivity;
import interactivespaces.controller.client.node.SpaceControllerActivityInstallationManager;
import interactivespaces.controller.client.node.SpaceControllerCommunicator;
import interactivespaces.controller.client.node.SpaceControllerControl;
import interactivespaces.controller.client.node.SpaceControllerHeartbeat;
import interactivespaces.controller.client.node.SpaceControllerLiveActivityDeleteRequest;
import interactivespaces.controller.client.node.SpaceControllerLiveActivityDeleteStatus;
import interactivespaces.controller.client.node.SpaceControllerLiveActivityDeployRequest;
import interactivespaces.controller.client.node.SpaceControllerLiveActivityDeployStatus;
import interactivespaces.controller.common.ros.RosSpaceControllerConstants;
import interactivespaces.controller.domain.InstalledLiveActivity;
import interactivespaces.domain.basic.pojo.SimpleSpaceController;
import interactivespaces.master.server.remote.client.RemoteMasterServerClient;
import interactivespaces.master.server.remote.client.ros.RosRemoteMasterServerClient;
import interactivespaces.system.InteractiveSpacesEnvironment;
import interactivespaces.util.InteractiveSpacesUtilities;
import interactivespaces_msgs.ActivityConfigurationParameterRequest;
import interactivespaces_msgs.ActivityConfigurationRequest;
import interactivespaces_msgs.ControllerActivityRuntimeRequest;
import interactivespaces_msgs.ControllerActivityStatus;
import interactivespaces_msgs.ControllerFullStatus;
import interactivespaces_msgs.ControllerRequest;
import interactivespaces_msgs.ControllerStatus;
import interactivespaces_msgs.LiveActivityDeleteRequest;
import interactivespaces_msgs.LiveActivityDeleteStatus;
import interactivespaces_msgs.LiveActivityDeployRequest;
import interactivespaces_msgs.LiveActivityDeployStatus;

import java.nio.ByteOrder;
import java.util.Map;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.ros.internal.node.topic.SubscriberIdentifier;
import org.ros.message.MessageDeserializer;
import org.ros.message.MessageFactory;
import org.ros.message.MessageListener;
import org.ros.message.MessageSerializer;
import org.ros.node.ConnectedNode;
import org.ros.node.NodeConfiguration;
import org.ros.node.topic.DefaultPublisherListener;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.PublisherListener;
import org.ros.node.topic.Subscriber;
import org.ros.osgi.common.RosEnvironment;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;

/**
 * An {@link SpaceControllerCommunicator} using ROS for communication.
 * 
 * @author Keith M. Hughes
 */
public class RosSpaceControllerCommunicator implements
		SpaceControllerCommunicator {

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
	 * Publisher for activity status.
	 */
	private Publisher<ControllerActivityStatus> activityStatusPublisher;

	/**
	 * Subscriber for activity runtime requests.
	 */
	private Subscriber<ControllerActivityRuntimeRequest> activityRuntimeRequest;

	/**
	 * A ROS message serialize for controller full status messages.
	 */
	private MessageSerializer<ControllerFullStatus> controllerFullStatusMessageSerializer;

	/**
	 * ROS message deserializer for live activity deployment requests
	 */
	private MessageDeserializer<LiveActivityDeployRequest> liveActivityDeployRequestDeserializer;

	/**
	 * ROS message serializer for live activity deployment statuses
	 */
	private MessageSerializer<LiveActivityDeployStatus> liveActivityDeployStatusSerializer;

	/**
	 * ROS message deserializer for live activity deletetion requests
	 */
	private MessageDeserializer<LiveActivityDeleteRequest> liveActivityDeleteRequestDeserializer;

	/**
	 * ROS message serializer for live activity deletion statuses
	 */
	private MessageSerializer<LiveActivityDeleteStatus> liveActivityDeleteStatusSerializer;

	/**
	 * ROS message deserializer for the activity configuration requests
	 */
	private MessageDeserializer<ActivityConfigurationRequest> activityConfigurationRequestDeserializer;

	/**
	 * Activity installer for the controller
	 */
	private SpaceControllerActivityInstallationManager spaceControllerActivityInstallManager;

	/**
	 * The space environment for this communicator.
	 */
	private InteractiveSpacesEnvironment spaceEnvironment;

	public RosSpaceControllerCommunicator(
			SpaceControllerActivityInstallationManager spaceControllerActivityInstaller,
			RosEnvironment rosEnvironment,
			InteractiveSpacesEnvironment spaceEnvironment) {
		this.spaceControllerActivityInstallManager = spaceControllerActivityInstaller;
		this.rosEnvironment = rosEnvironment;
		this.spaceEnvironment = spaceEnvironment;
	}

	@Override
	public void onStartup() {
		final NodeConfiguration nodeConfiguration = rosEnvironment
				.getPublicNodeConfigurationWithNodeName();
		nodeConfiguration.setNodeName("interactivespaces/controller");

		node = rosEnvironment.newNode(nodeConfiguration);
		rosMessageFactory = node.getTopicMessageFactory();

		PublisherListener<ControllerStatus> controllerStatusPublisherListener = new DefaultPublisherListener<ControllerStatus>() {
			@Override
			public void onNewSubscriber(Publisher<ControllerStatus> publisher,
					SubscriberIdentifier subscriber) {
				if (spaceEnvironment.getLog().isInfoEnabled()) {
					spaceEnvironment.getLog().info(
							String.format(
									"New subscriber for controller status %s",
									subscriber.getNodeIdentifier()));
				}
			}
		};

		controllerStatusPublisher = node.newPublisher(
				RosSpaceControllerConstants.CONTROLLER_STATUS_TOPIC_NAME,
				RosSpaceControllerConstants.CONTROLLER_STATUS_MESSAGE_TYPE);
		controllerStatusPublisher
				.addListener(controllerStatusPublisherListener);

		controllerRequestSubscriber = node.newSubscriber(
				RosSpaceControllerConstants.CONTROLLER_REQUEST_TOPIC_NAME,
				RosSpaceControllerConstants.CONTROLLER_REQUEST_MESSAGE_TYPE);
		controllerRequestSubscriber
				.addMessageListener(new MessageListener<ControllerRequest>() {
					@Override
					public void onNewMessage(ControllerRequest request) {
						handleControllerRequest(request);
					}
				});

		activityStatusPublisher = node
				.newPublisher(
						RosSpaceControllerConstants.ACTIVITY_RUNTIME_STATUS_TOPIC_NAME,
						RosSpaceControllerConstants.ACTIVITY_RUNTIME_STATUS_MESSAGE_TYPE);

		activityRuntimeRequest = node
				.newSubscriber(
						RosSpaceControllerConstants.ACTIVITY_RUNTIME_REQUEST_TOPIC_NAME,
						RosSpaceControllerConstants.ACTIVITY_RUNTIME_REQUEST_MESSAGE_TYPE);
		activityRuntimeRequest
				.addMessageListener(new MessageListener<ControllerActivityRuntimeRequest>() {
					@Override
					public void onNewMessage(
							ControllerActivityRuntimeRequest request) {
						handleActivityRuntimeRequest(request);
					}
				});

		controllerFullStatusMessageSerializer = node
				.getMessageSerializationFactory().newMessageSerializer(
						"interactivespaces_msgs/ControllerFullStatus");

		activityConfigurationRequestDeserializer = node
				.getMessageSerializationFactory().newMessageDeserializer(
						"interactivespaces_msgs/ActivityConfigurationRequest");

		liveActivityDeployStatusSerializer = node
				.getMessageSerializationFactory().newMessageSerializer(
						"interactivespaces_msgs/LiveActivityDeployStatus");

		liveActivityDeployRequestDeserializer = node
				.getMessageSerializationFactory().newMessageDeserializer(
						"interactivespaces_msgs/LiveActivityDeployRequest");

		liveActivityDeleteStatusSerializer = node
				.getMessageSerializationFactory().newMessageSerializer(
						"interactivespaces_msgs/LiveActivityDeleteStatus");

		liveActivityDeleteRequestDeserializer = node
				.getMessageSerializationFactory().newMessageDeserializer(
						"interactivespaces_msgs/LiveActivityDeleteRequest");
	}

	@Override
	public void notifyRemoteMasterServerAboutStartup(
			SimpleSpaceController controllerInfo) {
		RemoteMasterServerClient masterServerClient = new RosRemoteMasterServerClient();
		masterServerClient.startup(rosEnvironment);
		InteractiveSpacesUtilities.delay(1000);

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
	 *            The ROS request.
	 */
	@VisibleForTesting
	void handleControllerRequest(
			interactivespaces_msgs.ControllerRequest request) {
		switch (request.getOperation()) {
		case ControllerRequest.OPERATION_STATUS:
			publishControllerFullStatus();

			break;

		case ControllerRequest.OPERATION_SHUTDOWN_ACTIVITIES:
			controllerControl.shutdownAllActivities();

			break;

		case ControllerRequest.OPERATION_SHUTDOWN_CONTROLLER:
			controllerControl.shutdownControllerContainer();

			break;

		case ControllerRequest.OPERATION_DEPLOY_LIVE_ACTIVITY:
			handleLiveActivityDeployment(liveActivityDeployRequestDeserializer
					.deserialize(request.getPayload()));

			break;

		case ControllerRequest.OPERATION_DELETE_LIVE_ACTIVITY:
			handleLiveActivityDeletion(liveActivityDeleteRequestDeserializer
					.deserialize(request.getPayload()));

			break;

		case ControllerRequest.OPERATION_CLEAN_DATA_TMP:
			controllerControl.cleanControllerTempData();

			break;

		case ControllerRequest.OPERATION_CLEAN_DATA_PERMANENT:
			controllerControl.cleanControllerPermanentData();

			break;

		default:
			spaceEnvironment.getLog().error(
					String.format("Unknown ROS controller request %d",
							request.getOperation()));
		}
	}

	/**
	 * Create and publish controller full status.
	 */
	private void publishControllerFullStatus() {
		spaceEnvironment.getLog().info("Getting full controller status");

		SimpleSpaceController controllerInfo = controllerControl
				.getControllerInfo();

		ControllerStatus status = rosMessageFactory
				.newFromType(ControllerStatus._TYPE);
		status.setUuid(controllerInfo.getUuid());
		status.setStatus(ControllerStatus.STATUS_FULL);

		ControllerFullStatus fullStatus = rosMessageFactory
				.newFromType(ControllerFullStatus._TYPE);
		fullStatus.setName(controllerInfo.getName());
		fullStatus.setDescription(controllerInfo.getDescription());
		fullStatus.setHostId(controllerInfo.getHostId());

		for (InstalledLiveActivity activity : controllerControl
				.getAllInstalledLiveActivities()) {
			System.out.println("activity in statusall is "
					+ activity.getIdentifyingName());
			ControllerActivityStatus cas = rosMessageFactory
					.newFromType(ControllerActivityStatus._TYPE);
			cas.setUuid(activity.getUuid());

			ActiveControllerActivity activeActivity = controllerControl
					.getActiveActivityByUuid(cas.getUuid());
			if (activeActivity != null) {
				System.out.println("Active activity in statusall is "
						+ activity.getIdentifyingName());
				Activity instance = activeActivity.getInstance();
				ActivityState state = null;
				if (instance != null) {
					state = instance.getActivityStatus().getState();
				} else {
					state = ActivityState.READY;
				}
				cas.setStatus(translateActivityState(state));

				if (spaceEnvironment.getLog().isInfoEnabled()) {
					spaceEnvironment
							.getLog()
							.info(String
									.format("Full status live activity %s status %s, returning %d",
											cas.getUuid(), state,
											cas.getStatus()));
				}
			} else {
				cas.setStatus(ControllerActivityStatus.STATUS_READY);
				spaceEnvironment
						.getLog()
						.warn(String
								.format("Full status live activity %s not found, returning READY",
										cas.getUuid()));
			}

			fullStatus.getActivities().add(cas);
		}

		ChannelBuffer serialize = ChannelBuffers.dynamicBuffer(
				ByteOrder.LITTLE_ENDIAN, 256);
		controllerFullStatusMessageSerializer.serialize(fullStatus, serialize);
		status.setData(serialize);

		controllerStatusPublisher.publish(status);
	}

	/**
	 * Handle a live activity deployment request.
	 * 
	 * @param deployRequest
	 *            the deployment request
	 */
	private void handleLiveActivityDeployment(
			LiveActivityDeployRequest deployRequest) {
		SpaceControllerLiveActivityDeployRequest controllerDeployRequest = getControllerDeploymentRequest(deployRequest);
		SpaceControllerLiveActivityDeployStatus controllerDeployStatus = spaceControllerActivityInstallManager
				.handleDeploymentRequest(controllerDeployRequest);
		LiveActivityDeployStatus deployStatus = getDeployStatus(controllerDeployStatus);

		ControllerStatus status = rosMessageFactory
				.newFromType(ControllerStatus._TYPE);
		status.setUuid(controllerControl.getControllerInfo().getUuid());
		status.setStatus(ControllerStatus.STATUS_ACTIVITY_INSTALL);

		ChannelBuffer serialize = ChannelBuffers.dynamicBuffer(
				ByteOrder.LITTLE_ENDIAN, 256);
		liveActivityDeployStatusSerializer.serialize(deployStatus, serialize);
		status.setData(serialize);

		controllerStatusPublisher.publish(status);
	}

	/**
	 * Get the controller deployment request for a communicator deployment
	 * request.
	 * 
	 * @param deployRequest
	 *            the communicator deployment request
	 * 
	 * @return the controller deployment request
	 */
	public SpaceControllerLiveActivityDeployRequest getControllerDeploymentRequest(
			LiveActivityDeployRequest deployRequest) {
		SpaceControllerLiveActivityDeployRequest controllerDeployRequest = new SpaceControllerLiveActivityDeployRequest(
				deployRequest.getUuid(), deployRequest.getActivitySourceUri(),
				deployRequest.getIdentifyingName(), deployRequest.getVersion());
		return controllerDeployRequest;
	}

	/**
	 * Get the communicator deployment status.
	 * 
	 * @param controllerDeployStatus
	 *            the controller deploy status
	 * 
	 * @return the communicator deployment status
	 */
	public LiveActivityDeployStatus getDeployStatus(
			SpaceControllerLiveActivityDeployStatus controllerDeployStatus) {
		LiveActivityDeployStatus deployStatus = rosMessageFactory
				.newFromType(LiveActivityDeployStatus._TYPE);

		deployStatus.setUuid(controllerDeployStatus.getUuid());
		deployStatus.setTimeDeployed(controllerDeployStatus.getTimeDeployed());

		switch (controllerDeployStatus.getStatus()) {
		case SpaceControllerLiveActivityDeployStatus.STATUS_SUCCESS:
			deployStatus.setStatus(LiveActivityDeployStatus.STATUS_SUCCESS);
			break;

		case SpaceControllerLiveActivityDeployStatus.STATUS_FAILURE_COPY:
			deployStatus
					.setStatus(LiveActivityDeployStatus.STATUS_FAILURE_COPY);
			break;

		case SpaceControllerLiveActivityDeployStatus.STATUS_FAILURE_UNPACK:
			deployStatus
					.setStatus(LiveActivityDeployStatus.STATUS_FAILURE_UNPACK);
			break;

		default:
			spaceEnvironment
					.getLog()
					.warn(String
							.format("Unknown space controller activity deploy status %d",
									controllerDeployStatus.getStatus()));

		}
		return deployStatus;
	}

	/**
	 * Handle a live activity deletion request.
	 * 
	 * @param deleteRequest
	 *            the deletion request
	 */
	private void handleLiveActivityDeletion(
			LiveActivityDeleteRequest deleteRequest) {
		SpaceControllerLiveActivityDeleteRequest controllerDeleteRequest = getSpaceControllerLiveActivityDeleteRequest(deleteRequest);
		SpaceControllerLiveActivityDeleteStatus controllerDeleteStatus = spaceControllerActivityInstallManager
				.handleDeleteRequest(controllerDeleteRequest);
		LiveActivityDeleteStatus deleteStatus = getLiveActivityDeleteStatus(controllerDeleteStatus);

		ControllerStatus status = rosMessageFactory
				.newFromType(ControllerStatus._TYPE);
		status.setUuid(controllerControl.getControllerInfo().getUuid());
		status.setStatus(ControllerStatus.STATUS_ACTIVITY_DELETE);

		ChannelBuffer serialize = ChannelBuffers.dynamicBuffer(
				ByteOrder.LITTLE_ENDIAN, 256);
		liveActivityDeleteStatusSerializer.serialize(deleteStatus, serialize);
		status.setData(serialize);

		controllerStatusPublisher.publish(status);
	}

	/**
	 * Get the controller live activity delete request from the communicator
	 * delete request.
	 * 
	 * @param deleteRequest
	 *            the communicator delete request
	 * 
	 * @return the controller delete request
	 */
	public SpaceControllerLiveActivityDeleteRequest getSpaceControllerLiveActivityDeleteRequest(
			LiveActivityDeleteRequest deleteRequest) {
		SpaceControllerLiveActivityDeleteRequest controllerDeleteRequest = new SpaceControllerLiveActivityDeleteRequest(
				deleteRequest.getUuid(), deleteRequest.getIdentifyingName(),
				deleteRequest.getVersion());
		return controllerDeleteRequest;
	}

	/**
	 * Get the communicator live activity delete status from the controller
	 * delete status.
	 * 
	 * @param controllerDeleteStatus
	 *            the controller delete status
	 * 
	 * @return the communicator live activity delete status
	 */
	public LiveActivityDeleteStatus getLiveActivityDeleteStatus(
			SpaceControllerLiveActivityDeleteStatus controllerDeleteStatus) {
		LiveActivityDeleteStatus deleteStatus = rosMessageFactory
				.newFromType(LiveActivityDeleteStatus._TYPE);
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
			spaceEnvironment
					.getLog()
					.warn(String
							.format("Unknown space controller activity delete status %d",
									controllerDeleteStatus.getStatus()));
		}
		return deleteStatus;
	}

	/**
	 * Handle a ROS activity control request coming in.
	 * 
	 * @param request
	 *            The ROS request.
	 */
	@VisibleForTesting
	void handleActivityRuntimeRequest(
			final ControllerActivityRuntimeRequest request) {
		spaceEnvironment.getExecutorService().submit(new Runnable() {
			public void run() {
				String uuid = request.getUuid();
				switch (request.getOperation()) {
				case ControllerActivityRuntimeRequest.OPERATION_STARTUP:
					controllerControl.startupActivity(uuid);

					break;

				case ControllerActivityRuntimeRequest.OPERATION_ACTIVATE:
					controllerControl.activateActivity(uuid);

					break;

				case ControllerActivityRuntimeRequest.OPERATION_DEACTIVATE:
					controllerControl.deactivateActivity(uuid);

					break;

				case ControllerActivityRuntimeRequest.OPERATION_SHUTDOWN:
					controllerControl.shutdownActivity(uuid);

					break;

				case ControllerActivityRuntimeRequest.OPERATION_STATUS:
					controllerControl.statusActivity(uuid);

					break;

				case ControllerActivityRuntimeRequest.OPERATION_CONFIGURE:
					ActivityConfigurationRequest configurationRequest = activityConfigurationRequestDeserializer
							.deserialize(request.getData());

					handleActivityConfigurationRequest(uuid,
							configurationRequest);

					break;

				case ControllerActivityRuntimeRequest.OPERATION_CLEAN_DATA_PERMANENT:
					controllerControl.cleanActivityPermanentData(uuid);

					break;

				case ControllerActivityRuntimeRequest.OPERATION_CLEAN_DATA_TMP:
					controllerControl.cleanActivityTmpData(uuid);

					break;

				default:
					spaceEnvironment.getLog().error(
							String.format(
									"Unknown ROS activity runtime request %d",
									request.getOperation()));
				}
			}
		});
	}

	/**
	 * Handle a configuration request.
	 * 
	 * @param uuid
	 *            uuid of the activity
	 * @param configurationRequest
	 *            the configuration request
	 */
	private void handleActivityConfigurationRequest(String uuid,
			ActivityConfigurationRequest configurationRequest) {
		Map<String, Object> values = Maps.newHashMap();

		for (ActivityConfigurationParameterRequest parameterRequest : configurationRequest
				.getParameters()) {
			if (parameterRequest.getOperation() == ActivityConfigurationParameterRequest.OPERATION_ADD) {
				values.put(parameterRequest.getName(),
						parameterRequest.getValue());
			}
		}

		controllerControl.configureActivity(uuid, values);
	}

	@Override
	public void publishActivityStatus(String uuid, ActivityStatus astatus) {
		try {
			ControllerActivityStatus status = rosMessageFactory
					.newFromType(ControllerActivityStatus._TYPE);
			status.setUuid(uuid);
			status.setStatus(translateActivityState(astatus.getState()));

			activityStatusPublisher.publish(status);
		} catch (Exception e) {
			spaceEnvironment
					.getLog()
					.error(String.format(
							"Could not publish Status change %s for Live Activity %s\n",
							uuid, astatus), e);
		}
	}

	/**
	 * Translate an Interactive Spaces activity status to its ROS message
	 * equivalent.
	 * 
	 * @param status
	 * @return
	 */
	private int translateActivityState(ActivityState state) {
		switch (state) {
		case UNKNOWN:
			return ControllerActivityStatus.STATUS_UNKNOWN;

		case READY:
			return ControllerActivityStatus.STATUS_READY;

		case RUNNING:
			return ControllerActivityStatus.STATUS_RUNNING;

		case ACTIVE:
			return ControllerActivityStatus.STATUS_ACTIVE;

		case CRASHED:
			return ControllerActivityStatus.STATUS_CRASH;

		case STARTUP_ATTEMPT:
			return ControllerActivityStatus.STATUS_STARTUP_ATTEMPT;

		case STARTUP_FAILURE:
			return ControllerActivityStatus.STATUS_STARTUP_FAILURE;

		case ACTIVATE_ATTEMPT:
			return ControllerActivityStatus.STATUS_ACTIVATE_ATTEMPT;

		case ACTIVATE_FAILURE:
			return ControllerActivityStatus.STATUS_ACTIVATE_FAILURE;

		case DEACTIVATE_ATTEMPT:
			return ControllerActivityStatus.STATUS_DEACTIVATE_ATTEMPT;

		case DEACTIVATE_FAILURE:
			return ControllerActivityStatus.STATUS_DEACTIVATE_FAILURE;

		case SHUTDOWN_ATTEMPT:
			return ControllerActivityStatus.STATUS_SHUTDOWN_ATTEMPT;

		case SHUTDOWN_FAILURE:
			return ControllerActivityStatus.STATUS_SHUTDOWN_FAILURE;

		case DEPLOY_ATTEMPT:
			return ControllerActivityStatus.STATUS_DEPLOY_ATTEMPT;

		case DEPLOY_FAILURE:
			return ControllerActivityStatus.STATUS_DEPLOY_FAILURE;

		case DOESNT_EXIST:
			return ControllerActivityStatus.STATUS_DOESNT_EXIST;

		default:
			return ControllerActivityStatus.STATUS_UNKNOWN;
		}
	}

	/**
	 * Set the Ros Environment the controller should run in.
	 * 
	 * @param rosEnvironment
	 */
	public void setRosEnvironment(RosEnvironment rosEnvironment) {
		this.rosEnvironment = rosEnvironment;
	}

	/**
	 * @param spaceControllerActivityInstallManager
	 *            the spaceControllerActivityInstallManager to set
	 */
	public void setSpaceControllerActivityInstallManager(
			SpaceControllerActivityInstallationManager spaceControllerActivityInstaller) {
		this.spaceControllerActivityInstallManager = spaceControllerActivityInstaller;
	}

	/**
	 * @param controllerControl
	 *            the controllerControl to set
	 */
	public void setControllerControl(SpaceControllerControl controllerControl) {
		this.controllerControl = controllerControl;
	}

	/**
	 * @param spaceEnvironment
	 *            the spaceEnvironment to set
	 */
	public void setSpaceEnvironment(
			InteractiveSpacesEnvironment spaceEnvironment) {
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
		private ControllerStatus status;

		public RosControllerHeartbeat() {
			status = rosMessageFactory.newFromType(ControllerStatus._TYPE);
			status.setStatus(ControllerStatus.STATUS_HEARTBEAT);
		}

		@Override
		public void sendHeartbeat() {
			// In case the UUID changed.
			status.setUuid(controllerControl.getControllerInfo().getUuid());
			controllerStatusPublisher.publish(status);
		}
	}
}
