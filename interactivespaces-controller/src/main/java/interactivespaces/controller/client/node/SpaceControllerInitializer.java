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

package interactivespaces.controller.client.node;

import interactivespaces.activity.binary.NativeActivityRunnerFactory;
import interactivespaces.system.InteractiveSpacesEnvironment;

/**
 * Interface for providing access to resources necessary for initializing a space controller.
 */
public interface SpaceControllerInitializer {

  /**
   * Get the space environment for this controller instance.
   *
   * @return {@link InteractiveSpacesEnvironment} for this controller
   */
  InteractiveSpacesEnvironment getSpaceEnvironment();

  /**
   * Get the activity factory for this controller instance.
   *
   * @return {@link ActiveControllerActivityFactory} for this controller
   */
  ActiveControllerActivityFactory getControllerActivityFactory();

  /**
   * Get the native runner factory for this controller instance.
   *
   * @return {@link NativeActivityRunnerFactory} for this controller
   */
  NativeActivityRunnerFactory getNativeActivityRunnerFactory();
}
