/*
 * Copyright (C) 2015 Google Inc.
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

package interactivespaces.liveactivity.runtime;

import interactivespaces.activity.binary.NativeActivityRunnerFactory;
import interactivespaces.activity.component.ActivityComponentFactory;
import interactivespaces.service.ServiceRegistry;

/**
 * A factory for creating various components for a live activity runtime.
 *
 * <p>
 * This is to ensure consistency between the various types of runtimes and controllers.
 *
 * @author Keith M. Hughes
 */
public interface LiveActivityRuntimeComponentFactory {

  /**
   * Create a new live activity runner factory.
   *
   * @return a new live activity runner factory
   */
  LiveActivityRunnerFactory newLiveActivityRunnerFactory();

  /**
   * Create a new native activity runner factory.
   *
   * @return a new native activity runner factory
   */
  NativeActivityRunnerFactory newNativeActivityRunnerFactory();

  /**
   * Create a new activity component factory.
   *
   * @return a new activity component factory
   */
  ActivityComponentFactory newActivityComponentFactory();

  /**
   * Register and startup core components.
   *
   * @param serviceRegistry
   *        the service registry
   */
  void registerCoreServices(ServiceRegistry serviceRegistry);

  /**
   * Unregister and shut down core components.
   *
   * @param serviceRegistry
   *        the service registry
   */
  void unregisterCoreServices(ServiceRegistry serviceRegistry);
}
