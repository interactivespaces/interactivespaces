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

package interactivespaces.controller.android;

import interactivespaces.system.core.configuration.ConfigurationProvider;

import android.content.SharedPreferences;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * An Android {@link ConfigurationProvider}.
 *
 * @author Keith M. Hughes
 */
public class AndroidConfigurationProvider implements ConfigurationProvider {
  public static final String PREF_MASTER_PORT = "PREF_MASTER_PORT";
  public static final String PREF_MASTER_HOST = "PREF_MASTER_HOST";
  public static final String PREF_CONTROLLER_DESCRIPTION = "PREF_CONTROLLER_DESCRIPTION";
  public static final String PREF_CONTROLLER_NAME = "PREF_CONTROLLER_NAME";
  public static final String PREF_HOST_ID = "PREF_HOST_ID";
  public static final String PREF_CONTROLLER_HOST = "PREF_CONTROLLER_HOST";
  public static final String PREF_CONTROLLER_UUID = "PREF_CONTROLLER_UUID";

  /**
   * Value for the controller host property if the host's IP should be used.
   */
  public static final String CONTROLLER_HOST_DEVICE_IP = "$device_ip";

  /**
   * Get the initial properties for the Interactive Spaces boot from the
   * prefererences.
   *
   * @param prefs
   *          the shared preferences from Android
   *
   * @return a map of the shared properties
   *
   * @return RuntimeException a required property was missing
   */
  public static Map<String, String> getInitialProperties(SharedPreferences prefs)
      throws RuntimeException {

    String masterHost = getRequiredPrefValue(prefs, PREF_MASTER_HOST);
    String masterPort = getRequiredPrefValue(prefs, PREF_MASTER_PORT);

    String controllerHost = getRequiredPrefValue(prefs, PREF_CONTROLLER_HOST);
    if (CONTROLLER_HOST_DEVICE_IP.equals(controllerHost)) {
      controllerHost = AndroidInteractiveSpacesEnvironment.getLocalIpAddress();
    }

    String controllerUuid = getRequiredPrefValue(prefs, PREF_CONTROLLER_UUID);

    String controllerName = getRequiredPrefValue(prefs, PREF_CONTROLLER_NAME);

    String controllerDescription = getRequiredPrefValue(prefs, PREF_CONTROLLER_DESCRIPTION);

    String hostId = getRequiredPrefValue(prefs, PREF_HOST_ID);

    Map<String, String> properties = new HashMap<String, String>();

    properties.put("org.ros.master.uri", String.format("http://%s:%s/", masterHost, masterPort));
    properties.put("interactivespaces.host", controllerHost);
    properties.put("interactivespaces.hostid", hostId);
    properties.put("interactivespaces.container.type", "controller");
    properties.put("interactivespaces.network.type", "localdev");
    properties.put("interactivespaces.controller.uuid", controllerUuid);
    properties.put("interactivespaces.controller.name", controllerName);
    properties.put("interactivespaces.controller.description", controllerDescription);

    Log.i(AndroidLoggingProvider.BASE_LOG_NAME, properties.toString());

    return properties;
  }

  /**
   * Get a needed preference value.
   *
   * @param prefs
   *          the preferences for the service
   * @param name
   *          the name of the preference value
   *
   * @return a trimmed version of the value
   *
   * @throws Exception
   *           there was no value found
   */
  private static String getRequiredPrefValue(SharedPreferences prefs, String name)
      throws RuntimeException {
    String value = prefs.getString(name, null);
    if (value != null) {
      value = value.trim();
      if (!value.isEmpty()) {
        return value;
      }
    }

    String message = String.format("No value for required preference %s", name);
    Log.e(AndroidLoggingProvider.BASE_LOG_NAME, message);
    throw new RuntimeException(message);
  }

  /**
   * The shared preferences from the application.
   */
  private SharedPreferences prefs;

  public AndroidConfigurationProvider(SharedPreferences prefs) {
    this.prefs = prefs;
  }

  @Override
  public Map<String, String> getInitialConfiguration() {
    return getInitialProperties(prefs);
  }
}
