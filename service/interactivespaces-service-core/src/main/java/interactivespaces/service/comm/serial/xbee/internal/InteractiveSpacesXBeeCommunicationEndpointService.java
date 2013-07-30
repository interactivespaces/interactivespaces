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

import com.google.common.collect.Maps;

import interactivespaces.service.comm.serial.SerialCommunicationEndpointService;
import interactivespaces.service.comm.serial.xbee.XBeeCommunicationEndpoint;
import interactivespaces.service.comm.serial.xbee.XBeeCommunicationEndpointService;
import interactivespaces.system.InteractiveSpacesEnvironment;

import org.apache.commons.logging.Log;

import java.util.Map;

/**
 * An XBee communications endpoint service using the Interactive Spaces XBee
 * library.
 *
 * @author Keith M. Hughes s
 */
public class InteractiveSpacesXBeeCommunicationEndpointService implements
    XBeeCommunicationEndpointService {

  /**
   * Space environment for this service.
   */
  private InteractiveSpacesEnvironment spaceEnvironment;

  /**
   * The metadata for the service.
   */
  private Map<String, Object> metadata = Maps.newHashMap();

  @Override
  public Map<String, Object> getMetadata() {
    return metadata;
  }

  @Override
  public String getName() {
    return XBeeCommunicationEndpointService.SERVICE_NAME;
  }

  @Override
  public void startup() {
    // Nothing to do yet.
  }

  @Override
  public void shutdown() {
    // Nothing to do yet.
  }

  @Override
  public XBeeCommunicationEndpoint newXBeeCommunicationEndpoint(String portName, Log log) {
    SerialCommunicationEndpointService serialService =
        spaceEnvironment.getServiceRegistry().getRequiredService(
            SerialCommunicationEndpointService.SERVICE_NAME);

    return new InteractiveSpacesXBeeCommunicationEndpoint(
        serialService.newSerialEndpoint(portName), spaceEnvironment.getExecutorService(), log);
  }

  @Override
  public void setSpaceEnvironment(InteractiveSpacesEnvironment spaceEnvironment) {
    this.spaceEnvironment = spaceEnvironment;
  }
}
