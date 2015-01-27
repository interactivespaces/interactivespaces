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

package interactivespaces.activity;

import interactivespaces.activity.binary.NativeActivityRunnerFactory;
import interactivespaces.activity.component.ActivityComponentFactory;
import interactivespaces.system.InteractiveSpacesEnvironment;

/**
 * A base implementation of the activity runtime.
 *
 * @author Keith M. Hughes
 */
public class BaseActivityRuntime implements ActivityRuntime {
  /**
   *
   * The native activity runner factory.
   */
  private NativeActivityRunnerFactory nativeActivityRunnerFactory;

  /**
   * The activity component factory.
   */
  private ActivityComponentFactory activityComponentFactory;

  /**
   * The space environment for the runtime.
   */
  private InteractiveSpacesEnvironment spaceEnvironment;

  /**
   * Construct a runtime.
   *
   * @param nativeActivityRunnerFactory
   *          the native activity runner factory
   * @param activityComponentFactory
   *          the activity component factory
   * @param spaceEnvironment
   *          the space environment
   */
  public BaseActivityRuntime(NativeActivityRunnerFactory nativeActivityRunnerFactory,
      ActivityComponentFactory activityComponentFactory, InteractiveSpacesEnvironment spaceEnvironment) {
    this.nativeActivityRunnerFactory = nativeActivityRunnerFactory;
    this.activityComponentFactory = activityComponentFactory;
    this.spaceEnvironment = spaceEnvironment;
  }

  @Override
  public NativeActivityRunnerFactory getNativeActivityRunnerFactory() {
    return nativeActivityRunnerFactory;
  }

  @Override
  public ActivityComponentFactory getActivityComponentFactory() {
    return activityComponentFactory;
  }

  @Override
  public InteractiveSpacesEnvironment getSpaceEnvironment() {
    return spaceEnvironment;
  }
}
