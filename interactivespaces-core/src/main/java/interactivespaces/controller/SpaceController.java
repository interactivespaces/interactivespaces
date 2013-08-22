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

package interactivespaces.controller;

import interactivespaces.activity.binary.NativeActivityRunnerFactory;
import interactivespaces.activity.component.ActivityComponentFactory;
import interactivespaces.domain.basic.pojo.SimpleSpaceController;
import interactivespaces.system.InteractiveSpacesEnvironment;

/**
 * A controller for Interactive Spaces activities.
 *
 * <p>
 * This controller runs on a given machine and controls a group of activities on
 * that machine.
 *
 *
 * @author Keith M. Hughes
 */
public interface SpaceController {

  /**
   * Environment value giving the controller's
   * {@link NativeActivityRunnerFactory}.
   */
  public static final String ENVIRONMENT_CONTROLLER_NATIVE_RUNNER = "controller.native.runner";

  /**
   * Configuration property giving the UUID of the controller.
   */
  public static final String CONFIGURATION_CONTROLLER_UUID = "interactivespaces.controller.uuid";

  /**
   * Configuration property giving the name of the controller.
   */
  public static final String CONFIGURATION_CONTROLLER_NAME = "interactivespaces.controller.name";

  /**
   * Configuration property giving the description of the controller.
   */
  public static final String CONFIGURATION_CONTROLLER_DESCRIPTION =
      "interactivespaces.controller.description";

  /**
   * Start up the controller.
   */
  void startup();

  /**
   * Shut the controller down.
   *
   * <p>
   * This will shut down all apps in the controller as well.
   */
  void shutdown();

  /**
   * Start up all activities in the controller that aren't currently started.
   */
  void startupAllActivities();

  /**
   * Shut down all activities in the controller.
   */
  void shutdownAllActivities();

  /**
   * Start up an activity given its UUID.
   *
   * @param uuid
   *          UUID of the activity to start.
   */
  void startupActivity(String uuid);

  /**
   * Start up an activity given its UUID.
   *
   * @param uuid
   *          UUID of the activity to start.
   */
  void shutdownActivity(String uuid);

  /**
   * Start up an activity given its UUID.
   *
   * @param uuid
   *          UUID of the activity to activate.
   */
  void activateActivity(String uuid);

  /**
   * Start up an activity given its UUID.
   *
   * @param uuid
   *          UUID of the activity to deactivate
   */
  void deactivateActivity(String uuid);

  /**
   * Cause a status check of an activity given its UUID.
   *
   * @param uuid
   *          UUID of the activity to get the status of
   */
  void statusActivity(String uuid);

  /**
   * Capture data for the given controller from a URI.
   *
   * @param bundleUri
   *          The transfer uri.
   */
  void captureControllerDataBundle(String bundleUri);

  /**
   * Restore data for the given controller from a URI.
   *
   * @param bundleUri
   *          The transfer uri.
   */
  void restoreControllerDataBundle(String bundleUri);

  /**
   * Get a factory for native activities runners.
   *
   * @return the factory to use
   */
  NativeActivityRunnerFactory getNativeActivityRunnerFactory();

  /**
   * Get the activity component factory for the controller.
   *
   * @return the factory for activity components
   */
  ActivityComponentFactory getActivityComponentFactory();

  /**
   * Get the Interactive Spaces environment.
   *
   * @return the space environment
   */
  InteractiveSpacesEnvironment getSpaceEnvironment();

  /**
   * Get information about the controller.
   *
   * @return information about the controller
   */
  SimpleSpaceController getControllerInfo();
}
