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

package interactivespaces.liveactivity.runtime.osgi;

import interactivespaces.activity.binary.NativeActivityRunnerFactory;
import interactivespaces.liveactivity.runtime.BaseLiveActivityRuntimeListener;
import interactivespaces.liveactivity.runtime.LiveActivityRunnerFactory;
import interactivespaces.liveactivity.runtime.LiveActivityRuntime;
import interactivespaces.osgi.service.InteractiveSpacesServiceOsgiBundleActivator;

/**
 * A listener for live activity runtimes that registers live activity runtime services with OSGi.
 *
 * @author Keith M. Hughes
 */
public class OsgiServiceRegistrationLiveActivityRuntimeListener extends BaseLiveActivityRuntimeListener {

  /**
   * The bundle activator to be used for registering services.
   */
  private InteractiveSpacesServiceOsgiBundleActivator bundleActivator;

  /**
   * Construct a new listener.
   *
   * @param bundleActivator
   *          the bundle activator to be used for registering services
   */
  public OsgiServiceRegistrationLiveActivityRuntimeListener(InteractiveSpacesServiceOsgiBundleActivator bundleActivator) {
    this.bundleActivator = bundleActivator;
  }

  @Override
  public void onLiveActivityRuntimeStartup(LiveActivityRuntime runtime) {

    LiveActivityRunnerFactory liveActivityRunnerFactory = runtime.getLiveActivityRunnerFactory();
    bundleActivator.registerOsgiFrameworkService(LiveActivityRunnerFactory.class.getName(), liveActivityRunnerFactory);

    NativeActivityRunnerFactory nativeActivityRunnerFactory = runtime.getNativeActivityRunnerFactory();
    bundleActivator.registerOsgiFrameworkService(NativeActivityRunnerFactory.class.getName(),
        nativeActivityRunnerFactory);
  }
}
