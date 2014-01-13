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

package interactivespaces.service.image.gesture;

import interactivespaces.service.SupportedService;

import org.apache.commons.logging.Log;

/**
 * A service for getting gesture endpoints.
 *
 * @author Keith M. Hughes
 */
public interface GestureService extends SupportedService {

  /**
   * The name for the service.
   */
  String SERVICE_NAME = "image.gesture";

  /**
   * Get a new gesture endpoint with the default connection parameters.
   *
   * @param log
   *          the logger to use
   *
   * @return a new gesture endpoint
   */
  GestureEndpoint newGestureEndpoint(Log log);

  /**
   * Get a new gesture endpoint which connects at a given local host and port
   * for a server-based gesture recognizer.
   *
   * @param host
   *          the host to listen to
   * @param port
   *          the port to listen to
   * @param log
   *          the logger to use
   *
   * @return a new gesture endpoint
   */
  GestureEndpoint newGestureEndpoint(String host, int port, Log log);
}
