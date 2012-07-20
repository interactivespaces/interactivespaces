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

package interactivespaces.controller.activity.installation;

/**
 * A listener for events from activity installs.
 *
 * @author Keith M. Hughes
 */
public interface ActivityInstallationListener {
	
	/**
	 * An activity has been installed.
	 * 
	 * <p>
	 * This is sent after the activity installation is complete.
	 * 
	 * @param uuid
	 * 		UUID of the installed activity.
	 */
	void onActivityInstall(String uuid);
	
	/**
	 * An activity has been removed.
	 * 
	 * <p>
	 * This is sent after the activity removal is complete.
	 * 
	 * @param uuid
	 * 		UUID of the removed activity.
	 */
	void onActivityRemove(String uuid);
}
