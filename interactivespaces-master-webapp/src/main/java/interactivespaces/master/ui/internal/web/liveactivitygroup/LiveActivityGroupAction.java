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

package interactivespaces.master.ui.internal.web.liveactivitygroup;

import interactivespaces.domain.basic.LiveActivity;
import interactivespaces.domain.basic.LiveActivityGroup;
import interactivespaces.master.server.services.ActivityRepository;
import interactivespaces.master.ui.internal.web.WebSupport;

import org.springframework.webflow.execution.RequestContext;

/**
 * WebFlow support for working with a {@link LiveActivity}.
 * 
 * @author Keith M. Hughes
 */
public class LiveActivityGroupAction {
	
	/**
	 * Repository for activities.
	 */
	private ActivityRepository activityRepository;

	/**
	 * Get a new activity group form.
	 * 
	 * @return
	 */
	public LiveActivityGroupForm newLiveActivityGroup() {
		return new LiveActivityGroupForm();
	}

	/**
	 * Add entities to the flow context needed by the new entity page.
	 * 
	 * @param context
	 *            The Webflow context.
	 */
	public void addLiveActivityGroupEntities(RequestContext context) {
		context.getViewScope().put(
				"liveactivities",
				WebSupport.getLiveActivitySelections(activityRepository
						.getAllLiveActivities()));
	}

	/**
	 * Save the new group.
	 * 
	 * @param form
	 *            the live activity group form
	 */
	public void saveLiveActivityGroup(LiveActivityGroupForm form) {
		LiveActivityGroup finalGroup = activityRepository.newLiveActivityGroup();

		form.saveLiveActivityGroup(finalGroup, activityRepository);

		activityRepository.saveLiveActivityGroup(finalGroup);

		// So the ID gets copied out of the flow.
		form.getLiveActivityGroup().setId(finalGroup.getId());
	}

	/**
	 * @param activityRepository
	 *            the activityRepository to set
	 */
	public void setActivityRepository(ActivityRepository activityRepository) {
		this.activityRepository = activityRepository;
	}

}
