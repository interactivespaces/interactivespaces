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
import interactivespaces.service.comm.network.client.UdpPacket;
import interactivespaces.service.comm.network.client.internal.netty.NettyUdpClientNetworkCommunicationEndpointService;
import interactivespaces.service.comm.network.server.UdpServerNetworkCommunicationEndpoint;
import interactivespaces.service.comm.network.server.UdpServerNetworkCommunicationEndpointListener;
import interactivespaces.service.comm.network.server.UdpServerRequest;
import interactivespaces.service.comm.network.server.internal.netty.NettyUdpServerNetworkCommunicationEndpointService;
import interactivespaces.system.SimpleInteractiveSpacesEnvironment;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.impl.Jdk14Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A test for the Netty UDP socket client and server.
 *
 * @author Keith M. Hughes
 */
public class NettyUdpSocketTest {

  private Log log;
  private ScheduledExecutorService threadPool;
  private NettyUdpClientNetworkCommunicationEndpointService clientService;
  private NettyUdpServerNetworkCommunicationEndpointService serverService;

  @Before
  public void setup() {
    log = new Jdk14Logger("goober");

    threadPool = Executors.newScheduledThreadPool(100);

    SimpleInteractiveSpacesEnvironment spaceEnvironment = new SimpleInteractiveSpacesEnvironment();
    spaceEnvironment.setExecutorService(threadPool);

    clientService = new NettyUdpClientNetworkCommunicationEndpointService();
    clientService.setSpaceEnvironment(spaceEnvironment);
    clientService.startup();

    serverService = new NettyUdpServerNetworkCommunicationEndpointService();
    serverService.setSpaceEnvironment(spaceEnvironment);
    serverService.startup();
  }

  @After
  public void cleanup() {
    clientService.shutdown();
    serverService.shutdown();
    threadPool.shutdown();
  }

  /**
   * Test a round trip from a UDP client to a UDP server and back.
   *
   * @throws Exception
   *           something bad
   */
  @Test
  public void testUdpPacketSend() throws Exception {
    final byte[] serverResponseExpectedData = new byte[] { 17, 2, 89, 127 };
    byte[] serverRequestExpectedData = new byte[] { 55, 66, 22, 87 };

    int serverPort = 10000;

    final CountDownLatch serverReceiveLatch = new CountDownLatch(1);
    final AtomicReference<UdpServerRequest> serverRequestActualData = new AtomicReference<UdpServerRequest>();

    final CountDownLatch clientReceiveLatch = new CountDownLatch(1);
    final AtomicReference<byte[]> serverResponseActualData = new AtomicReference<byte[]>();

    UdpServerNetworkCommunicationEndpoint serverEndpoint = serverService.newServer(serverPort, log);
    UdpClientNetworkCommunicationEndpoint clientEndpoint = clientService.newClient(log);

    try {
      serverEndpoint.addListener(new UdpServerNetworkCommunicationEndpointListener() {

        @Override
        public void onUdpRequest(UdpServerNetworkCommunicationEndpoint endpoint, UdpServerRequest request) {
          serverRequestActualData.set(request);

          request.writeResponse(serverResponseExpectedData);

          serverReceiveLatch.countDown();
        }
      });

      serverEndpoint.startup();

      clientEndpoint.addListener(new UdpClientNetworkCommunicationEndpointListener() {

        @Override
        public void onUdpResponse(UdpClientNetworkCommunicationEndpoint endpoint, byte[] response,
            InetSocketAddress remoteAddress) {
          serverResponseActualData.set(response);

          clientReceiveLatch.countDown();
        }
      });
      clientEndpoint.startup();

      UdpPacket packet = clientEndpoint.newUdpPacket(serverRequestExpectedData.length);
      packet.writeBytes(serverRequestExpectedData);
      ;
      packet.write(new InetSocketAddress("127.0.0.1", serverPort));

      Assert.assertTrue(clientReceiveLatch.await(5, TimeUnit.SECONDS));
      Assert.assertTrue(serverReceiveLatch.await(5, TimeUnit.SECONDS));
    } finally {
      clientEndpoint.shutdown();
      serverEndpoint.shutdown();
    }

    Assert.assertArrayEquals(serverRequestExpectedData, serverRequestActualData.get().getRequest());
    Assert.assertArrayEquals(serverResponseExpectedData, serverResponseActualData.get());
  }

}
