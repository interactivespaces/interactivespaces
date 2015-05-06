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

package interactivespaces.util.process;

import interactivespaces.util.resource.ManagedResource;

import java.util.Map;

/**
 * A collection of native application runners.
 *
 * <p>
 * The collection will periodically sample the application runners to monitor their lifecycles.
 *
 * @author Keith M. Hughes
 */
public interface NativeApplicationRunnerCollection extends ManagedResource {

  /**
   * Create a new application runner with the given description.
   *
   * <p>
   * The runner is added to the collection. The collection will handle the lifecycle of the runner.
   *
   * @param description
   *          the description of the runner
   *
   * @return a new application runner appropriate for the current platform
   */
  NativeApplicationRunner addNativeApplicationRunner(NativeApplicationDescription description);

  /**
   * Create a new application runner with the given config.
   *
   * <p>
   * The runner is added to the collection. The collection will handle the lifecycle of the runner.
   *
   * @param config
   *          the configuration for the runner
   *
   * @return a new application runner appropriate for the current platform
   *
   * @deprecated Use {@link #newNativeApplicationRunner() and use the configuration setters.
   */
  @Deprecated
  NativeApplicationRunner addNativeApplicationRunner(Map<String, Object> config);

  /**
   * Add a native application runner to the collection.
   *
   * <p>
   * The runner should not be started yet. The collection will control the lifecycle of the runner.
   *
   * @param runner
   *          the runner to add to the collection
   */
  void addNativeApplicationRunner(NativeApplicationRunner runner);

  /**
   * Create a new application runner.
   *
   * <p>
   * The collection will not own the runner, it will need to be added.
   *
   * @return a new runner
   */
  NativeApplicationRunner newNativeApplicationRunner();
}
