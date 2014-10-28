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

package interactivespaces.service.control.opensoundcontrol.internal;

import interactivespaces.service.BaseSupportedService;
import interactivespaces.service.comm.network.client.UdpClientNetworkCommunicationEndpointService;
import interactivespaces.service.control.opensoundcontrol.OpenSoundControlClientCommunicationEndpoint;
import interactivespaces.service.control.opensoundcontrol.OpenSoundControlClientCommunicationEndpointService;

import org.apache.commons.logging.Log;

import java.net.InetSocketAddress;
import java.nio.ByteOrder;

/**
 * An Interactive Spaces implementation of
 * {@link OpenSoundControlClientCommunicationEndpointService}.
 *
 * @author Keith M. Hughes
 */
public class InteractiveSpacesOpenSoundControlClientCommunicationEndpointService extends
    BaseSupportedService implements OpenSoundControlClientCommunicationEndpointService {

  /**
   * Service for obtaining UDP communication endpoints.
   */
  private UdpClientNetworkCommunicationEndpointService udpEndpointService;

  @Override
  public String getName() {
    return SERVICE_NAME;
  }

  @Override
  public void startup() {
    udpEndpointService =
        getSpaceEnvironment().getServiceRegistry().getRequiredService(
            UdpClientNetworkCommunicationEndpointService.SERVICE_NAME);
  }

  @Override
  public OpenSoundControlClientCommunicationEndpoint newUdpEndpoint(String remoteHost,
      int remotePort, Log log) {
    return new InteractiveSpacesUdpOpenSoundControlClientCommunicationsEndpoint(
        new InetSocketAddress(remoteHost, remotePort), udpEndpointService.newClient(
            ByteOrder.BIG_ENDIAN, log), log);
  }
}
