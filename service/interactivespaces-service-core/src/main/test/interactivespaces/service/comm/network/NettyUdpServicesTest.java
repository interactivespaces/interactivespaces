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
 * the License.
 */

package interactivespaces.service.comm.network;

import interactivespaces.service.comm.network.client.UdpClientNetworkCommunicationEndpoint;
import interactivespaces.service.comm.network.client.UdpClientNetworkCommunicationEndpointListener;
import interactivespaces.service.comm.network.client.internal.netty.NettyUdpClientNetworkCommunicationEndpointService;
import interactivespaces.service.comm.network.server.UdpServerNetworkCommunicationEndpoint;
import interactivespaces.service.comm.network.server.UdpServerNetworkCommunicationEndpointListener;
import interactivespaces.service.comm.network.server.UdpServerRequest;
import interactivespaces.service.comm.network.server.internal.netty.NettyUdpServerNetworkCommunicationEndpointService;
import interactivespaces.system.ActiveTestInteractiveSpacesEnvironment;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * tests for the UDP services
 *
 * @author Keith M. Hughes
 */
public class NettyUdpServicesTest {

  private ActiveTestInteractiveSpacesEnvironment spaceEnvironment;
  private NettyUdpServerNetworkCommunicationEndpointService serverService;
  private NettyUdpClientNetworkCommunicationEndpointService clientService;

  private UdpServerNetworkCommunicationEndpoint serverEndpoint;
  private UdpClientNetworkCommunicationEndpoint clientEndpoint;
  private InetSocketAddress remoteAddress;

  @Before
  public void setup() {
    spaceEnvironment =
        ActiveTestInteractiveSpacesEnvironment.newActiveTestInteractiveSpacesEnvironment();

    serverService = new NettyUdpServerNetworkCommunicationEndpointService();
    serverService.setSpaceEnvironment(spaceEnvironment);
    serverService.startup();

    int serverPort = 10000;
    serverEndpoint = serverService.newServer(serverPort, spaceEnvironment.getLog());

    clientService = new NettyUdpClientNetworkCommunicationEndpointService();
    clientService.setSpaceEnvironment(spaceEnvironment);
    clientService.startup();

    remoteAddress = new InetSocketAddress("127.0.0.1", serverPort);
    clientEndpoint = clientService.newClient(spaceEnvironment.getLog());
  }

  @After
  public void cleanup() {
    clientService.shutdown();
    serverService.shutdown();

    spaceEnvironment.getExecutorService().shutdown();
  }

  /**
   * test a successful connection.
   */
  @Test
  public void testSuccess() throws Exception {

    final String expectedRequestMessage = "You there?";
    byte[] requestBytes = expectedRequestMessage.getBytes();

    final String expectedResponseMessage = "Yabadabadoo!!!!!!!!!";

    final AtomicReference<String> receivedRequestMessage = new AtomicReference<String>();
    final AtomicReference<String> receivedResponseMessage = new AtomicReference<String>();
    final AtomicReference<InetSocketAddress> receivedSocketAddress = new AtomicReference<InetSocketAddress>();

    final CountDownLatch countdownLatch = new CountDownLatch(2);

    serverEndpoint.addListener(new UdpServerNetworkCommunicationEndpointListener() {

      @Override
      public void onUdpRequest(UdpServerNetworkCommunicationEndpoint endpoint,
          UdpServerRequest request) {
        String newValue = new String(request.getRequest());
        receivedRequestMessage.set(newValue);
        if (receivedRequestMessage.get().equals(expectedRequestMessage)) {
          request.writeResponse(expectedResponseMessage.getBytes());
        }

        countdownLatch.countDown();
      }
    });

    clientEndpoint.addListener(new UdpClientNetworkCommunicationEndpointListener() {
      @Override
      public void onUdpResponse(UdpClientNetworkCommunicationEndpoint endpoint, byte[] response,
          InetSocketAddress remoteAddress) {
        String message = new String(response);
        receivedResponseMessage.set(message);
        receivedSocketAddress.set(remoteAddress);

        countdownLatch.countDown();
      }
    });

    serverEndpoint.startup();

    clientEndpoint.startup();
    clientEndpoint.write(remoteAddress, requestBytes);

    Assert.assertTrue("Response took too long", countdownLatch.await(5, TimeUnit.SECONDS));
    Assert.assertEquals(expectedRequestMessage, receivedRequestMessage.get());
    Assert.assertEquals(expectedResponseMessage, receivedResponseMessage.get());
    Assert.assertEquals(remoteAddress.getPort(), receivedSocketAddress.get().getPort());
    Assert.assertEquals(remoteAddress.getHostName(), receivedSocketAddress.get().getHostName());

  }
}
