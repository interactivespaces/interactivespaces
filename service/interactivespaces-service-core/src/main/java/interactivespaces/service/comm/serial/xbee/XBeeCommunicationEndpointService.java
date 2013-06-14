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

package interactivespaces.service.comm.serial.xbee;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.service.SupportedService;

import org.apache.commons.logging.Log;

/**
 * A service for obtaining XBee communication endpoints.
 *
 * <p>
 * This communication endpoint assumes the XBee is a Series 2 radio in escaped
 * API mode (AP=2).
 *
 * @author Keith M. Hughes
 */
public interface XBeeCommunicationEndpointService extends SupportedService {

  /**
   * The name of the service.
   */
  public static final String SERVICE_NAME = "comm.xbee";

  /**
   * Create a new XBee endpoint for the given port.
   *
   * @param portName
   *          name of the port (OS dependent)
   * @param log
   *          the log to use for this endpoint
   *
   * @return an XBee endpoint for the given port
   *
   * @throws InteractiveSpacesException
   *           the port has already been opened or no such port exists on the
   *           host
   */
  XBeeCommunicationEndpoint newXBeeCommunicationEndpoint(String portName, Log log);
}
