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

package interactivespaces.controller.activity.wrapper.internal.script.osgi;

import interactivespaces.controller.activity.wrapper.internal.script.ScriptActivityWrapperFactory;
import interactivespaces.liveactivity.runtime.LiveActivityRunnerFactory;
import interactivespaces.osgi.service.InteractiveSpacesServiceOsgiBundleActivator;
import interactivespaces.service.script.ScriptService;

/**
 * An OSGi activator for the script activity wrapper.
 *
 * @author Keith M. Hughes
 */
public class OsgiScriptActivityWrapperActivator extends InteractiveSpacesServiceOsgiBundleActivator {

  /**
   * The service tracker for the script service.
   */
  private MyServiceTracker<ScriptService> scriptServiceTracker;

  /**
   * The service tracker for the live activity runner factory.
   */
  private MyServiceTracker<LiveActivityRunnerFactory> liveActivityRunnerFactoryTracker;

  /**
   * The live activity runner factory.
   */
  private LiveActivityRunnerFactory liveActivityRunnerFactory;

  /**
   * The activity wrapper factory for scripted activities.
   */
  private ScriptActivityWrapperFactory scriptActivityWrapperFactory;

  @Override
  public void onStart() {
    scriptServiceTracker = newMyServiceTracker(ScriptService.class.getName());
    liveActivityRunnerFactoryTracker = newMyServiceTracker(LiveActivityRunnerFactory.class.getName());
  }

  @Override
  public void onStop() {
    if (liveActivityRunnerFactory != null) {
      liveActivityRunnerFactory.unregisterActivityWrapperFactory(scriptActivityWrapperFactory);
      liveActivityRunnerFactory = null;
    }
  }

  @Override
  protected void allRequiredServicesAvailable() {
    liveActivityRunnerFactory = liveActivityRunnerFactoryTracker.getMyService();

    scriptActivityWrapperFactory = new ScriptActivityWrapperFactory(scriptServiceTracker.getMyService());
    liveActivityRunnerFactory.registerActivityWrapperFactory(scriptActivityWrapperFactory);
  }
}
