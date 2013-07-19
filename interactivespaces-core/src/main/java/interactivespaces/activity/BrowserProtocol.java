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

import java.util.HashMap;
import java.util.Map;

/**
 * Protocol information to send to a browser.
 *
 * @author Keith M. Hughes
 */
public class BrowserProtocol {

  /**
   * The type of command being sent to the browser.
   */
  public static final String WEBAPP_COMMAND_TYPE = "command";

  /**
   * The "update configuration" command to be sent to the browser.
   *
   * <p>
   * The key for the property should be {{@link #WEBAPP_COMMAND_TYPE}
   */
  public static final String WEBAPP_COMMAND_CONFIG_UPDATE = "config_update";

  /**
   * The "performance data" command to be sent to the browser.
   *
   * <p>
   * The key for the property should be {{@link #WEBAPP_COMMAND_TYPE}
   */
  public static final String WEBAPP_COMMAND_PERFORMANCE_DATA = "performance_data";

  /**
   * Get a partially instantiated command object.
   *
   * @param command
   *          The command to be sent.
   *
   * @return The partially instantiated command. The {#WEBAPP_COMMAND_TYPE}
   *         property will be set.
   */
  public static Map<String, Object> getCommand(String command) {
    Map<String, Object> comm = new HashMap<String, Object>();

    comm.put(WEBAPP_COMMAND_TYPE, command);

    return comm;
  }
}
