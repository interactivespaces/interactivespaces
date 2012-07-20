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
import interactivespaces.controller.common.ros.RosSpaceControllerSupport;
import interactivespaces.domain.basic.ActivityConfiguration;
import interactivespaces.domain.basic.ConfigurationParameter;
import interactivespaces.domain.basic.LiveActivity;
import interactivespaces.domain.basic.SpaceController;
import interactivespaces.master.server.services.RemoteControllerClient;
import interactivespaces.master.server.services.RemoteSpaceControllerClientListener;
import interactivespaces.master.server.services.internal.RemoteControllerClientListenerHelper;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
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
import org.ros.node.topic.CountDownPublisherListener;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.Subscriber;

import com.google.common.collect.Lists;

/**
 * A {@link RemoteControllerClient} which uses ROS.
 * 
 * @author Keith M. Hughes
 */
public class RosRemoteControllerClient implements RemoteControllerClient {

	private static final byte[] NO_ACTIVITY_REQUEST_DATA = new byte[0];

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
	private Map<String, SpaceControllerCommunicator> controllerCommunicators = new HashMap<String, RosRemoteControllerClient.SpaceControllerCommunicator>();

	/**
	 * Helps with listeners for activity events.
	 */
	private RemoteControllerClientListenerHelper remoteControllerClientListeners = new RemoteControllerClientListenerHelper();

	/**
	 * The ROS environment the client is running in.
	 */
	private MasterRosContext masterRosContext;

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
	 * ROS message deserializer for the full controller status
	 */
	private MessageDeserializer<ControllerFullStatus> controllerFullStatusDeserializer;

	/**
	 * ROS message serializer for the full controller status
	 */
	private MessageSerializer<ActivityConfigurationRequest> activityConfigurationRequestSerializer;

	@Override
	public void startup() {
		log.error("Starting up ROS remote controller");
		Node node = masterRosContext.getNode();

		liveActivityDeployRequestSerializer = node
				.getMessageSerializationFactory().newMessageSerializer(
						"interactivespaces_msgs/LiveActivityDeployRequest");

		liveActivityDeployStatusDeserializer = node
				.getMessageSerializationFactory().newMessageDeserializer(
						"interactivespaces_msgs/LiveActivityDeployStatus");

		controllerFullStatusDeserializer = node
				.getMessageSerializationFactory().newMessageDeserializer(
						"interactivespaces_msgs/ControllerFullStatus");

		activityConfigurationRequestSerializer = node
				.getMessageSerializationFactory().newMessageSerializer(
						"interactivespaces_msgs/ActivityConfigurationRequest");

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
		for (SpaceControllerCommunicator communicator : controllerCommunicators
				.values()) {
			communicator.shutdown();
		}
		controllerCommunicators.clear();

		remoteControllerClientListeners.clear();
	}

	@Override
	public void connect(SpaceController controller) {
		getCommunicator(controller, true);
	}

	@Override
	public void disconnect(SpaceController controller) {
		shutdownCommunicator(controller);
	}

	@Override
	public void requestShutdown(SpaceController controller) {
		sendControllerRequest(controller,
				ControllerRequest.OPERATION_SHUTDOWN_CONTROLLER);

		// Leave attached ROS control topic in place but should shutdown
		// heartbeat alarm (once there is one)
	}

	@Override
	public void requestStatus(SpaceController controller) {
		sendControllerRequest(controller, ControllerRequest.OPERATION_STATUS);
	}

	@Override
	public void shutdownAllActivities(SpaceController controller) {
		sendControllerRequest(controller,
				ControllerRequest.OPERATION_SHUTDOWN_ACTIVITIES);
	}

	@Override
	public void deployActivity(LiveActivity liveActivity,
			LiveActivityDeployRequest request) {
		ByteBuffer serialize = liveActivityDeployRequestSerializer
				.serialize(request);
		sendControllerRequest(liveActivity.getController(),
				ControllerRequest.OPERATION_DEPLOY_LIVE_ACTIVITY,
				serialize.array());
	}

	@Override
	public void fullConfigureActivity(LiveActivity activity) {
		ActivityConfiguration configuration = activity.getConfiguration();
		if (configuration == null) {
			log.info(String.format(
					"No configuration parameters for installed activity %s",
					activity.getUuid()));

			return;
		}

		ArrayList<ActivityConfigurationParameterRequest> parameterRequests = Lists
				.newArrayList();
		for (ConfigurationParameter parameter : configuration.getParameters()) {
			ActivityConfigurationParameterRequest newParameter = new ActivityConfigurationParameterRequest();
			newParameter.operation = ActivityConfigurationParameterRequest.OPERATION_ADD;
			newParameter.name = parameter.getName();
			newParameter.value = parameter.getValue();

			parameterRequests.add(newParameter);
		}

		if (parameterRequests.isEmpty()) {
			log.info(String.format(
					"No configuration parameters for installed activity %s",
					activity.getUuid()));

			return;
		}

		ActivityConfigurationRequest request = new ActivityConfigurationRequest();
		request.parameters = parameterRequests;

		ByteBuffer serialize = activityConfigurationRequestSerializer
				.serialize(request);

		sendActivityRuntimeRequest(activity,
				ControllerActivityRuntimeRequest.OPERATION_CONFIGURE,
				serialize.array());
	}

