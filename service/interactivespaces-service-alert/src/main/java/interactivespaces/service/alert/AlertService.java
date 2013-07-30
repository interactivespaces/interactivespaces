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

package interactivespaces.service.alert;

import interactivespaces.service.SupportedService;

/**
 * Execute scripts in Interactive Spaces.
 *
 * @author Keith M. Hughes
 */
public interface AlertService extends SupportedService {

  /**
   * The name of the service.
   */
  public static final String SERVICE_NAME = "alert";

  /**
   * Raise an alert
   *
   * @param alertType
   *          the type of alert
   * @param id
   *          ID of the entity which created the alert
   * @param message
   *          a message about what the alert is
   */
  void raiseAlert(String alertType, String id, String message);

  /**
   * Register a new alert notifier with the service.
   *
   * @param notifier
   *          the notifier to register
   */
  void registerAlertNotifier(AlertNotifier notifier);

  /**
   * Unregister a new alert notifier with the service.
   *
   * <p>
   * Does nothing if the notifier wasn't registered.
   *
   * @param notifier
   *          the notifier to unregister
   */
  void unregisterAlertNotifier(AlertNotifier notifier);
}
