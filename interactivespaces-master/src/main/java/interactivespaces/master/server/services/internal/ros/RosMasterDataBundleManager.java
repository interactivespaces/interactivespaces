/*
 * Copyright (C) 2013 Google Inc.
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
 * the LicControllerDense.
 */
package interactivespaces.master.server.services.internal.ros;

import interactivespaces.master.server.services.ActiveSpaceController;
import interactivespaces.master.server.services.internal.BasicMasterDataBundleManager;

import interactivespaces_msgs.ControllerDataRequest;
import interactivespaces_msgs.ControllerRequest;
import org.jboss.netty.buffer.ChannelBuffer;
import org.ros.message.MessageSerializer;
import org.ros.node.ConnectedNode;

/**
 * Ros-based implementation of a master data bundle manager.
 */
public class RosMasterDataBundleManager extends BasicMasterDataBundleManager {

  /**
   * Remote space controller client for communication.
   */
  private RosRemoteSpaceControllerClient rosRemoteSpaceControllerClient;

  /**
   * The ROS environment the client is running in.
   */
  private MasterRosContext masterRosContext;

  /**
   * ROS message serializer for the controller data requests.
   */
  private MessageSerializer<ControllerDataRequest> controllerDataRequestMessageSerializer;

  @Override
  public void startup() {
    super.startup();
    controllerDataRequestMessageSerializer = getMasterNode()
        .getMessageSerializationFactory().newMessageSerializer(ControllerDataRequest._TYPE);
  }

  /**
   * Return the master connection node for communication.
   *
   * @return
   *       master connection node.
   */
  private ConnectedNode getMasterNode() {
    return masterRosContext.getNode();
  }

  /**
   * Create an empty controller data request message.
   *
   * @return
   *       empty controller data request
   */
  private ControllerDataRequest newControllerDataRequest() {
    return getMasterNode().getTopicMessageFactory().newFromType(ControllerDataRequest._TYPE);
  }

  @Override
  protected void sendControllerDataBundleCaptureRequest(ActiveSpaceController controller,
      String destinationUri) {
    ControllerDataRequest request = newControllerDataRequest();
    request.setTransferType(ControllerDataRequest.TRANSFER_TYPE_CONTROLLER_DATA_PERMANENT);
    request.setTransferUri(destinationUri);

    ChannelBuffer serialize = controllerDataRequestMessageSerializer.serialize(request);
    rosRemoteSpaceControllerClient.sendControllerRequest(controller,
        ControllerRequest.OPERATION_CONTROLLER_CAPTURE_DATA, serialize);
  }

  @Override
  protected void sendControllerDataBundleRestoreRequest(ActiveSpaceController controller,
      String sourceUri) {
    ControllerDataRequest request = newControllerDataRequest();
    request.setTransferType(ControllerDataRequest.TRANSFER_TYPE_CONTROLLER_DATA_PERMANENT);
    request.setTransferUri(sourceUri);

    ChannelBuffer serialize = controllerDataRequestMessageSerializer.serialize(request);
    rosRemoteSpaceControllerClient.sendControllerRequest(controller,
        ControllerRequest.OPERATION_CONTROLLER_RESTORE_DATA, serialize);
  }

  /**
   * @param masterRosContext
   *          the rosEnvironment to set
   */
  public void setMasterRosContext(MasterRosContext masterRosContext) {
    this.masterRosContext = masterRosContext;
  }

  /**
   * @param rosRemoteControllerClient
   *           the remote controller client to set
   */
  public void setRosRemoteSpaceControllerClient(RosRemoteSpaceControllerClient rosRemoteControllerClient) {
    this.rosRemoteSpaceControllerClient = rosRemoteControllerClient;
  }
}