	@Override
	public void startupActivity(LiveActivity activity) {
		sendActivityRuntimeRequest(activity,
				ControllerActivityRuntimeRequest.OPERATION_STARTUP,
				NO_ACTIVITY_REQUEST_DATA);
	}

	@Override
	public void activateActivity(LiveActivity activity) {
		sendActivityRuntimeRequest(activity,
				ControllerActivityRuntimeRequest.OPERATION_ACTIVATE,
				NO_ACTIVITY_REQUEST_DATA);
	}

	@Override
	public void deactivateActivity(LiveActivity activity) {
		sendActivityRuntimeRequest(activity,
				ControllerActivityRuntimeRequest.OPERATION_DEACTIVATE,
				NO_ACTIVITY_REQUEST_DATA);
	}

	@Override
	public void shutdownActivity(LiveActivity activity) {
		sendActivityRuntimeRequest(activity,
				ControllerActivityRuntimeRequest.OPERATION_SHUTDOWN,
				NO_ACTIVITY_REQUEST_DATA);
	}

	@Override
	public void statusActivity(LiveActivity activity) {
		sendActivityRuntimeRequest(activity,
				ControllerActivityRuntimeRequest.OPERATION_STATUS,
				NO_ACTIVITY_REQUEST_DATA);
	}

	/**
	 * Send a controller request to a controller.
	 * 
	 * <p>
	 * The request is sent asynchronously.
	 * 
	 * @param activity
	 *            The activity the request is being sent to.
	 * @param operation
	 *            The operation requested.
	 */
	private void sendControllerRequest(SpaceController controller, int operation) {
		sendControllerRequest(controller, operation, null);
	}

	/**
	 * Send a controller request to a controller.
	 * 
	 * <p>
	 * The request is sent asynchronously.
	 * 
	 * @param controller
	 *            the controller the request is being sent to
	 * @param operation
	 *            the operation requested
	 * @param payload
	 *            any data to be sent with the request (can be {@code null})
	 */
	private void sendControllerRequest(SpaceController controller,
			int operation, byte[] payload) {
		ControllerRequest request = new ControllerRequest();
		request.operation = operation;

		if (payload != null) {
			request.payload = payload;
		}

		SpaceControllerCommunicator communicator = getCommunicator(controller,
				true);

		communicator.sendControllerRequest(request);
	}

	/**
	 * Send an activity runtime request to a controller.
	 * 
	 * <p>
	 * The request is sent asynchronously.
	 * 
	 * @param activity
	 *            The activity the request is being sent to.
	 * @param operation
	 *            The operation requested.
	 */
	private void sendActivityRuntimeRequest(LiveActivity activity,
			int operation, byte[] data) {
		ControllerActivityRuntimeRequest request = new ControllerActivityRuntimeRequest();
		request.uuid = activity.getUuid();
		request.data = data;

		SpaceControllerCommunicator communicator = getCommunicator(
				activity.getController(), true);

		request.operation = operation;

		communicator.sendActivityRuntimeRequest(request);
	}

	/**
	 * Handle controller status updates.
	 * 
	 * @param status
	 *            The status update.
	 */
	private void handleRemoteControllerStatusUpdate(ControllerStatus status) {
		switch (status.status) {
		case ControllerStatus.STATUS_HEARTBEAT:
			long timestamp = System.currentTimeMillis();
			remoteControllerClientListeners.signalSpaceControllerHeartbeat(
					status.uuid, timestamp);

			break;

		case ControllerStatus.STATUS_FULL:
			ByteBuffer payloadBuffer = ByteBuffer.wrap(status.data);
			payloadBuffer.order(ByteOrder.LITTLE_ENDIAN).position(0)
					.limit(status.data.length);

			ControllerFullStatus fullStatus = controllerFullStatusDeserializer
					.deserialize(payloadBuffer);

			log.info(String.format(
					"Received controller full status %s, %d activities",
					status.uuid, fullStatus.activities.size()));
			for (ControllerActivityStatus activity : fullStatus.activities) {
				log.info(String.format("\tActivity %s, %d\n", activity.uuid,
						activity.status));
				handleRemoteActivityStatusUpdate(activity);
			}

			break;

		case ControllerStatus.STATUS_ACTIVITY_INSTALL:
			ByteBuffer deployStatusBuffer = ByteBuffer.wrap(status.data);
			deployStatusBuffer.order(ByteOrder.LITTLE_ENDIAN).position(0)
					.limit(status.data.length);

			LiveActivityDeployStatus deployStatus = liveActivityDeployStatusDeserializer
					.deserialize(deployStatusBuffer);

			remoteControllerClientListeners
					.signalActivityInstall(
							deployStatus.uuid,
							deployStatus.status == LiveActivityDeployStatus.STATUS_SUCCESS);

			break;

		default:
			log.warn(String.format("Unknown status type %d, for controller %s",
					status.status, status.uuid));
		}
	}

