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

package interactivespaces.controller.client.node;

import interactivespaces.controller.activity.wrapper.ActivityWrapperFactory;
import interactivespaces.liveactivity.runtime.LiveActivityRunnerFactory;

/**
 * A factory that delegates to the actual factory for registering and unregistering activity wrapper factories.
 *
 * <p>
 * This factory is only used until all activities using cgs stop registering their activity types.
 *
 * @author Keith M. Hughes
 *
 * @deprecated
 */
@Deprecated
public class DoNotUseActiveControllerActivityFactory implements ActiveControllerActivityFactory {

  /**
   * The actual factory to be used.
   */
  private LiveActivityRunnerFactory liveActivityRunnerFactory;

  /**
   * Construct the factory.
   *
   * @param liveActivityRunnerFactory
   *          the factory delegated to
   */
  public DoNotUseActiveControllerActivityFactory(LiveActivityRunnerFactory liveActivityRunnerFactory) {
    this.liveActivityRunnerFactory = liveActivityRunnerFactory;
  }

  @Override
  public void registerActivityWrapperFactory(ActivityWrapperFactory factory) {
    liveActivityRunnerFactory.registerActivityWrapperFactory(factory);
  }

  @Override
  public void unregisterActivityWrapperFactory(ActivityWrapperFactory factory) {
    liveActivityRunnerFactory.unregisterActivityWrapperFactory(factory);
  }
}
