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

package interactivespaces.example.activity.comm.network.tcp.server.hello;

import interactivespaces.activity.impl.BaseActivity;
import interactivespaces.service.comm.network.server.TcpServerNetworkCommunicationEndpoint;
import interactivespaces.service.comm.network.server.TcpServerNetworkCommunicationEndpointListener;
import interactivespaces.service.comm.network.server.TcpServerNetworkCommunicationEndpointService;
import interactivespaces.service.comm.network.server.TcpServerRequest;

import com.google.common.base.Charsets;

/**
 * An Interactive Spaces Java-based activity that demonstrates using a TCP server.
 *
 * <p>
 * A simple test message is received and a text response will be generated.
 *
 * @author Keith M. Hughes
 */
public class HelloTcpServerActivity extends BaseActivity {

  /**
   * The name of the config property for obtaining the TCP server port.
   */
  public static final String CONFIGURATION_PROPERTY_TCP_SERVER_PORT = "space.comm.tcp.server.port";

  /**
   * The terminators for the end of a message.
   */
  public static final byte[][] MESSAGE_TERMINATORS = new byte[][] { new byte[] { '\n' } };

  @Override
  public void onActivitySetup() {
    TcpServerNetworkCommunicationEndpointService communicationEndpointService =
        getSpaceEnvironment().getServiceRegistry().getRequiredService(
            TcpServerNetworkCommunicationEndpointService.SERVICE_NAME);

    int serverPort = getConfiguration().getRequiredPropertyInteger(CONFIGURATION_PROPERTY_TCP_SERVER_PORT);

    TcpServerNetworkCommunicationEndpoint<String> tcpServer =
        communicationEndpointService.newStringServer(MESSAGE_TERMINATORS, Charsets.UTF_8, serverPort, getLog());
    tcpServer.addListener(new TcpServerNetworkCommunicationEndpointListener<String>() {
      @Override
      public void
          onTcpRequest(TcpServerNetworkCommunicationEndpoint<String> endpoint, TcpServerRequest<String> request) {
        handleTcpRequest(request);
      }
    });
    addManagedResource(tcpServer);
  }

  /**
   * Handle the TCP request which has come in.
   *
   * @param request
   *          the request
   */
  private void handleTcpRequest(TcpServerRequest<String> request) {
    String message = request.getMessage();
    getLog().info(String.format("TCP server got request from client %s: %s", request.getRemoteAddress(), message));

    String response = "Hey, the server got :" + message;
    request.writeMessage(response);
  }
}
