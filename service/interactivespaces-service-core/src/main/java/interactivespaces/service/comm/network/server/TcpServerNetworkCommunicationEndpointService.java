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

package interactivespaces.service.comm.network.server;

import interactivespaces.service.SupportedService;

import org.apache.commons.logging.Log;

import java.nio.charset.Charset;

/**
 * A service for {@link TcpServerNetworkCommunicationEndpoint} instances.
 *
 * @author Keith M. Hughes
 */
public interface TcpServerNetworkCommunicationEndpointService extends SupportedService {

  /**
   * Name for the service.
   */
  String SERVICE_NAME = "comm.network.tcp.server";

  /**
   * Create a new TCP server endpoint which uses strings for messages.
   *
   * @param delimiters
   *          the delimiters for messages
   * @param charset
   *          the character set for messages
   * @param serverPort
   *          port the server will listen to
   * @param log
   *          the logger to use
   *
   * @return the communication endpoint
   */
  TcpServerNetworkCommunicationEndpoint<String> newStringServer(byte[][] delimiters, Charset charset, int serverPort,
      Log log);
}
