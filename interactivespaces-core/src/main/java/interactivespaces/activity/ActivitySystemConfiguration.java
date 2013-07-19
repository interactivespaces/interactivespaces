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

package interactivespaces.activity;

import interactivespaces.configuration.Configuration;
import interactivespaces.configuration.SystemConfiguration;

/**
 * A collection of property names used in the Interactive Spaces system
 * configuration specifically for activities to use.
 *
 * @author Keith M. Hughes
 */
public class ActivitySystemConfiguration {

  /**
   * Prefix of the name of the configuration property for a native platform
   * browser to use.
   *
   * Will have the OS added to the end separated by a dot.
   */
  public static final String ACTIVITY_NATIVE_BROWSER_BINARY_PREFIX =
      "interactivespaces.activity.external.native.browser.binary";

  /**
   * Name of the configuration property for flags to be handed to the native
   * platform browser to start up a browser app. Uses
   * {@link JavaAdapter.text.MessageFormat} to format, {0} is the URL to open to
   * start.
   */
  public static final String ACTIVITY_NATIVE_BROWSER_COMMAND_FLAGS_PREFIX =
      "interactivespaces.activity.external.native.browser.command.flags";

  /**
   * Get the native browser to use according to the activity configuration.
   *
   * @param configuration
   *          The activity configuration
   * @return The full path to the binary to use for the browser.
   */
  public static String getActivityNativeBrowserBinary(Configuration configuration) {
    String os = configuration.getRequiredPropertyString(SystemConfiguration.PLATFORM_OS);

    return configuration
        .getRequiredPropertyString(ACTIVITY_NATIVE_BROWSER_BINARY_PREFIX + "." + os);
  }

  /**
   * Get the native browser command flags to use according to the activity
   * configuration.
   *
   * @param configuration
   *          The activity configuration
   * @param True
   *          if this should be the debug flags, false otherwise.
   * @return The full command flag portion of the browser command. Will have the
   *         URL to begin with as {0}.
   */
  public static String getActivityNativeBrowserCommandFlags(Configuration configuration,
      boolean debug) {
    String os = configuration.getRequiredPropertyString(SystemConfiguration.PLATFORM_OS);

    if (debug) {
      String debugVersion =
          configuration.getPropertyString(ACTIVITY_NATIVE_BROWSER_COMMAND_FLAGS_PREFIX + "."
              + "debug" + "." + os);
      if (debugVersion != null) {
        return debugVersion;
      }
    }

    return configuration.getRequiredPropertyString(ACTIVITY_NATIVE_BROWSER_COMMAND_FLAGS_PREFIX
        + "." + os);
  }
}
