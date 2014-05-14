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
 * A {@link NativeApplicationRunner} for OS-X.
 *
 * @author Keith M. Hughes
 */
public class OsxNativeApplicationRunner extends BaseNativeApplicationRunner {

  /**
   * Tag this launcher identifies itself with.
   */
  public static final String OPERATING_SYSTEM_TAG = "osx";

  /**
   * Construct the runner.
   *
   * @param spaceEnvironment
   *          the space environment to use
   * @param log
   *          the logger to use
   */
  public OsxNativeApplicationRunner(InteractiveSpacesEnvironment spaceEnvironment, Log log) {
    super(spaceEnvironment, log);
  }

  @Override
  public boolean handleProcessExit(int exitValue, String[] commands) {
    String returnValue = null;
    UnixReturnValue unixReturnValue = UnixReturnValue.get(exitValue);
    if (unixReturnValue != null) {
      returnValue = unixReturnValue.toString();
    } else {
      returnValue = Integer.toString(exitValue);
    }
    getLog().info(String.format("Return value from process is %s for %s", returnValue, commands[0]));

    return true;
  }
}
