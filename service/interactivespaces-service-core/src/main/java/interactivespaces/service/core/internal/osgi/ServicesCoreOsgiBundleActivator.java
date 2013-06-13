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

import interactivespaces.service.Service;
import interactivespaces.service.SupportedService;
import interactivespaces.service.comm.serial.xbee.XBeeCommunicationEndpointService;
import interactivespaces.service.comm.serial.xbee.internal.InteractiveSpacesXBeeCommunicationEndpointService;

import org.osgi.framework.BundleContext;

/**
 * The Bundle Activator for the core InteractiveSpaces services.
 *
 * @author Keith M. Hughes
 */
public class ServicesCoreOsgiBundleActivator extends InteractiveSpacesServiceOsgiBundleActivator {

  /**
   * The mail receiver service created by this bundle.
   */
  private InteractiveSpacesXBeeCommunicationEndpointService xbeeCommEndpointService;

  @Override
  public void stop(BundleContext context) throws Exception {
    xbeeCommEndpointService.shutdown();

    interactiveSpacesEnvironmentTracker.getMyService().getServiceRegistry()
        .unregisterService(XBeeCommunicationEndpointService.SERVICE_NAME, xbeeCommEndpointService);

    xbeeCommEndpointService = null;
  }

  @Override
  protected void allServicesAvailable() {
    xbeeCommEndpointService = new InteractiveSpacesXBeeCommunicationEndpointService();

    registerNewInteractiveSpacesService(XBeeCommunicationEndpointService.SERVICE_NAME,
        xbeeCommEndpointService);
  }
}
