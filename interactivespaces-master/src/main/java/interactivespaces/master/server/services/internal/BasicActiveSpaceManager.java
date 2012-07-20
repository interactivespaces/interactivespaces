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

package interactivespaces.master.server.services.internal;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.domain.basic.LiveActivityGroup;
import interactivespaces.domain.space.Space;
import interactivespaces.master.server.services.ActiveLiveActivity;
import interactivespaces.master.server.services.ActiveSpace;
import interactivespaces.master.server.services.ActiveSpaceManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;

/**
 * An {@link ActiveSpaceManager}.
 * 
 * @author Keith M. Hughes
 */
public class BasicActiveSpaceManager implements ActiveSpaceManager {

	// TODO(keith): This class needs to be refactored.

	/**
	 * The spaces being managed.
	 */
	private Map<String, ActiveSpace> activeSpaces = new HashMap<String, ActiveSpace>();

	/**
	 * The controller manager the space manager uses for controlling activities.
	 */
	private InternalActiveControllerManager activeControllerManager;

	/**
	 * Get the active space associated with a give space.
	 * 
	 * @param space
	 *            The space the active space is wanted for.
	 * 
	 * @return The active space for the space.
	 * 
	 * @throws InteractiveSpacesException
	 *             The space has not been loaded yet.
	 */
	public ActiveSpace getActiveSpace(Space space) {
		synchronized (activeSpaces) {
			ActiveSpace aspace = activeSpaces.get(space.getId());

			if (aspace == null) {
				aspace = new ActiveSpace(space);
				activeSpaces.put(space.getId(), aspace);
			} else {
				aspace.updateSpace(space);
			}

			return aspace;
		}
	}

	@Override
	public void deploySpace(Space space) {
		final Set<ActiveLiveActivity> deployedActivities = Sets.newHashSet();

		SpaceWalker walker = new SpaceWalker() {
			@Override
			protected void doVisit(Space space) {
				for (LiveActivityGroup activityGroup : space
						.getActivityGroups()) {
					activeControllerManager
							.deployActiveLiveActivityGroupChecked(
									activeControllerManager
											.getActiveLiveActivityGroup(activityGroup),
									deployedActivities);
				}
			}
		};

		walker.walk(space);
	}

	@Override
	public void configureSpace(Space space) {
		final Set<ActiveLiveActivity> deployedActivities = Sets.newHashSet();

		SpaceWalker walker = new SpaceWalker() {
			@Override
			protected void doVisit(Space space) {
				for (LiveActivityGroup activityGroup : space
						.getActivityGroups()) {
					activeControllerManager
							.configureActiveLiveActivityGroupChecked(
									activeControllerManager
											.getActiveLiveActivityGroup(activityGroup),
									deployedActivities);
				}
			}
		};

		walker.walk(space);
	}

	@Override
	public void startupSpace(Space space) {
		SpaceWalker walker = new SpaceWalker() {
			@Override
			protected void doVisit(Space aspace) {
				for (LiveActivityGroup activityGroup : aspace
						.getActivityGroups()) {
					activeControllerManager
							.startupActiveActivityGroup(activeControllerManager
									.getActiveLiveActivityGroup(activityGroup));
				}
			}
		};

		walker.walk(space);
	}

	@Override
	public void shutdownSpace(Space space) {
		SpaceWalker walker = new SpaceWalker() {
			@Override
			protected void doVisit(Space space) {
				for (LiveActivityGroup activityGroup : space
						.getActivityGroups()) {
					activeControllerManager
							.shutdownActiveActivityGroup(activeControllerManager
									.getActiveLiveActivityGroup(activityGroup));
				}
			}
		};

		walker.walk(space);
	}

	@Override
	public void activateSpace(Space space) {
		SpaceWalker walker = new SpaceWalker() {
			@Override
			protected void doVisit(Space space) {
				for (LiveActivityGroup activityGroup : space
						.getActivityGroups()) {
					activeControllerManager
							.activateActiveActivityGroup(activeControllerManager
									.getActiveLiveActivityGroup(activityGroup));
				}
			}
		};

		walker.walk(space);
	}

	@Override
	public void deactivateSpace(Space space) {
		SpaceWalker walker = new SpaceWalker() {
			@Override
			protected void doVisit(Space space) {
				for (LiveActivityGroup activityGroup : space
						.getActivityGroups()) {
					activeControllerManager
							.deactivateActiveActivityGroup(activeControllerManager
									.getActiveLiveActivityGroup(activityGroup));
				}
			}
		};

		walker.walk(space);
	}

	/**
	 * @param activeControllerManager
	 *            the activeControllerManager to set
	 */
	public void setActiveControllerManager(
			InternalActiveControllerManager activeControllerManager) {
		this.activeControllerManager = activeControllerManager;
	}
}
