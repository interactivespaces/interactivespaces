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

package interactivespaces.activity.binary;

import interactivespaces.util.process.NativeApplicationRunner;
import interactivespaces.util.process.restart.RestartStrategy;

/**
 * A runner for native web browsers so that they can be controlled by Interactive Spaces.
 *
 * @author Keith M. Hughes
 */
public interface NativeWebBrowserRunner {

  /**
   * Start up the browser.
   *
   * @param initialUrl
   *          URL the browser should start on
   * @param debug
   *          {@code true} if the browser should have a URL bar, etc, to make it easier to debug
   */
  void startup(String initialUrl, boolean debug);

  /**
   * Get the default restart strategy for the browser.
   *
   * @return the restart strategy for the browser
   */
  RestartStrategy<NativeApplicationRunner> getDefaultRestartStrategy();

  /**
   * Shut the browser down.
   */
  void shutdown();

  /**
   * Is the browser still running?
   *
   * @return {@code true} if the browser is running
   */
  boolean isRunning();

  /**
   * Get the native application runner for the browser.
   *
   * @return the native application runner for the browser
   */
  NativeApplicationRunner getNativeWebBrowserRunner();
}
