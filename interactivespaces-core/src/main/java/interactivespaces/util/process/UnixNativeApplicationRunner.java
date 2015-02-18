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
 * A base {@link NativeApplicationRunner} for unix-based operating systems.
 *
 * @author Keith M. Hughes
 */
public abstract class UnixNativeApplicationRunner extends BaseNativeApplicationRunner {

  /**
   * Create a new activity runner for a unix-based operating system.
   *
   * @param runnerParser
   *          the runner parser
   * @param spaceEnvironment
   *          environment to use
   * @param log
   *          logger for logging
   */
  public UnixNativeApplicationRunner(NativeApplicationRunnerParser runnerParser,
      InteractiveSpacesEnvironment spaceEnvironment, Log log) {
    super(runnerParser, spaceEnvironment, log);
  }

  @Override
  public boolean handleProcessExit(int exitValue, String[] commands) {
    String returnValue = null;
    try {
      UnixReturnValue unixReturnValue = UnixReturnValue.get(exitValue);
      if (unixReturnValue != null) {
        returnValue = unixReturnValue.toString();

        if (unixReturnValue == UnixReturnValue.EXIT_NORMALLY) {
          return true;
        }
      } else {
        returnValue = Integer.toString(exitValue);
      }

      return false;
    } finally {
      getLog().info(String.format("Return value from process is %s for %s", returnValue, commands[0]));
    }
  }
}
