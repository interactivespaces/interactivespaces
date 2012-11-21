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

import interactivespaces.activity.ActivityState;
import interactivespaces.activity.ActivityStatus;
import interactivespaces.controller.client.node.ActiveControllerActivity;
import interactivespaces.controller.client.node.SpaceControllerActivityInstaller;
import interactivespaces.controller.client.node.SpaceControllerCommunicator;
import interactivespaces.controller.client.node.SpaceControllerControl;
import interactivespaces.controller.client.node.SpaceControllerHeartbeat;
import interactivespaces.controller.common.ros.RosSpaceControllerConstants;
import interactivespaces.controller.domain.InstalledLiveActivity;
import interactivespaces.domain.basic.pojo.SimpleSpaceController;
import interactivespaces.master.server.remote.client.RemoteMasterServerClient;
import interactivespaces.master.server.remote.client.ros.RosRemoteMasterServerClient;
import interactivespaces.system.InteractiveSpacesEnvironment;
import interactivespaces.util.InteractiveSpacesUtilities;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Map;

import org.ros.message.MessageDeserializer;
import org.ros.message.MessageListener;
import org.ros.message.MessageSerializer;
import org.ros.message.interactivespaces_msgs.ActivityConfigurationParameterRequest;
import org.ros.message.interactivespaces_msgs.ActivityConfigurationRequest;
import org.ros.message.interactivespaces_msgs.ControllerActivityRuntimeRequest;
import org.ros.message.interactivespaces_msgs.ControllerActivityStatus;
import org.ros.message.interactivespaces_msgs.ControllerFullStatus;
import org.ros.message.interactivespaces_msgs.ControllerRequest;
import org.ros.message.interactivespaces_msgs.ControllerStatus;
import org.ros.message.interactivespaces_msgs.LiveActivityDeployRequest;
import org.ros.message.interactivespaces_msgs.LiveActivityDeployStatus;
import org.ros.node.Node;
import org.ros.node.NodeConfiguration;
import org.ros.node.topic.DefaultPublisherListener;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.PublisherListener;
import org.ros.node.topic.Subscriber;
import org.ros.osgi.common.RosEnvironment;

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
	private Node node;

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
	 * ROS message deserializer for the activity configuration requests
	 */
	private MessageDeserializer<ActivityConfigurationRequest> activityConfigurationRequestDeserializer;

	/**
	 * Activity installer for the controller
	 */
	private SpaceControllerActivityInstaller spaceControllerActivityInstaller;

	/**
	 * The space environment for this communicator.
	 */
	private InteractiveSpacesEnvironment spaceEnvironment;

	public RosSpaceControllerCommunicator(
			SpaceControllerActivityInstaller spaceControllerActivityInstaller,
			RosEnvironment rosEnvironment,
			InteractiveSpacesEnvironment spaceEnvironment) {
		this.spaceControllerActivityInstaller = spaceControllerActivityInstaller;
		this.rosEnvironment = rosEnvironment;
		this.spaceEnvironment = spaceEnvironment;
	}

	@Override
	public void onStartup() {
		final NodeConfiguration nodeConfiguration = rosEnvironment
				.getPublicNodeConfigurationWithNodeName();
		nodeConfiguration.setNodeName("interactivespaces/controller");

		node = rosEnvironment.newNode(nodeConfiguration);

		PublisherListener<ControllerStatus> controllerStatusPublisherListener = new DefaultPublisherListener<ControllerStatus>() {
			@Override
			public void onNewSubscriber(Publisher<ControllerStatus> publisher) {
				spaceEnvironment.getLog().info(
						"New subscriber for controller status");
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
	private void handleControllerRequest(ControllerRequest request) {
		switch (request.operation) {
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
			ByteBuffer payloadBuffer = ByteBuffer.wrap(request.payload);
			payloadBuffer.order(ByteOrder.LITTLE_ENDIAN).position(0)
					.limit(request.payload.length);

			LiveActivityDeployRequest deployRequest = liveActivityDeployRequestDeserializer
					.deserialize(payloadBuffer);

			handleLiveActivityDeployment(deployRequest);

			break;

		default:
			spaceEnvironment.getLog().error(
					String.format("Unknown ROS controller request %d",
							request.operation));
		}
	}

	/**
	 * Create and publish controller full status.
	 */
	private void publishControllerFullStatus() {
		spaceEnvironment.getLog().info("Getting full controller status");

		SimpleSpaceController controllerInfo = controllerControl
				.getControllerInfo();

		ControllerStatus status = new ControllerStatus();
		status.uuid = controllerInfo.getUuid();
		status.status = ControllerStatus.STATUS_FULL;

		ControllerFullStatus fullStatus = new ControllerFullStatus();
		fullStatus.name = controllerInfo.getName();
		fullStatus.description = controllerInfo.getDescription();
		fullStatus.host_id = controllerInfo.getHostId();

		for (InstalledLiveActivity activity : controllerControl
				.getAllInstalledLiveActivities()) {
			ControllerActivityStatus cas = new ControllerActivityStatus();
			cas.uuid = activity.getUuid();

			ActiveControllerActivity activeActivity = controllerControl
					.getActiveActivityByUuid(cas.uuid);
			if (activeActivity != null) {
				ActivityState state = activeActivity.getInstance()
						.getActivityStatus().getState();
				cas.status = translateActivityState(state);
				spaceEnvironment
						.getLog()
						.info(String
								.format("Full status live activity %s status %s, returning %d",
										cas.uuid, state, cas.status));
			} else {
				cas.status = ControllerActivityStatus.STATUS_READY;
				spaceEnvironment
						.getLog()
						.info(String
								.format("Full status live activity %s not found, returning READY",
										cas.uuid));
			}

			fullStatus.activities.add(cas);
		}

		ByteBuffer serialize = controllerFullStatusMessageSerializer
				.serialize(fullStatus);
		status.data = serialize.array();

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
		LiveActivityDeployStatus deployStatus = spaceControllerActivityInstaller
				.handleDeploymentRequest(deployRequest);

		ControllerStatus status = new ControllerStatus();
		status.uuid = controllerControl.getControllerInfo().getUuid();
		status.status = ControllerStatus.STATUS_ACTIVITY_INSTALL;

		ByteBuffer serialize = liveActivityDeployStatusSerializer
				.serialize(deployStatus);
		status.data = serialize.array();

		controllerStatusPublisher.publish(status);
	}

	/**
	 * Handle a ROS activity control request coming in.
	 * 
	 * @param request
	 *            The ROS request.
	 */
	private void handleActivityRuntimeRequest(
			ControllerActivityRuntimeRequest request) {
		switch (request.operation) {
		case ControllerActivityRuntimeRequest.OPERATION_STARTUP:
			controllerControl.startupActivity(request.uuid);

			break;

		case ControllerActivityRuntimeRequest.OPERATION_ACTIVATE:
			controllerControl.activateActivity(request.uuid);

			break;

		case ControllerActivityRuntimeRequest.OPERATION_DEACTIVATE:
			controllerControl.deactivateActivity(request.uuid);

			break;

		case ControllerActivityRuntimeRequest.OPERATION_SHUTDOWN:
			controllerControl.shutdownActivity(request.uuid);

			break;

		case ControllerActivityRuntimeRequest.OPERATION_STATUS:
			controllerControl.statusActivity(request.uuid);

			break;

		case ControllerActivityRuntimeRequest.OPERATION_CONFIGURE:
			ByteBuffer payloadBuffer = ByteBuffer.wrap(request.data);
			payloadBuffer.order(ByteOrder.LITTLE_ENDIAN).position(0)
					.limit(request.data.length);

			ActivityConfigurationRequest configurationRequest = activityConfigurationRequestDeserializer
					.deserialize(payloadBuffer);

			handleActivityConfigurationRequest(request.uuid,
					configurationRequest);

			break;

		default:
			spaceEnvironment.getLog().error(
					String.format("Unknown ROS activity runtime request %d",
							request.operation));
		}
	}

	/**
	 * Handle a configuration request.
	 * 
	 * @param uuid
	 * @param configurationRequest
	 */
	private void handleActivityConfigurationRequest(String uuid,
			ActivityConfigurationRequest configurationRequest) {
		Map<String, Object> values = Maps.newHashMap();

		for (ActivityConfigurationParameterRequest parameterRequest : configurationRequest.parameters) {
			if (parameterRequest.operation == ActivityConfigurationParameterRequest.OPERATION_ADD) {
				values.put(parameterRequest.name, parameterRequest.value);
			}
		}

		controllerControl.configureActivity(uuid, values);
	}

	@Override
	public void publishActivityStatus(String uuid, ActivityStatus astatus) {
		try {
			ControllerActivityStatus status = new ControllerActivityStatus();
			status.uuid = uuid;
			status.status = translateActivityState(astatus.getState());

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
	 * @param spaceControllerActivityInstaller
	 *            the spaceControllerActivityInstaller to set
	 */
	public void setSpaceControllerActivityInstaller(
			SpaceControllerActivityInstaller spaceControllerActivityInstaller) {
		this.spaceControllerActivityInstaller = spaceControllerActivityInstaller;
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
			status = new ControllerStatus();
			status.status = ControllerStatus.STATUS_HEARTBEAT;
		}

		@Override
		public void heartbeat() {
			// In case the UUID changed.
			status.uuid = controllerControl.getControllerInfo().getUuid();
			controllerStatusPublisher.publish(status);
		}
	}
}
