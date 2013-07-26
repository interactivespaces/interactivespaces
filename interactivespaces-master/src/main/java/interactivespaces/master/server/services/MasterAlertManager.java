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

package interactivespaces.master.server.services;

/**
 * An alert manager for the master.
 *
 * <p>
 * This manager will watch events happening in the master and decide whether or
 * not to alert someone or something of the event, or lack thereof (e.g. a
 * controller hasn't sent a heartbeat in a while).
 *
 * @author Keith M. Hughes
 */
public interface MasterAlertManager {

  /**
   * Alert type for a controller timeout.
   */
  public static final String ALERT_TYPE_CONTROLLER_TIMEOUT = "controller.timeout";

  /**
   * Start the alert manager up.
   */
  void startup();

  /**
   * Shut the alert manager up.
   */
  void shutdown();
}
