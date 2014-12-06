/*
 * Copyright (C) 2014 Google Inc.
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

package interactivespaces.example.activity.comm.network.udp.server.data;

import interactivespaces.activity.impl.BaseActivity;
import interactivespaces.service.comm.network.server.UdpServerNetworkCommunicationEndpoint;
import interactivespaces.service.comm.network.server.UdpServerNetworkCommunicationEndpointListener;
import interactivespaces.service.comm.network.server.UdpServerNetworkCommunicationEndpointService;
import interactivespaces.service.comm.network.server.UdpServerRequest;

import java.nio.ByteBuffer;

/**
 * An Interactive Spaces Java-based activity that demonstrates using a UDP server with binary data.
 *
 * <p>
 * A simple data message of an integer followed by a float is received and a response is generated consisting of the
 * float multiplied by 3 followed by the int multiplied by 5.
 *
 * @author Keith M. Hughes
 */
public class DataUdpServerActivity extends BaseActivity {

  /**
   * The name of the config property for obtaining the UDP server port.
   */
  public static final String CONFIGURATION_PROPERTY_UDP_SERVER_PORT = "space.comm.udp.server.port";

  @Override
  public void onActivitySetup() {
    UdpServerNetworkCommunicationEndpointService communicationEndpointService =
        getSpaceEnvironment().getServiceRegistry().getRequiredService(
            UdpServerNetworkCommunicationEndpointService.SERVICE_NAME);

    int serverPort = getConfiguration().getRequiredPropertyInteger(CONFIGURATION_PROPERTY_UDP_SERVER_PORT);

    UdpServerNetworkCommunicationEndpoint udpServer = communicationEndpointService.newServer(serverPort, getLog());
    udpServer.addListener(new UdpServerNetworkCommunicationEndpointListener() {
      @Override
      public void onUdpRequest(UdpServerNetworkCommunicationEndpoint endpoint, UdpServerRequest request) {
        handleUdpRequest(request);
      }
    });
    addManagedResource(udpServer);
  }

  /**
   * Handle the UDP request that has come in.
   *
   * @param request
   *          the request
   */
  private void handleUdpRequest(UdpServerRequest request) {
    // The items must be read in the order written by the sender, or the reads need to specify the exact locations
    // of the data.
    ByteBuffer byteBuffer = request.newRequestByteBuffer();
    int intNumber = byteBuffer.getInt();
    float floatNumber = byteBuffer.getFloat();

    getLog().info(
        String.format("UDP server got request from client %s: int is %d, float is %f", request.getRemoteAddress(),
            intNumber, floatNumber));

    request.newWriteableUdpPacket(8).writeFloat(floatNumber * 3).writeInt(intNumber * 5)
        .write(request.getRemoteAddress());
  }
}
