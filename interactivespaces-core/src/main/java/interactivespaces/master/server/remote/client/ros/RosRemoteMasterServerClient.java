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

import interactivespaces_msgs.ControllerDescription;
import interactivespaces_msgs.MasterServerData;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.ros.message.MessageSerializer;
import org.ros.node.ConnectedNode;
import org.ros.node.NodeConfiguration;
import org.ros.node.topic.Publisher;
import org.ros.osgi.common.RosEnvironment;

import java.nio.ByteOrder;

/**
 * A client for talking to an Master Server
 *
 * @author Keith M. Hughes
 */
public class RosRemoteMasterServerClient implements RemoteMasterServerClient {

  /**
   * ROS node the client is attached to.
   */
  private ConnectedNode node;

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
    NodeConfiguration configuration = rosEnvironment.getPublicNodeConfigurationWithNodeName();
    configuration.setNodeName("interactivespaces/master/client");

    node = rosEnvironment.newNode(configuration);

    controllerDescriptionSerializer =
        node.getMessageSerializationFactory().newMessageSerializer(
            RemoteMasterServerConstants.MASTER_SERVER_CONTROLLER_DESCRIPTION_MESSAGE_TYPE);

    masterTopicPublisher =
        node.newPublisher(RemoteMasterServerConstants.MASTER_SERVER_TOPIC_NAME,
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
    ControllerDescription description =
        node.getTopicMessageFactory().newFromType(ControllerDescription._TYPE);
    description.setUuid(controller.getUuid());
    description.setName(controller.getName());
    description.setDescription(controller.getDescription());
    description.setHostId(controller.getHostId());

    MasterServerData data = masterTopicPublisher.newMessage();
    data.setDataType(MasterServerData.DATA_TYPE_CONTROLLER_STARTUP);

    ChannelBuffer serialize = controllerDescriptionSerializer.serialize(description);
    data.setData(serialize);

    masterTopicPublisher.publish(data);
  }
}
