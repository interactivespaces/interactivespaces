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

package interactivespaces.service.alert.internal;

import interactivespaces.service.alert.AlertNotifier;
import interactivespaces.service.alert.AlertService;
import interactivespaces.system.InteractiveSpacesEnvironment;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A basic implementation of an {@link AlertService}.
 * 
 * @author Keith M. Hughes
 */
public class BasicAlertService implements AlertService {

	/**
	 * All alert notifiers for the service.
	 */
	private List<AlertNotifier> alertNotifiers = new CopyOnWriteArrayList<AlertNotifier>();

	/**
	 * Interactive Spaces environment for scripting engine.
	 */
	private InteractiveSpacesEnvironment spaceEnvironment;

	@Override
	public void startup() {
		spaceEnvironment.getLog().info("Alert manager started");
	}

	@Override
	public void shutdown() {
		// Nothing to do
	}

	@Override
	public void raiseAlert(String alertType, String id, String message) {
		spaceEnvironment.getLog().error("Alert being raised.");

		for (AlertNotifier notifier : alertNotifiers) {
			try {
				notifier.notify(alertType, id, message);
			} catch (Exception e) {
				spaceEnvironment.getLog().error("Error notifying about alert", e);
			}
		}
	}

	@Override
	public void registerAlertNotifier(AlertNotifier notifier) {
		spaceEnvironment.getLog().info("Alert notifier registered with alert manager");
		alertNotifiers.add(notifier);
	}

	@Override
	public void unregisterAlertNotifier(AlertNotifier notifier) {
		alertNotifiers.remove(notifier);
	}

	/**
	 * @param spaceEnvironment
	 *            the spaceEnvironment to set
	 */
	public void setSpaceEnvironment(
			InteractiveSpacesEnvironment spaceEnvironment) {
		this.spaceEnvironment = spaceEnvironment;
	}
}
