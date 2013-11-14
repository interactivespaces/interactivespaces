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

import interactivespaces.activity.Activity;
import interactivespaces.activity.ActivityFilesystem;
import interactivespaces.activity.binary.NativeActivityRunnerFactory;
import interactivespaces.activity.component.ActivityComponentFactory;
import interactivespaces.activity.execution.ActivityExecutionContext;
import interactivespaces.configuration.Configuration;
import interactivespaces.domain.basic.pojo.SimpleSpaceController;
import interactivespaces.system.InteractiveSpacesEnvironment;
import interactivespaces.util.resource.ManagedResource;

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
public interface SpaceController extends ManagedResource {

  /**
   * Environment value giving the controller's
   * {@link NativeActivityRunnerFactory}.
   */
  String ENVIRONMENT_CONTROLLER_NATIVE_RUNNER = "controller.native.runner";

  /**
   * Configuration property giving the UUID of the controller.
   */
  String CONFIGURATION_CONTROLLER_UUID = "interactivespaces.controller.uuid";

  /**
   * Configuration property giving the name of the controller.
   */
  String CONFIGURATION_CONTROLLER_NAME = "interactivespaces.controller.name";

  /**
   * Configuration property giving the description of the controller.
   */
  String CONFIGURATION_CONTROLLER_DESCRIPTION =
      "interactivespaces.controller.description";

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

  /**
   * Prepare an instance of an activity to run.
   *
   * @param activity
   *          information about the activity whose instance is to be initialized
   * @param activityFilesystem
   *          the filesystem for the activity instance
   * @param instance
 *            the instance of the activity being started up
   * @param configuration
*             the configuration for the instance
   * @param executionContext
   *          execution context for this activity
   */
  void initializeActivityInstance(MinimalLiveActivity activity, ActivityFilesystem activityFilesystem,
      Activity instance, Configuration configuration, ActivityExecutionContext executionContext);
}
