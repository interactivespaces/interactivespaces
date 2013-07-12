/*
 * Copyright (C) 2012 Google Inc.
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

package interactivespaces.service.comm.serial;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.service.SupportedService;

import java.util.List;

/**
 * Get {link serialEndpoint} instances.
 *
 * @author Keith M. Hughes
 */
public interface SerialCommunicationEndpointService extends SupportedService {

  /**
   * The name of the service.
   */
  public static final String SERVICE_NAME = "comm.serial";

  /**
   * Get a list of all serial ports ready to be connected to.
   *
   * @return a potentially empty list of ports.
   */
  List<String> getSerialPorts();

  /**
   * Create a new serial endpoint for the given port.
   *
   * @param portName
   *          name of the port (OS dependent)
   *
   * @return a serial endpoint for the given port
   *
   * @throws InteractiveSpacesException
   *           the port has already been opened or no such port exists on the
   *           host
   */
  SerialCommunicationEndpoint newSerialEndpoint(String portName);
}
