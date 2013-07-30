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

import interactivespaces.InteractiveSpacesException;
import interactivespaces.system.InteractiveSpacesEnvironment;

import org.apache.commons.logging.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

/**
 * A {@link NativeActivityRunner} for Windows systems.
 *
 * @author Keith M. Hughes
 */
public class WindowsNativeActivityRunner extends BaseNativeActivityRunner {

  /**
   * Tag this launcher identifies itself with.
   */
  public static final String OPERATING_SYSTEM_TAG = "windows";

  /**
   * Name of the application to run
   */
  private String appName;

  public WindowsNativeActivityRunner(InteractiveSpacesEnvironment spaceEnvironment, Log log) {
    super(spaceEnvironment, log);
  }

  @Override
  public String[] getCommand() {
    List<String> builder = new ArrayList<String>();

    appName = (String) config.get(ACTIVITYNAME);
    if (appName != null) {
      builder.add(appName);

      String commandFlags = (String) config.get(FLAGS);
      for (String arg : commandFlags.split("\\s")) {
        builder.add(arg);
      }

      // Build up the command line.
      for (Entry<String, Object> entry : config.entrySet()) {
        if (ACTIVITYNAME.equals(entry.getKey()) || FLAGS.equals(entry.getKey()))
          continue;

        String arg = " --" + entry.getKey();
        Object value = entry.getValue();
        if (value != null)
          arg += "=" + value.toString();

        builder.add(arg);
      }

      return builder.toArray(new String[builder.size()]);
    } else {
      throw new InteractiveSpacesException("No property called " + ACTIVITYNAME);
    }
  }

  @Override
  public boolean handleProcessExit(int exitValue, String[] command) {
    log.info(String.format("Return value from process is %s for %s",
        UnixReturnValue.get(exitValue), command[0]));

    return true;
  }
}