	/**
	 * Handle activity status updates.
	 * 
	 * @param status
	 *            the status update
	 */
	private void handleRemoteActivityStatusUpdate(
			ControllerActivityStatus status) {
		ActivityState newState;

		switch (status.status) {
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

		log.info(String.format("Remote activity %s has reported state %s",
				status.uuid, newState));

		remoteControllerClientListeners.signalActivityStateChange(status.uuid,
				newState);
	}

	@Override
	public RemoteControllerClientListenerHelper getRemoteControllerClientListeners() {
		return remoteControllerClientListeners;
	}

	@Override
	public void addRemoteSpaceControllerClientListener(
			RemoteSpaceControllerClientListener listener) {
		remoteControllerClientListeners.addListener(listener);
	}

	@Override
	public void removeRemoteSpaceControllerClientListener(
			RemoteSpaceControllerClientListener listener) {
		remoteControllerClientListeners.removeListener(listener);
	}

	/**
	 * Get the communicator for a given controller.
	 * 
	 * @param controller
	 *            The controller
	 * @param create
	 *            {@code true} is a communicator should be created if there is
	 *            none associated with the controller.
	 * 
	 * @return The communicator for the controller. Will be {@code null} if
	 *         there is none and creation wasn't specified.
	 */
	private SpaceControllerCommunicator getCommunicator(
			SpaceController controller, boolean create) {
		String remoteNode = controller.getHostId();
		synchronized (controllerCommunicators) {
			SpaceControllerCommunicator communicator = controllerCommunicators
					.get(remoteNode);

			if (communicator == null) {
				communicator = new SpaceControllerCommunicator(controller.getUuid());
				communicator.startup(masterRosContext.getNode(), remoteNode,
						controllerStatusListener, activityStatusListener);
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
	 *            The controller
	 */
	private void shutdownCommunicator(SpaceController controller) {
		String remoteNode = controller.getHostId();
		SpaceControllerCommunicator communicator = null;
		synchronized (controllerCommunicators) {
			communicator = controllerCommunicators.remove(remoteNode);
		}

		if (communicator != null) {
			communicator.shutdown();
			log.info(String.format(
					"Communicator for controller %s shutdown and removed",
					controller.getUuid()));
		}
	}

	/**
	 * @param rosEnvironment
	 *            the rosEnvironment to set
	 */
	public void setMasterRosContext(MasterRosContext masterRosContext) {
		this.masterRosContext = masterRosContext;
	}

	/**
	 * @param log
	 *            the log to set
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
		 * UUID of the space controller.
		 */
		private String uuid;

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

		public SpaceControllerCommunicator(String uuid) {
			this.uuid = uuid;
		}

		/**
		 * Start the communicator up.
		 * 
		 * @param node
		 *            the node which is running the communicator
		 * @param remoteNode
		 *            the remote node
		 * @param activityStatusListener
		 *            the listener for activity status messages
		 */
		public void startup(Node node, String remoteNode,
				MessageListener<ControllerStatus> controllerStatusListener,
				MessageListener<ControllerActivityStatus> activityStatusListener) {
			publisherListener = CountDownPublisherListener.newFromCounts(2, 2,
					2, 2, 2);
			controllerStatusSubscriber = RosSpaceControllerSupport
					.getControllerStatusSubscriber(node, remoteNode,
							controllerStatusListener, null);
			controllerRequestPublisher = RosSpaceControllerSupport
					.getControllerRequestPublisher(node, remoteNode,
							publisherListener);

			activityStatusSubscriber = RosSpaceControllerSupport
					.getControllerActivityStatusSubscriber(node, remoteNode,
							activityStatusListener, null);
			activityRuntimeRequestPublisher = RosSpaceControllerSupport
					.getControllerActivityRuntimeRequestPublisher(node,
							remoteNode, publisherListener);
			
			remoteControllerClientListeners.signalSpaceControllerConnectAttempt(uuid);
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
			
			remoteControllerClientListeners.signalSpaceControllerDisconnectAttempt(uuid);
		}

		/**
		 * Send a request to a controller.
		 * 
		 * @param request
		 *            The request to send.
		 */
		public void sendControllerRequest(ControllerRequest request) {
			try {
				if (publisherListener.awaitNewSubscriber(
						controllerConnectionTimeWait, TimeUnit.MILLISECONDS)) {
					controllerRequestPublisher.publish(request);
				} else {
					throw new InteractiveSpacesException(String.format(
							"No connection to controller in %d milliseconds",
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
		 *            The request to send.
		 */
		public void sendActivityRuntimeRequest(
				ControllerActivityRuntimeRequest request) {
			try {
				if (publisherListener.awaitNewSubscriber(
						controllerConnectionTimeWait, TimeUnit.MILLISECONDS)) {
					activityRuntimeRequestPublisher.publish(request);
				} else {
					throw new InteractiveSpacesException(String.format(
							"No connection to controller in %d milliseconds",
							controllerConnectionTimeWait));
				}
			} catch (InterruptedException e) {
				// TODO(keith): Decide what to do.
				log.warn("Controller activity request interrupted");
			}
		}

	}

}
