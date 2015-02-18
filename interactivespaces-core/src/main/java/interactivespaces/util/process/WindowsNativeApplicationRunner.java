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

import interactivespaces.system.InteractiveSpacesEnvironment;

import org.apache.commons.logging.Log;

/**
 * A {@link NativeApplicationRunner} for Windows systems.
 *
 * @author Keith M. Hughes
 */
public class WindowsNativeApplicationRunner extends BaseNativeApplicationRunner {

  /**
   * Tag this launcher identifies itself with.
   */
  public static final String OPERATING_SYSTEM_TAG = "windows";

  /**
   * Construct a new runner.
   *
   * @param runnerParser
   *          the runner parser
   * @param spaceEnvironment
   *          the space environment for the runner
   * @param log
   *          the logger to use
   */
  public WindowsNativeApplicationRunner(NativeApplicationRunnerParser runnerParser,
      InteractiveSpacesEnvironment spaceEnvironment, Log log) {
    super(runnerParser, spaceEnvironment, log);
  }

  @Override
  public boolean handleProcessExit(int exitValue, String[] command) {
    getLog().info(String.format("Return value from process is %s for %s", UnixReturnValue.get(exitValue), command[0]));

    return true;
  }
}
