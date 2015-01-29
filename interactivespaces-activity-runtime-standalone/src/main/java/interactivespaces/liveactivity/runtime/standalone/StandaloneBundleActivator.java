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

package interactivespaces.liveactivity.runtime.standalone;

import interactivespaces.activity.binary.NativeActivityRunnerFactory;
import interactivespaces.configuration.Configuration;
import interactivespaces.controller.SpaceController;
import interactivespaces.liveactivity.runtime.LiveActivityRunnerFactory;
import interactivespaces.osgi.service.InteractiveSpacesServiceOsgiBundleActivator;
import interactivespaces.system.InteractiveSpacesEnvironment;

import org.apache.commons.logging.Log;

import java.io.File;

/**
 * Bundle activator for the standalone controller. This will be activated if it is included in the controller bootstrap
 * directory, but will only do anything if the appropriate mode is set in configuration.
 *
 * @author Trevor Pering
 */
public class StandaloneBundleActivator extends InteractiveSpacesServiceOsgiBundleActivator {

  /**
   * Config parameter for the instance suffix.
   */
  public static final String CONFIGURATION_INTRERACTIVESPACES_STANDALONE_INSTANCE =
      "interactivespaces.standalone.instance";

  /**
   * Config parameter for activity runtime.
   */
  public static final String CONFIGURATION_INTRERACTIVESPACES_STANDALONE_ACTIVITY_RUNTIME =
      "interactivespaces.standalone.activity.runtime";

  /**
   * Config parameter for activity source.
   */
  public static final String CONFIGURATION_INTRERACTIVESPACES_STANDALONE_ACTIVITY_SOURCE =
      "interactivespaces.standalone.activity.source";

  /**
   * Config parameter for activity config.
   */
  public static final String CONFIGURATION_INTRERACTIVESPACES_STANDALONE_ACTIVITY_CONFIG =
      "interactivespaces.standalone.activity.config";

  /**
   * Config parameter for router type.
   */
  public static final String CONFIGURATION_INTRERACTIVESPACES_STANDALONE_ROUTER_TYPE =
      "interactivespaces.standalone.router.type";

  /**
   * Mode value for standalone controller mode.
   */
  public static final String CONFIGURATION_VALUE_CONTROLLER_MODE_STANDALONE = "standalone";

  /**
   * Space environment from launcher.
   */
  private InteractiveSpacesEnvironment spaceEnvironment;

  /**
   * Tracker for getting the activity factory.
   */
  private MyServiceTracker<LiveActivityRunnerFactory> activityFactoryMyServiceTracker;

  /**
   * Tracker for getting the native activity runner factory.
   */
  private MyServiceTracker<NativeActivityRunnerFactory> nativeActivityRunnerFactoryTracker;

  @Override
  public void onStart() {
    activityFactoryMyServiceTracker = newMyServiceTracker(LiveActivityRunnerFactory.class.getName());

    nativeActivityRunnerFactoryTracker = newMyServiceTracker(NativeActivityRunnerFactory.class.getName());
  }

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
    if (!CONFIGURATION_VALUE_CONTROLLER_MODE_STANDALONE.equals(controllerMode)) {
      getLog().info("Not activating standalone space controller, mode is " + controllerMode);
      return;
    }

    LiveActivityRunnerFactory liveActivityRunnerFactory = activityFactoryMyServiceTracker.getMyService();

    NativeActivityRunnerFactory nativeActivityRunnerFactory = nativeActivityRunnerFactoryTracker.getMyService();

    // TODO(keith): Change once have live activity runtime integration.
    StandaloneActivityRunner standaloneActivityRunner =
        new StandaloneActivityRunner(spaceEnvironment, liveActivityRunnerFactory, null);

    String instance = systemConfiguration.getPropertyString(CONFIGURATION_INTRERACTIVESPACES_STANDALONE_INSTANCE, "");
    standaloneActivityRunner.setInstanceSuffix(instance);

    String activityRuntimePath =
        systemConfiguration.getPropertyString(CONFIGURATION_INTRERACTIVESPACES_STANDALONE_ACTIVITY_RUNTIME);
    getLog().info("activityRuntimePath is " + activityRuntimePath);
    standaloneActivityRunner.setActivityRuntimeDir(new File(activityRuntimePath));

    String activitySourcePath =
        systemConfiguration.getPropertyString(CONFIGURATION_INTRERACTIVESPACES_STANDALONE_ACTIVITY_SOURCE);
    getLog().info("activitySourcePath is " + activitySourcePath);

    String activityConfigPath =
        systemConfiguration.getPropertyString(CONFIGURATION_INTRERACTIVESPACES_STANDALONE_ACTIVITY_CONFIG);
    getLog().info("activityConfigPath is " + activityConfigPath);

    String standaloneRouterType =
        systemConfiguration.getPropertyString(CONFIGURATION_INTRERACTIVESPACES_STANDALONE_ROUTER_TYPE);
    if (standaloneRouterType != null) {
      getLog().info("configuring to use router type " + standaloneRouterType);
      standaloneActivityRunner.setUseStandaloneRouter("standalone".equals(standaloneRouterType));
    }

    standaloneActivityRunner.setActivityConfigFile(new File(activityConfigPath,
        StandaloneActivityRunner.ACTIVITY_SPECIFIC_CONFIG_FILE_NAME));
    standaloneActivityRunner.setLocalConfigFile(new File(activityConfigPath,
        StandaloneActivityRunner.LOCAL_CONFIG_FILE_NAME));

    standaloneActivityRunner.prepareFilesystem();
    standaloneActivityRunner.makeController();
    standaloneActivityRunner.createActivity();
    standaloneActivityRunner.reportStatus();
    standaloneActivityRunner.startupActivity();
    standaloneActivityRunner.reportStatus();
    standaloneActivityRunner.activateActivity();
    standaloneActivityRunner.reportStatus();
    standaloneActivityRunner.startPlayback();
  }

}
