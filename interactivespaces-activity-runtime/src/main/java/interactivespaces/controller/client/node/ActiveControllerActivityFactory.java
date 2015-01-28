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

import interactivespaces.controller.activity.wrapper.ActivityWrapperFactory;

/**
 * A factory for active controller instances.
 *
 * @author Keith M. Hughes
 *
 * @deprecated Use {@link LiveactivityRUnnerFactory}
 */
@Deprecated
public interface ActiveControllerActivityFactory {

  /**
   * Register an {@link ActivityWrapperFactory}.
   *
   * @param factory
   *          activity wrapper factory
   */
  void registerActivityWrapperFactory(ActivityWrapperFactory factory);

  /**
   * Unregister an {@link ActivityWrapperFactory}.
   *
   * <p>
   * Nothing happens if the factory was never registered.
   *
   * @param factory
   *          activity wrapper factory
   */
  void unregisterActivityWrapperFactory(ActivityWrapperFactory factory);

}
