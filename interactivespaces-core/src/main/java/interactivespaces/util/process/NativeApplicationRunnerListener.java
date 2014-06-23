/*
 * Copyright (C) 2014 Google Inc.
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

import interactivespaces.util.process.restart.RestartStrategyListener;

/**
 * A listener for lifecycle events on a {@link NativeApplicationRunner}.
 *
 * @author Keith M. Hughes
 */
public interface NativeApplicationRunnerListener extends RestartStrategyListener<NativeApplicationRunner> {

  /**
   * The native application runner is starting.
   *
   * <p>
   * This event is before the binary is launched.
   *
   * @param runner
   *          the runner
   */
  void onNativeApplicationRunnerStarting(NativeApplicationRunner runner);

  /**
   * The native application runner has started.
   *
   * <p>
   * This event is immediately after the binary has successfully launched.
   *
   * @param runner
   *          the runner
   */
  void onNativeApplicationRunnerRunning(NativeApplicationRunner runner);

  /**
   * The shutdown method has been called on the native application runner.
   *
   * <p>
   * This can be used, for example, to try and shutdown the native application
   * cleanly by sending it a signal or something.
   *
   * @param runner
   *          the runner
   *
   * @return {@code true} if actually performing the shutdown operation, which
   *         means the runner won't try and shut the application down
   */
  boolean onNativeApplicationRunnerShutdownRequested(NativeApplicationRunner runner);

  /**
   * The native application runner has stopped running.
   *
   * <p>
   * This is immediately after the native process has completed.
   *
   * @param runner
   *          the runner
   */
  void onNativeApplicationRunnerShutdown(NativeApplicationRunner runner);

  /**
   * The native application runner refused to even start.
   *
   * @param runner
   *          the runner
   */
  void onNativeApplicationRunnerStartupFailed(NativeApplicationRunner runner);
}
