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
import interactivespaces.service.comm.network.client.internal.netty.NettyUdpClientNetworkCommunicationEndpointService;
import interactivespaces.service.comm.network.server.internal.netty.NettyUdpServerNetworkCommunicationEndpointService;
import interactivespaces.service.comm.serial.xbee.internal.InteractiveSpacesXBeeCommunicationEndpointService;
import interactivespaces.service.control.osc.OscClientCommunicationEndpointService;
import interactivespaces.service.control.osc.internal.InteractiveSpacesOscClientCommunicationEndpointService;

/**
 * The Bundle Activator for the core InteractiveSpaces services.
 *
 * @author Keith M. Hughes
 */
public class ServicesCoreOsgiBundleActivator extends InteractiveSpacesServiceOsgiBundleActivator {

  /**
   * The XBee service created by this bundle.
   */
  private InteractiveSpacesXBeeCommunicationEndpointService xbeeCommEndpointService;

  /**
   * UDP Client service
   */
  private NettyUdpClientNetworkCommunicationEndpointService udpClientService;

  /**
   * UDP Client service
   */
  private NettyUdpServerNetworkCommunicationEndpointService udpServerService;

  /**
   * Open Sound Control service
   */
  private OscClientCommunicationEndpointService oscService;

  @Override
  protected void allRequiredServicesAvailable() {
    xbeeCommEndpointService = new InteractiveSpacesXBeeCommunicationEndpointService();

    registerNewInteractiveSpacesService(xbeeCommEndpointService);

    udpClientService = new NettyUdpClientNetworkCommunicationEndpointService();
    registerNewInteractiveSpacesService(udpClientService);

    udpServerService = new NettyUdpServerNetworkCommunicationEndpointService();
    registerNewInteractiveSpacesService(udpServerService);

    oscService = new InteractiveSpacesOscClientCommunicationEndpointService();
    registerNewInteractiveSpacesService(oscService);
  }
}
