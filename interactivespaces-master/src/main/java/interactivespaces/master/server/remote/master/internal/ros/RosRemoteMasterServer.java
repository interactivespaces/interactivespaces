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

package interactivespaces.master.server.remote.master.internal.ros;

import interactivespaces.domain.basic.SpaceController;
import interactivespaces.domain.basic.pojo.SimpleSpaceController;
import interactivespaces.master.server.remote.RemoteMasterServerConstants;
import interactivespaces.master.server.remote.master.RemoteMasterServer;
import interactivespaces.master.server.remote.master.RemoteMasterServerListener;
import interactivespaces.master.server.services.internal.ros.MasterRosContext;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.logging.Log;
import org.ros.message.MessageDeserializer;
import org.ros.message.MessageListener;
import org.ros.message.interactivespaces_msgs.ControllerDescription;
import org.ros.message.interactivespaces_msgs.MasterServerData;
import org.ros.node.Node;
import org.ros.node.topic.Subscriber;

/**
 * A ROS-based {@link RemoteMasterServer}.
 * 
 * @author Keith M. Hughes
 */
public class RosRemoteMasterServer implements RemoteMasterServer {

	/**
	 * The ROS environment the client is running in.
	 */
	private MasterRosContext masterRosContext;

	/**
	 * Logger for the controller.
	 */
	private Log log;

	/**
	 * ROS message deserializer for the full controller status
	 */
	private MessageDeserializer<ControllerDescription> controllerDescriptionDeserializer;

	/**
	 * Subscriber for the master topic.
	 */
	private Subscriber<MasterServerData> masterTopicSubscriber;

	/**
	 * All listeners for master server events.
	 */
	private List<RemoteMasterServerListener> listeners = new CopyOnWriteArrayList<RemoteMasterServerListener>();

	@Override
	public void startup() {
		log.error("Starting up ROS master server");
		Node node = masterRosContext.getNode();

		masterTopicSubscriber = node.newSubscriber(
				RemoteMasterServerConstants.MASTER_SERVER_TOPIC_NAME,
				RemoteMasterServerConstants.MASTER_SERVER_TOPIC_MESSAGE_TYPE);
		masterTopicSubscriber
				.addMessageListener(new MessageListener<MasterServerData>() {
					@Override
					public void onNewMessage(MasterServerData data) {
						handleMasterServerData(data);
					}
				});

		controllerDescriptionDeserializer = node
				.getMessageSerializationFactory()
				.newMessageDeserializer(
						RemoteMasterServerConstants.MASTER_SERVER_CONTROLLER_DESCRIPTION_MESSAGE_TYPE);
	}

	@Override
	public void shutdown() {
		if (masterTopicSubscriber != null) {
			masterTopicSubscriber.shutdown();
			masterTopicSubscriber = null;
		}
	}

	@Override
	public void addListener(RemoteMasterServerListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeListener(RemoteMasterServerListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Master server data has come in.
	 * 
	 * @param data
	 *            the data
	 */
	private void handleMasterServerData(MasterServerData data) {

		switch (data.data_type) {
		case MasterServerData.DATA_TYPE_CONTROLLER_STARTUP:
			ByteBuffer payloadBuffer = ByteBuffer.wrap(data.data);
			payloadBuffer.order(ByteOrder.LITTLE_ENDIAN).position(0)
					.limit(data.data.length);

			ControllerDescription controllerDescription = controllerDescriptionDeserializer
					.deserialize(payloadBuffer);

			handleControllerDescription(controllerDescription);

			break;

		default:
			log.error(String.format("Unknown MasterServerData data type %d",
					data.data_type));
		}
	}

	/**
	 * A new controller description has come in.
	 * 
	 * @param controllerDescription
	 */
	private void handleControllerDescription(
			ControllerDescription controllerDescription) {
		log.info(String.format("Controller %s (Host ID %s) is online.",
				controllerDescription.uuid, controllerDescription.host_id));

		SpaceController controller = new SimpleSpaceController();
		controller.setUuid(controllerDescription.uuid);
		controller.setName(controllerDescription.name);
		controller.setDescription(controllerDescription.description);
		controller.setHostId(controllerDescription.host_id);

		signalControllerRegisteration(controller);
	}

	/**
	 * Signal all listeners about a new controller registration.
	 * 
	 * @param controller
	 *            information about the controller
	 */
	private void signalControllerRegisteration(SpaceController controller) {
		for (RemoteMasterServerListener listener : listeners) {
			listener.onControllerRegistration(controller);
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
}
