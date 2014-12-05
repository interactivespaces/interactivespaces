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

package interactivespaces.example.activity.comm.network.udp.client.hello;

import interactivespaces.activity.impl.BaseActivity;
import interactivespaces.service.comm.network.client.UdpClientNetworkCommunicationEndpoint;
import interactivespaces.service.comm.network.client.UdpClientNetworkCommunicationEndpointListener;
import interactivespaces.service.comm.network.client.UdpClientNetworkCommunicationEndpointService;

import java.net.InetSocketAddress;

/**
 * An Interactive Spaces Java-based activity which demonstrates using a UDP client.
 *
 * <p>
 * A simple test message is sent and a text response will be printed.
 *
 * @author Keith M. Hughes
 */
public class HelloUdpClientActivity extends BaseActivity {

  /**
   * The name of the config property for obtaining the UDP server host.
   */
  public static final String CONFIGURATION_PROPERTY_UDP_SERVER_HOST = "space.comm.udp.server.host";

  /**
   * The default value for the UDP server host.
   */
  public static final String CONFIGURATION_PROPERTY_DEFAULT_UDP_SERVER_PORT = "127.0.0.1";

  /**
   * The name of the config property for obtaining the UDP server port.
   */
  public static final String CONFIGURATION_PROPERTY_UDP_SERVER_PORT = "space.comm.udp.server.port";

  /**
   * The address of the UDP server.
   */
  private InetSocketAddress udpServerAddress;

  /**
   * The UDP client.
   */
  private UdpClientNetworkCommunicationEndpoint udpClient;

  @Override
  public void onActivitySetup() {
    UdpClientNetworkCommunicationEndpointService communicationEndpointService =
        getSpaceEnvironment().getServiceRegistry().getRequiredService(
            UdpClientNetworkCommunicationEndpointService.SERVICE_NAME);

    String remoteUdpServerHost =
        getConfiguration().getPropertyString(CONFIGURATION_PROPERTY_UDP_SERVER_HOST,
            CONFIGURATION_PROPERTY_DEFAULT_UDP_SERVER_PORT);
    int remoteUdpServerPort = getConfiguration().getRequiredPropertyInteger(CONFIGURATION_PROPERTY_UDP_SERVER_PORT);

    udpClient = communicationEndpointService.newClient(getLog());
    udpClient.addListener(new UdpClientNetworkCommunicationEndpointListener() {
      @Override
      public void onUdpResponse(UdpClientNetworkCommunicationEndpoint endpoint, byte[] response,
          InetSocketAddress remoteAddress) {
        handleUdpResponse(response, remoteAddress);
      }
    });
    addManagedResource(udpClient);

    udpServerAddress = new InetSocketAddress(remoteUdpServerHost, remoteUdpServerPort);
  }

  @Override
  public void onActivityActivate() {
    udpClient.write(udpServerAddress, "Hey server, I just activated".getBytes());
  }

  @Override
  public void onActivityDeactivate() {
    udpClient.write(udpServerAddress, "Hey server, I just deactivated".getBytes());
  }

  /**
   * Handle the UDP response that has come in.
   *
   * @param response
   *          the response
   * @param remoteAddress
   *          address of the emote UDP
   */
  private void handleUdpResponse(byte[] response, InetSocketAddress remoteAddress) {
    String message = new String(response);
    getLog().info(String.format("UDP client got response from server %s: %s", remoteAddress, message));
  }
}
