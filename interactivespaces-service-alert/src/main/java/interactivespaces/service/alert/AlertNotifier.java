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

/**
 * Provide notification for an alert.
 * 
 * @author Keith M. Hughes
 */
public interface AlertNotifier {

	/**
	 * Send a notification about the alert.
	 * 
	 * @param alertType
	 *            the type of alert
	 * @param id
	 *            ID of the item alerting
	 * @param message
	 *            message about the alert
	 */
	void notify(String alertType, String id, String message);
}
