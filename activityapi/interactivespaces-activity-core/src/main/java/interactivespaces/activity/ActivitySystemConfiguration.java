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
import interactivespaces.system.core.configuration.CoreConfiguration;

/**
 * A collection of property names used in the Interactive Spaces system configuration specifically for activities to
 * use.
 *
 * @author Keith M. Hughes
 */
public final class ActivitySystemConfiguration {

  /**
   * Prefix of the name of the configuration property for a native platform browser to use.
   *
   * Will have the OS added to the end separated by a dot.
   */
  public static final String CONFIGURATION_PREFIX_ACTIVITY_NATIVE_BROWSER_BINARY =
      "interactivespaces.activity.external.native.browser.binary";

  /**
   * Prefix of the name of the configuration property for flags to be handed to the native platform browser to start up
   * a browser app. Uses {@link java.text.MessageFormat} to format, {@code {0}} is the URL to open to start.
   */
  public static final String CONFIGURATION_PREFIX_ACTIVITY_NATIVE_BROWSER_COMMAND_FLAGS =
      "interactivespaces.activity.external.native.browser.command.flags";

  /**
   * Prefix of the name of the configuration property for environment variables to be handed to the native platform
   * browser to start up a browser app.
   *
   * <p>
   * The value of the config parameter should be a set of key value pairs, e.g.
   *
   * <pre>
   * var1=val1 var2=val2
   * </pre>
   *
   * <p>
   * values can be removed from the environment by not giving a value, e.g.
   *
   * <pre>
   * var1=val1 var2 var3=val3
   * </pre>
   *
   * <p>
   * will set {@code var1} to {@code val1}, {@code var2} to {@code val2}, and remove {@code var2}.
   */
  public static final String CONFIGURATION_PREFIX_ACTIVITY_NATIVE_BROWSER_COMMAND_ENVIRONMENT =
      "interactivespaces.activity.external.native.browser.command.environment";

  /**
   * Get the native browser to use according to the activity configuration.
   *
   * @param configuration
   *          the activity configuration
   *
   * @return the full path to the binary to use for the browser
   */
  public static String getActivityNativeBrowserBinary(Configuration configuration) {
    String os = configuration.getRequiredPropertyString(CoreConfiguration.CONFIGURATION_INTERACTIVESPACES_PLATFORM_OS);

    return configuration.getRequiredPropertyString(CONFIGURATION_PREFIX_ACTIVITY_NATIVE_BROWSER_BINARY + "." + os);
  }

  /**
   * Get the native browser command flags to use according to the activity configuration.
   *
   * @param configuration
   *          the activity configuration
   * @param debug
   *          {@code true} if this should be the debug flags
   *
   * @return the full command flag portion of the browser command, will have the URL to begin with as {@code 0}
   */
  public static String getActivityNativeBrowserCommandFlags(Configuration configuration, boolean debug) {

    String simpleVersion = configuration.getPropertyString(CONFIGURATION_PREFIX_ACTIVITY_NATIVE_BROWSER_COMMAND_FLAGS);
    if (simpleVersion != null) {
      return simpleVersion;
    }

    String os = configuration.getRequiredPropertyString(CoreConfiguration.CONFIGURATION_INTERACTIVESPACES_PLATFORM_OS);
    if (debug) {
      String debugVersion =
          configuration.getPropertyString(CONFIGURATION_PREFIX_ACTIVITY_NATIVE_BROWSER_COMMAND_FLAGS + "." + "debug"
              + "." + os);
      if (debugVersion != null) {
        return debugVersion;
      }
    }

    return configuration.getRequiredPropertyString(CONFIGURATION_PREFIX_ACTIVITY_NATIVE_BROWSER_COMMAND_FLAGS + "."
        + os);
  }

  /**
   * Get the native browser command environment to use according to the activity configuration.
   *
   * @param configuration
   *          the activity configuration
   *
   * @return the full command environment portion of the browser command, can be {@code null}
   */
  public static String getActivityNativeBrowserCommandEnvironment(Configuration configuration) {

    String simpleVersion =
        configuration.getPropertyString(CONFIGURATION_PREFIX_ACTIVITY_NATIVE_BROWSER_COMMAND_ENVIRONMENT);
    if (simpleVersion != null) {
      return simpleVersion;
    }

    String os = configuration.getRequiredPropertyString(CoreConfiguration.CONFIGURATION_INTERACTIVESPACES_PLATFORM_OS);

    return configuration.getPropertyString(CONFIGURATION_PREFIX_ACTIVITY_NATIVE_BROWSER_COMMAND_ENVIRONMENT + "." + os);
  }

  /**
   * Required for lint check.
   */
  private ActivitySystemConfiguration() {
  }
}
