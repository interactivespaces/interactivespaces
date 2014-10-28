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

package interactivespaces.master.api.master;

import interactivespaces.util.resource.ManagedResource;

/**
 * A websocket connection to the master.
 *
 * @author Keith M. Hughes
 */
public interface MasterWebsocketManager extends ManagedResource {

  /**
   * Configuration property for the websocket port for the master.
   */
  String CONFIGURATION_MASTER_WEBSOCKET_PORT = "interactivespaces.master.api.websocket.port";

  /**
   * Default value for configuration property for the websocket port for the
   * master.
   */
  int CONFIGURATION_MASTER_WEBSOCKET_PORT_DEFAULT = 8090;
}
