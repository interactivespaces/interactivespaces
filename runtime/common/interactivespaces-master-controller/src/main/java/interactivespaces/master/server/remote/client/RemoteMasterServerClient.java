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

package interactivespaces.master.server.remote.client;

import interactivespaces.domain.basic.SpaceController;
import interactivespaces.util.resource.ManagedResource;

/**
 * A client for communicating with a master communication server.
 *
 * @author Keith M. Hughes
 */
public interface RemoteMasterServerClient extends ManagedResource {

  /**
   * Register a space controller.
   *
   * @param controller
   *          the controller information
   */
  void registerSpaceController(SpaceController controller);
}
