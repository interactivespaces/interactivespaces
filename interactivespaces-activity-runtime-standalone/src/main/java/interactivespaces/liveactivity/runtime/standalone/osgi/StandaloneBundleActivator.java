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

package interactivespaces.liveactivity.runtime.standalone.osgi;

import interactivespaces.configuration.Configuration;
import interactivespaces.controller.SpaceController;
import interactivespaces.liveactivity.runtime.StandardLiveActivityRuntimeComponentFactory;
import interactivespaces.liveactivity.runtime.standalone.development.DevelopmentStandaloneActivityRunner;
import interactivespaces.osgi.service.InteractiveSpacesServiceOsgiBundleActivator;
import interactivespaces.system.InteractiveSpacesEnvironment;

import org.apache.commons.logging.Log;

/**
 * Bundle activator for the standalone controller. This will be activated if it is included in the controller bootstrap
 * directory, but will only do anything if the appropriate mode is set in configuration.
 *
 * @author Trevor Pering
 */
public class StandaloneBundleActivator extends InteractiveSpacesServiceOsgiBundleActivator {

  /**
   * Space environment from launcher.
   */
  private InteractiveSpacesEnvironment spaceEnvironment;

  /**
   * Logging instance.
   *
   * @return logging instance for this activator
   */
  public Log getLog() {
    return spaceEnvironment.getLog();
  }

  @Override
  protected void allRequiredServicesAvailable() {
    spaceEnvironment = getInteractiveSpacesEnvironmentTracker().getMyService();

    getLog().info("Standalone are go!");

    Configuration systemConfiguration = spaceEnvironment.getSystemConfiguration();
    String controllerMode =
        systemConfiguration.getPropertyString(SpaceController.CONFIGURATION_INTERACTIVESPACES_CONTROLLER_MODE, null);
    if (!DevelopmentStandaloneActivityRunner.CONFIGURATION_VALUE_CONTROLLER_MODE_STANDALONE.equals(controllerMode)) {
      getLog().info("Not activating standalone space controller, mode is " + controllerMode);
      return;
    }

    StandardLiveActivityRuntimeComponentFactory runtimeComponentFactory =
        new StandardLiveActivityRuntimeComponentFactory(spaceEnvironment, getBundleContext());

    DevelopmentStandaloneActivityRunner runner = new DevelopmentStandaloneActivityRunner(runtimeComponentFactory, spaceEnvironment);
    addManagedResource(runner);
  }

}
