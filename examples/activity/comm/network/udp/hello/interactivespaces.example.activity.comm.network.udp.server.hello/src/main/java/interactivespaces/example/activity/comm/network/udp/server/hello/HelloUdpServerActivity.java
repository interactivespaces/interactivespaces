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

package interactivespaces.example.activity.comm.network.udp.server.hello;

import interactivespaces.activity.impl.BaseActivity;
import interactivespaces.service.comm.network.server.UdpServerNetworkCommunicationEndpoint;
import interactivespaces.service.comm.network.server.UdpServerNetworkCommunicationEndpointListener;
import interactivespaces.service.comm.network.server.UdpServerNetworkCommunicationEndpointService;
import interactivespaces.service.comm.network.server.UdpServerRequest;

/**
 * An Interactive Spaces Java-based activity that demonstrates using a UDP server.
 *
 * <p>
 * A simple test message is received and a text response will be generated.
 *
 * @author Keith M. Hughes
 */
public class HelloUdpServerActivity extends BaseActivity {

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
    String message = new String(request.getRequest());
    getLog().info(String.format("UDP server got request from client %s: %s", request.getRemoteAddress(), message));

    String response = "Hey, the server got :" + message;
    request.writeResponse(response.getBytes());
  }
}
