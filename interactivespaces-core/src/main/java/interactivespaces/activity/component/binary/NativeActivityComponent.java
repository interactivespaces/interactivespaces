/*
 * Copyright (C) 2013 Google Inc.
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

package interactivespaces.activity.component.binary;

import interactivespaces.activity.binary.NativeActivityRunner;
import interactivespaces.activity.component.ActivityComponent;
import interactivespaces.util.process.restart.RestartStrategy;

/**
 * An activity component for running native activities.
 *
 * @author Keith M. Hughes
 */
public interface NativeActivityComponent extends ActivityComponent {

  /**
   * The name of the component.
   */
  String COMPONENT_NAME = "runner.native";

  /**
   * Configuration property giving the location of the application executable
   * relative to the application installation directory.
   */
  String CONFIGURATION_ACTIVITY_EXECUTABLE = "space.activity.component.native.executable";

  /**
   * Configuration property giving the flags that a native application would use
   * to launch.
   */
  String CONFIGURATION_ACTIVITY_EXECUTABLE_FLAGS = "space.activity.component.native.executable.flags";

  /**
   * Get the native activity runner for the component.
   *
   * @return the native activity runner for the component
   */
  NativeActivityRunner getNativeActivityRunner();

  /**
   * Set the restart strategy to use when the runner is finally created.
   *
   * @param restartStrategy
   *          the restart strategy to use
   */
  void setRestartStrategy(RestartStrategy restartStrategy);
}
