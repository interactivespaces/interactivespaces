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
import interactivespaces.liveactivity.runtime.monitor.RemoteLiveActivityRuntimeMonitorService;
import interactivespaces.liveactivity.runtime.osgi.OsgiServiceRegistrationLiveActivityRuntimeListener;
import interactivespaces.liveactivity.runtime.standalone.development.DevelopmentStandaloneLiveActivityRuntime;
import interactivespaces.osgi.service.InteractiveSpacesServiceOsgiBundleActivator;
import interactivespaces.system.InteractiveSpacesEnvironment;
import interactivespaces.system.InteractiveSpacesSystemControl;
import interactivespaces.system.resources.ContainerResourceManager;

import org.apache.commons.logging.Log;

/**
 * Bundle activator for the standalone controller. This will be activated if it is included in the controller bootstrap
 * directory, but will only do anything if the appropriate mode is set in configuration.
 *
 * @author Trevor Pering
 */
public class StandaloneBundleActivator extends InteractiveSpacesServiceOsgiBundleActivator {

  /**
   * The default value for enabling the remote monitoring.
   */
  private static final String CONFIGURATION_NAME_MONITOR_ENABLE_DEFAULT_STANDALONE = "false";

  /**
   * OSGi service tracker for the interactive spaces control.
   */
  private MyServiceTracker<InteractiveSpacesSystemControl> interactiveSpacesSystemControlTracker;

  /**
   * OSGi service tracker for the interactive spaces control.
   */
  private MyServiceTracker<ContainerResourceManager> containerResourceManagerTracker;

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
  protected void onStart() {
    interactiveSpacesSystemControlTracker = newMyServiceTracker(InteractiveSpacesSystemControl.class.getName());
    containerResourceManagerTracker = newMyServiceTracker(ContainerResourceManager.class.getName());
  }

  @Override
  protected void allRequiredServicesAvailable() {
    spaceEnvironment = getInteractiveSpacesEnvironmentTracker().getMyService();

    getLog().info("Standalone are go!");

    Configuration systemConfiguration = spaceEnvironment.getSystemConfiguration();
    systemConfiguration.setValue(RemoteLiveActivityRuntimeMonitorService.CONFIGURATION_NAME_MONITOR_ENABLE_DEFAULT,
        CONFIGURATION_NAME_MONITOR_ENABLE_DEFAULT_STANDALONE);

    String controllerMode =
        systemConfiguration.getPropertyString(SpaceController.CONFIGURATION_INTERACTIVESPACES_CONTROLLER_MODE, null);
    if (!DevelopmentStandaloneLiveActivityRuntime.CONFIGURATION_VALUE_CONTROLLER_MODE_STANDALONE
        .equals(controllerMode)) {
      getLog().info("Not activating standalone space controller, mode is " + controllerMode);
      return;
    }

    StandardLiveActivityRuntimeComponentFactory runtimeComponentFactory =
        new StandardLiveActivityRuntimeComponentFactory(spaceEnvironment,
            containerResourceManagerTracker.getMyService());

    DevelopmentStandaloneLiveActivityRuntime runtime =
        new DevelopmentStandaloneLiveActivityRuntime(runtimeComponentFactory, spaceEnvironment,
            new OsgiServiceRegistrationLiveActivityRuntimeListener(this),
            interactiveSpacesSystemControlTracker.getMyService());
    addManagedResource(runtime);
  }
}
