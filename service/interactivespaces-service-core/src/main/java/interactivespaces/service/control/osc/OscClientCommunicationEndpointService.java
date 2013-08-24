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

package interactivespaces.service.control.osc;


import interactivespaces.service.SupportedService;

import org.apache.commons.logging.Log;

/**
 * A communication endpoint service for Open Sound Control clients.
 *
 * @author Keith M. Hughes
 */
public interface OscClientCommunicationEndpointService extends SupportedService {

  /**
   * The name of the service.
   */
  public static final String SERVICE_NAME = "control.osc.client";

  /**
   * Create a new endpoint.
   *
   * @param remoteHost
   *          the remote host containing the OSC server
   * @param remotePort
   *          the port on the remote host for the OSC server
   * @param log
   *          the logger for this connection
   *
   * @return the new endpoint
   */
  OscClientCommunicationEndpoint newEndpoint(String remoteHost, int remotePort, Log log);
}