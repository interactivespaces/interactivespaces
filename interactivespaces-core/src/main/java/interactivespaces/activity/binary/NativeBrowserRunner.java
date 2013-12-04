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

package interactivespaces.activity.binary;

import interactivespaces.activity.Activity;
import interactivespaces.activity.ActivitySystemConfiguration;
import interactivespaces.configuration.Configuration;
import interactivespaces.util.process.restart.LimitedRetryRestartStrategy;

import com.google.common.collect.Maps;

import java.text.MessageFormat;
import java.util.Map;

/**
 * A runner for native browsers.
 *
 * @author Keith M. Hughes
 */
public class NativeBrowserRunner {

  /**
   * The number of times the restart strategy will attempt a restart before
   * giving up.
   */
  public static final int RESTART_STRATEGY_NUMBER_RETRIES = 4;

  /**
   * How long the browser must be running before restart is considered
   * successful in milliseconds.
   */
  public static final int RESTART_STRATEGY_SUCCESS_TIME = 4000;

  /**
   * How often the restart will sample to see if restart has happened, in
   * milliseconds.
   */
  public static final int RESTART_STRATEGY_SAMPLE_TIME = 1000;

  /**
   * Activity this browser is running under.
   */
  private final Activity activity;

  /**
   * Launcher for the browser.
   */
  private NativeActivityRunner browserRunner;

  /**
   * Construct a new browser runner.
   *
   * @param activity
   *          the activity the browser is running
   */
  public NativeBrowserRunner(Activity activity) {
    this.activity = activity;
  }

  /**
   * Start up the browser.
   *
   * @param initialUrl
   *          URL the browser should start on.
   *
   * @param debug
   *          True if the browser should have a URL bar, etc, to make it easier
   *          to debug.
   */
  public void startup(String initialUrl, boolean debug) {
    browserRunner =
        activity.getController().getNativeActivityRunnerFactory().newPlatformNativeActivityRunner(activity.getLog());

    Configuration configuration = activity.getConfiguration();

    Map<String, Object> appConfig = Maps.newHashMap();
    appConfig.put(NativeActivityRunner.ACTIVITYNAME,
        ActivitySystemConfiguration.getActivityNativeBrowserBinary(configuration));

    String commandFlags =
        MessageFormat.format(ActivitySystemConfiguration.getActivityNativeBrowserCommandFlags(configuration, debug),
            initialUrl);

    appConfig.put(NativeActivityRunner.FLAGS, commandFlags);

    browserRunner.configure(appConfig);
    browserRunner.setRestartStrategy(getDefaultRestartStrategy());

    browserRunner.startup();
  }

  /**
   * Get the default restart strategy for the browser.
   *
   * @return the restart strategy for the browser
   */
  public LimitedRetryRestartStrategy getDefaultRestartStrategy() {
    return new LimitedRetryRestartStrategy(RESTART_STRATEGY_NUMBER_RETRIES, RESTART_STRATEGY_SAMPLE_TIME,
        RESTART_STRATEGY_SUCCESS_TIME, activity.getSpaceEnvironment());
  }

  /**
   * Shut the browser down.
   */
  public void shutdown() {
    if (browserRunner != null) {
      browserRunner.shutdown();

      browserRunner = null;
    }
  }

  /**
   * Is the browser still running?
   *
   * @return {@code true} if the browser is running
   */
  public boolean isRunning() {
    if (browserRunner != null) {
      return browserRunner.isRunning();
    } else {
      return false;
    }
  }

  /**
   * Get the native activity runner for the browser.
   *
   * @return the native activity runner for the browser
   */
  public NativeActivityRunner getBrowserRunner() {
    return browserRunner;
  }
}
