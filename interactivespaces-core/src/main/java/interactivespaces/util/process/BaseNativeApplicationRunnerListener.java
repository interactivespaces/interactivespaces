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

import interactivespaces.util.process.restart.RestartStrategy;

/**
 * A {@link NativeApplicationRunnerListener} which provides default
 * implementations for all lifecycle methods which do nothing.
 *
 * @author Keith M. Hughes
 */
public class BaseNativeApplicationRunnerListener implements NativeApplicationRunnerListener {

  @Override
  public void onNativeApplicationRunnerStarting(NativeApplicationRunner runner) {
    // Default is do nothing
  }

  @Override
  public void onNativeApplicationRunnerRunning(NativeApplicationRunner runner) {
    // Default is do nothing
  }

  @Override
  public boolean onNativeApplicationRunnerShutdownRequested(NativeApplicationRunner runner) {
    // Definitely not handling the shutdown
    return false;
  }

  @Override
  public void onNativeApplicationRunnerShutdown(NativeApplicationRunner runner) {
    // Default is do nothing
  }

  @Override
  public void onNativeApplicationRunnerStartupFailed(NativeApplicationRunner runner) {
    // Default is do nothing
  }

  @Override
  public boolean onRestartAttempt(RestartStrategy<NativeApplicationRunner> strategy,
      NativeApplicationRunner restartable, boolean continueRestart) {
    // Not stopping the restart
    return true;
  }

  @Override
  public void onRestartSuccess(RestartStrategy<NativeApplicationRunner> strategy, NativeApplicationRunner restartable) {
    // Default is do nothing
  }

  @Override
  public void onRestartFailure(RestartStrategy<NativeApplicationRunner> strategy, NativeApplicationRunner restartable) {
    // Default is do nothing
  }
}
