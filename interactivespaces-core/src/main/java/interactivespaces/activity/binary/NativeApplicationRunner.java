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

package interactivespaces.activity.binary;

import interactivespaces.util.process.restart.RestartStrategy;
import interactivespaces.util.process.restart.Restartable;
import interactivespaces.util.resource.ManagedResource;

import java.util.Map;

/**
 * A launcher of apps native to the given system.
 *
 * <p>
 * The configuration needs a property with name {#ACTIVITYNAME} which gives the
 * full descriptor (e.g. path on a Linux system) to the application.
 *
 * @author Keith M. Hughes
 */
public interface NativeApplicationRunner extends ManagedResource, Restartable {

  /**
   * The name of the property which gives the fully qualified name for the
   * application.
   */
  String ACTIVITYNAME = "activity";

  /**
   * A set of flags for the application.
   */
  String FLAGS = "flags";

  /**
   * Configure the launcher.
   *
   * @param config
   *          The configuration.
   */
  void configure(Map<String, Object> config);

  /**
   * Is the native app still running?
   *
   * @return True if the app is still running, false otherwise.
   */
  boolean isRunning();

  /**
   * Set the restart strategy for the runner.
   *
   * @param restartStrategy
   *          the strategy to be used
   */
  void setRestartStrategy(RestartStrategy restartStrategy);

  /**
   * Get the restart strategy for the runner.
   *
   * @return the strategy to be used
   */
  RestartStrategy getRestartStrategy();

  /**
   * Set how long to attempt a restart.
   *
   * @param restartDurationMaximum
   *          the restart attempt duration in milliseconds
   */
  void setRestartDurationMaximum(long restartDurationMaximum);
}
