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
import interactivespaces.controller.client.node.ActiveControllerActivityFactory;
import interactivespaces.osgi.service.InteractiveSpacesServiceOsgiBundleActivator;
import interactivespaces.service.script.ScriptService;

/**
 * An OSGi activator for the script activity wrapper.
 *
 * @author Keith M. Hughes
 */
public class OsgiScriptActivityWrapperActivator extends InteractiveSpacesServiceOsgiBundleActivator {

  /**
   * OSGi service tracker for the interactive spaces environment.
   */
  private MyServiceTracker<ScriptService> scriptServiceTracker;
  private MyServiceTracker<ActiveControllerActivityFactory> activeControllerActivityFactoryTracker;

  private ActiveControllerActivityFactory activeControllerActivityFactory;
  private ScriptActivityWrapperFactory scriptActivityWrapperFactory;

  @Override
  public void onStart() {
    scriptServiceTracker = newMyServiceTracker(ScriptService.class.getName());

    activeControllerActivityFactoryTracker =
        newMyServiceTracker(ActiveControllerActivityFactory.class.getName());
  }

  @Override
  public void onStop() {
    if (activeControllerActivityFactory != null) {
      activeControllerActivityFactory
          .unregisterActivityWrapperFactory(scriptActivityWrapperFactory);
      activeControllerActivityFactory = null;
    }

  }

  @Override
  protected void allRequiredServicesAvailable() {
    ScriptService scriptService = scriptServiceTracker.getMyService();
    activeControllerActivityFactory = activeControllerActivityFactoryTracker.getMyService();

    scriptActivityWrapperFactory = new ScriptActivityWrapperFactory(scriptService);
    activeControllerActivityFactory.registerActivityWrapperFactory(scriptActivityWrapperFactory);
  }
}
