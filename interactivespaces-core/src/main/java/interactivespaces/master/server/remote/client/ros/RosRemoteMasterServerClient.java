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

package interactivespaces.master.server.remote.client.ros;

import interactivespaces.domain.basic.SpaceController;
import interactivespaces.master.server.remote.RemoteMasterServerConstants;
import interactivespaces.master.server.remote.client.RemoteMasterServerClient;

import java.nio.ByteBuffer;

import org.ros.message.MessageSerializer;
import org.ros.message.interactivespaces_msgs.ControllerDescription;
import org.ros.message.interactivespaces_msgs.MasterServerData;
import org.ros.node.Node;
import org.ros.node.NodeConfiguration;
import org.ros.node.topic.Publisher;
import org.ros.osgi.common.RosEnvironment;

/**
 * A client for talking to an Master Server
 * 
 * @author Keith M. Hughes
 */
public class RosRemoteMasterServerClient implements RemoteMasterServerClient {

	/**
	 * ROS node the client is attached to.
	 */
	private Node node;

	/**
	 * Message serializer for controller descriptions.
	 */
	private MessageSerializer<ControllerDescription> controllerDescriptionSerializer;

	/**
	 * Publisher for the master topic.
	 */
	private Publisher<MasterServerData> masterTopicPublisher;

	@Override
	public void startup(RosEnvironment rosEnvironment) {
		NodeConfiguration configuration = rosEnvironment
				.getPublicNodeConfigurationWithNodeName();
		configuration.setNodeName("interactivespaces/master/client");

		node = rosEnvironment.newNode(configuration);
		controllerDescriptionSerializer = node
				.getMessageSerializationFactory()
				.newMessageSerializer(
						RemoteMasterServerConstants.MASTER_SERVER_CONTROLLER_DESCRIPTION_MESSAGE_TYPE);

		masterTopicPublisher = node.newPublisher(
				RemoteMasterServerConstants.MASTER_SERVER_TOPIC_NAME,
				RemoteMasterServerConstants.MASTER_SERVER_TOPIC_MESSAGE_TYPE);
	}

	@Override
	public void shutdown() {
		if (node != null) {
			node.shutdown();
			node = null;
		}
	}

	@Override
	public void sendControllerDescription(SpaceController controller) {
		ControllerDescription description = new ControllerDescription();
		description.uuid = controller.getUuid();
		description.name = controller.getName();
		description.description = controller.getDescription();
		description.host_id = controller.getHostId();

		MasterServerData data = new MasterServerData();
		data.data_type = MasterServerData.DATA_TYPE_CONTROLLER_STARTUP;

		ByteBuffer serialize = controllerDescriptionSerializer
				.serialize(description);
		data.data = serialize.array();

		masterTopicPublisher.publish(data);
	}
}
