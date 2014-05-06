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

package interactivespaces.service.comm.serial.xbee.internal;

import interactivespaces.service.BaseSupportedService;
import interactivespaces.service.comm.serial.SerialCommunicationEndpointService;
import interactivespaces.service.comm.serial.xbee.XBeeCommunicationEndpoint;
import interactivespaces.service.comm.serial.xbee.XBeeCommunicationEndpointService;

import org.apache.commons.logging.Log;

/**
 * An XBee communications endpoint service using the Interactive Spaces XBee
 * library.
 *
 * @author Keith M. Hughes s
 */
public class InteractiveSpacesXBeeCommunicationEndpointService extends BaseSupportedService implements
    XBeeCommunicationEndpointService {

  @Override
  public String getName() {
    return XBeeCommunicationEndpointService.SERVICE_NAME;
  }

  @Override
  public XBeeCommunicationEndpoint newXBeeCommunicationEndpoint(String portName, Log log) {
    SerialCommunicationEndpointService serialService =
        getSpaceEnvironment().getServiceRegistry().getRequiredService(SerialCommunicationEndpointService.SERVICE_NAME);

    return new InteractiveSpacesXBeeCommunicationEndpoint(serialService.newSerialEndpoint(portName),
        getSpaceEnvironment().getExecutorService(), log);
  }
}
