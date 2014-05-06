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

package interactivespaces.service.core.internal.osgi;

import interactivespaces.osgi.service.InteractiveSpacesServiceOsgiBundleActivator;
import interactivespaces.service.comm.network.client.internal.netty.NettyTcpClientNetworkCommunicationEndpointService;
import interactivespaces.service.comm.network.client.internal.netty.NettyUdpClientNetworkCommunicationEndpointService;
import interactivespaces.service.comm.network.server.internal.netty.NettyTcpServerNetworkCommunicationEndpointService;
import interactivespaces.service.comm.network.server.internal.netty.NettyUdpServerNetworkCommunicationEndpointService;
import interactivespaces.service.comm.serial.xbee.internal.InteractiveSpacesXBeeCommunicationEndpointService;
import interactivespaces.service.control.opensoundcontrol.internal.InteractiveSpacesOpenSoundControlClientCommunicationEndpointService;

/**
 * The Bundle Activator for the core InteractiveSpaces services.
 *
 * @author Keith M. Hughes
 */
public class ServicesCoreOsgiBundleActivator extends InteractiveSpacesServiceOsgiBundleActivator {

  @Override
  protected void allRequiredServicesAvailable() {
    registerNewInteractiveSpacesService(new InteractiveSpacesXBeeCommunicationEndpointService());

    registerNewInteractiveSpacesService(new NettyUdpClientNetworkCommunicationEndpointService());

    registerNewInteractiveSpacesService(new NettyUdpServerNetworkCommunicationEndpointService());

    registerNewInteractiveSpacesService(new NettyTcpClientNetworkCommunicationEndpointService());

    registerNewInteractiveSpacesService(new NettyTcpServerNetworkCommunicationEndpointService());

    registerNewInteractiveSpacesService(new InteractiveSpacesOpenSoundControlClientCommunicationEndpointService());
  }
}
