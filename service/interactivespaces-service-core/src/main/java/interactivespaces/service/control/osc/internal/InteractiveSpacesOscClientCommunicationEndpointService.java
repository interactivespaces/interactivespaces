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

package interactivespaces.service.control.osc.internal;

import interactivespaces.service.BaseSupportedService;
import interactivespaces.service.comm.network.client.UdpClientNetworkCommunicationEndpointService;
import interactivespaces.service.comm.network.client.internal.netty.NettyUdpClientNetworkCommunicationEndpointService;
import interactivespaces.service.control.osc.OscClientCommunicationEndpoint;
import interactivespaces.service.control.osc.OscClientCommunicationEndpointService;
import interactivespaces.service.control.osc.OscClientPacket;
import interactivespaces.system.ActiveTestInteractiveSpacesEnvironment;

import org.apache.commons.logging.Log;

import java.net.InetSocketAddress;
import java.nio.ByteOrder;

/**
 * An Interactive Spaces implementation of
 * {@link OscClientCommunicationEndpointService}.
 *
 * @author Keith M. Hughes
 */
public class InteractiveSpacesOscClientCommunicationEndpointService extends BaseSupportedService
    implements OscClientCommunicationEndpointService {

  public static void main(String[] args) {
    ActiveTestInteractiveSpacesEnvironment spaceEnvironment =
        ActiveTestInteractiveSpacesEnvironment.newActiveTestInteractiveSpacesEnvironment();

    spaceEnvironment.getServiceRegistry().registerService(
        new NettyUdpClientNetworkCommunicationEndpointService());
    InteractiveSpacesOscClientCommunicationEndpointService service =
        new InteractiveSpacesOscClientCommunicationEndpointService();
    service.setSpaceEnvironment(spaceEnvironment);
    service.startup();

    OscClientCommunicationEndpoint endpoint =
        service.newEndpoint("127.0.0.1", 7771, spaceEnvironment.getLog());
    endpoint.startup();
    OscClientPacket packet = endpoint.newOscPacket("/osc/1", "f");
    packet.writeFloat(440.0f);
    packet.write();
    endpoint.shutdown();
    spaceEnvironment.shutdown();
  }

  /**
   * Service for obtaining UDP communication endpoints
   */
  private UdpClientNetworkCommunicationEndpointService udpEndpointService;

  @Override
  public String getName() {
    return SERVICE_NAME;
  }

  @Override
  public void startup() {
    udpEndpointService =
        spaceEnvironment.getServiceRegistry().getRequiredService(
            UdpClientNetworkCommunicationEndpointService.NAME);
  }

  @Override
  public OscClientCommunicationEndpoint newEndpoint(String remoteHost, int remotePort, Log log) {
    return new InteractiveSpacesOscClientCommunicationsEndpoint(new InetSocketAddress(remoteHost,
        remotePort), udpEndpointService.newClient(ByteOrder.BIG_ENDIAN, log), log);
  }
}
