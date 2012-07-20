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

package interactivespaces.controller.repository.internal.pojo;

import interactivespaces.controller.domain.InstalledLiveActivity;
import interactivespaces.controller.domain.pojo.SimpleInstalledLiveActivity;
import interactivespaces.controller.repository.LocalSpaceControllerRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An in-memory version of the controller repository.
 * 
 * <p>
 * Nothing will be fully persisted.
 *
 * @author Keith M. Hughes
 */
public class InMemoryLocalSpaceControllerRepository implements LocalSpaceControllerRepository {

	/**
	 * Map of UUID to activity.
	 */
	private Map<String, InstalledLiveActivity> activitiesByUuid = new HashMap<String, InstalledLiveActivity>();
	
	@Override
	public void startup() {
		// Nothing to do
	}

	@Override
	public void shutdown() {
		// Nothing to do
	}

	@Override
	public InstalledLiveActivity newInstalledLiveActivity() {
		return new SimpleInstalledLiveActivity();
	}

	@Override
	public List<InstalledLiveActivity> getAllInstalledLiveActivities() {
		synchronized (activitiesByUuid) {
			return new ArrayList<InstalledLiveActivity>(activitiesByUuid.values());
		}
	}

	@Override
	public InstalledLiveActivity getInstalledLiveActivityByUuid(
			String uuid) {
		synchronized (activitiesByUuid) {
			return activitiesByUuid.get(uuid);
		}
	}

	@Override
	public InstalledLiveActivity saveInstalledLiveActivity(
			InstalledLiveActivity activity) {
		synchronized (activitiesByUuid) {
			activitiesByUuid.put(activity.getUuid(), activity);
		}

		return activity;
	}

	@Override
	public void deleteInstalledLiveActivity(
			InstalledLiveActivity activity) {
		synchronized (activitiesByUuid) {
			activitiesByUuid.remove(activity.getUuid());
		}
	}
}
