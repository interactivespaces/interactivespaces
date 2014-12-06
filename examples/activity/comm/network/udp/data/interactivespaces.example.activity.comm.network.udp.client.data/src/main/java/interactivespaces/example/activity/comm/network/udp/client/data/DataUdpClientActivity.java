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

package interactivespaces.example.activity.comm.network.udp.client.data;

import interactivespaces.activity.impl.BaseActivity;
import interactivespaces.service.comm.network.client.UdpClientNetworkCommunicationEndpoint;
import interactivespaces.service.comm.network.client.UdpClientNetworkCommunicationEndpointListener;
import interactivespaces.service.comm.network.client.UdpClientNetworkCommunicationEndpointService;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

/**
 * An Interactive Spaces Java-based activity that demonstrates using a UDP client that transmits numeric data.
 *
 * <p>
 * The number of times the client has been activated will be sent as an int. Deactivations send the negative of the
 * activation count as an int. Activations send {@code 3.14} as a float, deactivations send {@code 2.71} as a float. The
 * message sends the int first, followed by the float.
 *
 * <p>
 * The server responds with a float followed by an int. Server responses are printed.
 *
 * @author Keith M. Hughes
 */
public class DataUdpClientActivity extends BaseActivity {

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

  /**
   * The activation count.
   */
  private int activationCount = 0;

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
    udpClient.newWriteableUdpPacket(8).writeInt(++activationCount).writeFloat(3.14f).write(udpServerAddress);
  }

  @Override
  public void onActivityDeactivate() {
    udpClient.newWriteableUdpPacket(8).writeInt(-activationCount).writeFloat(2.71f).write(udpServerAddress);
  }

  /**
   * Handle the UDP response that has come in.
   *
   * @param response
   *          the response
   * @param remoteAddress
   *          address of the remote UDP
   */
  private void handleUdpResponse(byte[] response, InetSocketAddress remoteAddress) {
    // The client must read the data in the exact order it is written by the server.
    ByteBuffer byteBuffer = udpClient.newByteBuffer(response);
    float floatNumber = byteBuffer.getFloat();
    int intNumber = byteBuffer.getInt();

    getLog()
        .info(String.format("UDP client got response from server %s: %f %d", remoteAddress, floatNumber, intNumber));
  }
}
