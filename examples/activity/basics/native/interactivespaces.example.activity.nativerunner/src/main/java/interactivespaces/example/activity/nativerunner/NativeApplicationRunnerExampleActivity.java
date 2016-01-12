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

package interactivespaces.example.activity.nativerunner;

import interactivespaces.activity.impl.BaseActivity;
import interactivespaces.util.process.BaseNativeApplicationRunnerListener;
import interactivespaces.util.process.NativeApplicationRunner;
import interactivespaces.util.process.NativeApplicationRunnerCollection;
import interactivespaces.util.process.StandardNativeApplicationRunnerCollection;
import interactivespaces.util.process.restart.RestartStrategy;

/**
 * An example activity which shows how to use the {@link NativeApplicationRunner}.
 *
 * @author Keith M. Hughes
 */
public class NativeApplicationRunnerExampleActivity extends BaseActivity {

  /**
   * Configuration property giving the location of the activity executable relative to the activity installation
   * directory.
   */
  public static final String CONFIGURATION_APPLICATION_EXECUTABLE = "space.nativeapplication.executable";

  /**
   * Configuration property giving the flags that a native activity would use to launch.
   */
  public static final String CONFIGURATION_APPLICATION_EXECUTABLE_FLAGS = "space.nativeapplication.executable.flags";

  /**
   * The collection of native activity runners for this activity.
   */
  private NativeApplicationRunnerCollection nativeAppRunnerCollection;

  @Override
  public void onActivitySetup() {
    nativeAppRunnerCollection = new StandardNativeApplicationRunnerCollection(getSpaceEnvironment(), getLog());

    addManagedResource(nativeAppRunnerCollection);
  }

  @Override
  public void onActivityActivate() {
    getLog().info("Starting up runner on activate");

    NativeApplicationRunner runner = nativeAppRunnerCollection.newNativeApplicationRunner();

    runner.setExecutablePath(getConfiguration().getRequiredPropertyString(CONFIGURATION_APPLICATION_EXECUTABLE));
    runner.parseCommandArguments(
        getConfiguration().getRequiredPropertyString(CONFIGURATION_APPLICATION_EXECUTABLE_FLAGS));

    // If extending BaseNativeApplicationRunnerListener, it is not necessary to override every
    // method. Just showing this as an example.
    runner.addNativeApplicationRunnerListener(new BaseNativeApplicationRunnerListener() {
      @Override
      public void onNativeApplicationRunnerStarting(NativeApplicationRunner runner) {
        getLog().info("About to start running the native application");
      }

      @Override
      public void onNativeApplicationRunnerRunning(NativeApplicationRunner runner) {
        getLog().info("The native application is running");
      }

      @Override
      public void onNativeApplicationRunnerStartupFailed(NativeApplicationRunner runner) {
        getLog().info("The native application has failed to start");
      }

      @Override
      public boolean onNativeApplicationRunnerShutdownRequested(NativeApplicationRunner runner) {
        getLog().info("Something wants to shut down the native application");

        // This method is not handling the shutdown. If true is returned, this method could tell the
        // application to shut down somehow.
        return false;
      }

      @Override
      public void onNativeApplicationRunnerShutdown(NativeApplicationRunner runner) {
        getLog().info("The native application has shut down");
      }

      @Override
      public boolean onRestartAttempt(RestartStrategy<NativeApplicationRunner> restartStrategy,
          NativeApplicationRunner runner, boolean continueRestart) {
        getLog().info("The native application has crashed and a restart is about to be attempted.");

        // true means that the strategy should attempt a restart. If we should punt on restarting, then return false.
        return true;
      }

      @Override
      public void onRestartSuccess(RestartStrategy<NativeApplicationRunner> restartStrategy,
          NativeApplicationRunner runner) {
        getLog().info("The native application restart is successful. Yeah!");
      }

      @Override
      public void onRestartFailure(RestartStrategy<NativeApplicationRunner> restartStrategy,
          NativeApplicationRunner runner) {
        getLog().info("All atempts of restarting the native application have failed. BUMMER!");
      }
    });

    nativeAppRunnerCollection.addNativeApplicationRunner(runner);
  }
}
