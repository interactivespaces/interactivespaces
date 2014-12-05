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

package interactivespaces.example.activity.comm.network.tcp.client.hello;

import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.activity.impl.BaseActivity;
import interactivespaces.service.comm.network.client.TcpClientNetworkCommunicationEndpoint;
import interactivespaces.service.comm.network.client.TcpClientNetworkCommunicationEndpointListener;
import interactivespaces.service.comm.network.client.TcpClientNetworkCommunicationEndpointService;

import com.google.common.base.Charsets;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * An Interactive Spaces Java-based activity that demonstrates using a TCP client.
 *
 * <p>
 * A simple test message is sent and a text response will be printed.
 *
 * @author Keith M. Hughes
 */
public class HelloTcpClientActivity extends BaseActivity {

  /**
   * The name of the config property for obtaining the TCP server host.
   */
  public static final String CONFIGURATION_PROPERTY_TCP_SERVER_HOST = "space.comm.tcp.server.host";

  /**
   * The default value for the TCP server host.
   */
  public static final String CONFIGURATION_PROPERTY_DEFAULT_TCP_SERVER_PORT = "127.0.0.1";

  /**
   * The name of the config property for obtaining the TCP server port.
   */
  public static final String CONFIGURATION_PROPERTY_TCP_SERVER_PORT = "space.comm.tcp.server.port";

  /**
   * The terminators for the end of a message.
   */
  public static final byte[][] MESSAGE_TERMINATORS = new byte[][] { new byte[] { '\n' } };

  /**
   * The TCP client.
   */
  private TcpClientNetworkCommunicationEndpoint<String> tcpClient;

  @Override
  public void onActivitySetup() {
    TcpClientNetworkCommunicationEndpointService communicationEndpointService =
        getSpaceEnvironment().getServiceRegistry().getRequiredService(
            TcpClientNetworkCommunicationEndpointService.SERVICE_NAME);

    String remoteTcpServerHost =
        getConfiguration().getPropertyString(CONFIGURATION_PROPERTY_TCP_SERVER_HOST,
            CONFIGURATION_PROPERTY_DEFAULT_TCP_SERVER_PORT);
    int remoteTcpServerPort = getConfiguration().getRequiredPropertyInteger(CONFIGURATION_PROPERTY_TCP_SERVER_PORT);

    try {
      InetAddress remoteTcpServerHostAddress = InetAddress.getByName(remoteTcpServerHost);
      tcpClient =
          communicationEndpointService.newStringClient(MESSAGE_TERMINATORS, Charsets.UTF_8, remoteTcpServerHostAddress,
              remoteTcpServerPort, getLog());
      tcpClient.addListener(new TcpClientNetworkCommunicationEndpointListener<String>() {
        @Override
        public void onTcpResponse(TcpClientNetworkCommunicationEndpoint<String> endpoint, String response) {
          handleTcpResponse(response);
        }
      });
      addManagedResource(tcpClient);
    } catch (UnknownHostException e) {
      throw new SimpleInteractiveSpacesException(String.format("Could not get host %s", remoteTcpServerHost), e);
    }
  }

  @Override
  public void onActivityActivate() {
    tcpClient.write("Hey server, I just activated\n");
  }

  @Override
  public void onActivityDeactivate() {
    tcpClient.write("Hey server, I just deactivated\n");
  }

  /**
   * Handle the TCP response that has come in.
   *
   * @param response
   *          the response
   */
  private void handleTcpResponse(String response) {
    getLog().info(String.format("TCP client got response from server: %s", response));
  }
}
