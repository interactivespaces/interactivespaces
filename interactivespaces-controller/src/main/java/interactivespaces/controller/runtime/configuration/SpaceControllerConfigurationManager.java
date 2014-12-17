/*
 * Copyright (C) 2014 Google Inc.
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

package interactivespaces.controller.runtime.configuration;

import interactivespaces.util.resource.ManagedResource;

import java.util.Map;

/**
 * A manager to control a space controller's configuration.
 *
 * @author Keith M. Hughes
 */
public interface SpaceControllerConfigurationManager extends ManagedResource {

  /**
   * Load the space controller's persisted configuration and modify the configuration in the space environment due to
   * the persisted configuration.
   */
  void load();

  /**
   * Update the configuration.
   *
   * <p>
   * This will save it after it is updated. The space environment will also be updated.
   *
   * @param update
   *          key/value pairs for the update
   */
  void update(Map<String, String> update);
}
