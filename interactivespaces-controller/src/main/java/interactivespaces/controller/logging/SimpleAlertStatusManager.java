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

package interactivespaces.controller.logging;

import interactivespaces.activity.ActivityStatus;
import interactivespaces.controller.client.node.ActiveControllerActivity;

import org.apache.commons.logging.Log;

/**
 * Use the console to report status.
 * 
 * <p>
 * This should not be used in production. Someone should be emailed, or paged, or
 * something.
 *
 * @author Keith M. Hughes
 */
public class SimpleAlertStatusManager implements AlertStatusManager {
	
	/**
	 * Local log where things should be written.
	 */
	private Log log;
	
	@Override
	public void announceStatus(ActiveControllerActivity activity) {
		// TODO(keith): Stupid for now.
		ActivityStatus status = activity.getActivityStatus();
		log.error(String.format("ALERT: Activity %s has serious issues: %s\n", 
				activity.getInstance().getName(), status.getState().toString()));
		log.error(status.getDescription());
		Exception e = activity.getActivityStatus().getException();
		if (e != null) {
			log.error("Exception involved", e);
		}
	}

	/**
	 * @param log the log to set
	 */
	public void setLog(Log log) {
		this.log = log;
	}
}
