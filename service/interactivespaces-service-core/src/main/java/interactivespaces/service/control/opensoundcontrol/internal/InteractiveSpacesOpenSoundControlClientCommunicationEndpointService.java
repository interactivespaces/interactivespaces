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
import interactivespaces.service.control.opensoundcontrol.OpenSoundControlConstants;

import org.apache.commons.logging.Log;

import java.net.InetSocketAddress;

/**
 * An Interactive Spaces implementation of
 * {@link OpenSoundControlClientCommunicationEndpointService}.
 *
 * @author Keith M. Hughes
 */
public class InteractiveSpacesOpenSoundControlClientCommunicationEndpointService extends
    BaseSupportedService implements OpenSoundControlClientCommunicationEndpointService {

  @Override
  public String getName() {
    return SERVICE_NAME;
  }

  @Override
  public OpenSoundControlClientCommunicationEndpoint newUdpEndpoint(String remoteHost,
      int remotePort, Log log) {
    UdpClientNetworkCommunicationEndpointService udpEndpointService =
        getSpaceEnvironment().getServiceRegistry().getRequiredService(
            UdpClientNetworkCommunicationEndpointService.SERVICE_NAME);

    return new InteractiveSpacesUdpOpenSoundControlClientCommunicationsEndpoint(
        new InetSocketAddress(remoteHost, remotePort), udpEndpointService.newClient(
            OpenSoundControlConstants.OPEN_SOUND_CONTROL_BYTE_ORDER, log), log);
  }
}
