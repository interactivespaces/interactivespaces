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

package interactivespaces.master.ui.internal.web.space;

import interactivespaces.domain.basic.LiveActivityGroup;
import interactivespaces.domain.space.Space;
import interactivespaces.domain.space.pojo.SimpleSpace;
import interactivespaces.master.server.services.ActivityRepository;
import interactivespaces.master.ui.internal.web.UiUtilities;

import java.io.Serializable;
import java.util.List;

import com.google.common.collect.Lists;

/**
 * A simple form for spaces.
 * 
 * @author Keith M. Hughes
 */
public class SpaceForm implements Serializable {

	/**
	 * For serialization.
	 */
	private static final long serialVersionUID = 1804873238105388587L;

	/**
	 * The space data.
	 */
	private SimpleSpace space = new SimpleSpace();

	/**
	 * The IDs for spaces.
	 */
	private List<String> spaceIds;

	/**
	 * The IDs for live activity groups.
	 */
	private List<String> liveActivityGroupIds;

	/**
	 * @return the space
	 */
	public SimpleSpace getSpace() {
		return space;
	}

	/**
	 * @param space
	 *            the space to set
	 */
	public void setSpace(SimpleSpace space) {
		this.space = space;
	}

	/**
	 * Get the IDs of the direct space children.
	 * 
	 * @return the spaceIds
	 */
	public List<String> getSpaceIds() {
		return spaceIds;
	}

	/**
	 * Set the IDs of the direct space children.
	 * 
	 * @param spaceIds
	 *            the space IDs to set
	 */
	public void setSpaceIds(List<String> spaceIds) {
		this.spaceIds = spaceIds;
	}

	/**
	 * Get the IDs of the live activity groups.
	 * 
	 * @return the live activity group IDs
	 */
	public List<String> getLiveActivityGroupIds() {
		return liveActivityGroupIds;
	}

	/**
	 * Set the IDs of the live activity groups.
	 * 
	 * @param liveActivityGroupIds
	 *            the live activity group IDs to set
	 */
	public void setLiveActivityGroupIds(List<String> liveActivityGroupIds) {
		this.liveActivityGroupIds = liveActivityGroupIds;
	}

	/**
	 * Copy the contents of the form into the supplied space
	 * 
	 * @param destinationSpace
	 *            the space that the contents should be copied into
	 * @param spaceRepository
	 *            repository for space entities
	 * @param activityRepository
	 *            repository for activity entities
	 */
	public void saveSpace(Space destinationSpace,
			ActivityRepository activityRepository) {
		destinationSpace.setName(space.getName());
		destinationSpace.setDescription(space.getDescription());

		destinationSpace.clearSpaces();
		if (spaceIds != null
				&& !spaceIds.contains(UiUtilities.MULTIPLE_SELECT_NONE)) {
			for (String spaceId : spaceIds) {
				Space space = activityRepository.getSpaceById(spaceId);
				if (space != null) {
					destinationSpace.addSpace(space);
				}
			}
		}

		destinationSpace.clearActivityGroups();
		if (liveActivityGroupIds != null
				&& !liveActivityGroupIds
						.contains(UiUtilities.MULTIPLE_SELECT_NONE)) {
			for (String groupId : liveActivityGroupIds) {
				LiveActivityGroup group = activityRepository
						.getLiveActivityGroupById(groupId);
				if (group != null) {
					destinationSpace.addActivityGroup(group);
				}
			}
		}
	}

	/**
	 * Copy the contents of the supplied group into the form
	 * 
	 * @param sourceroup
	 *            the live activity group that the contents should be copied
	 *            from
	 * @param activityRepository
	 *            repository for activity entities
	 */
	public void copySpace(Space sourceSpace) {
		space.setName(sourceSpace.getName());
		space.setDescription(sourceSpace.getDescription());

		spaceIds = Lists.newArrayList();
		for (Space subspace : sourceSpace.getSpaces()) {
			spaceIds.add(subspace.getId());
		}

		liveActivityGroupIds = Lists.newArrayList();
		for (LiveActivityGroup group : sourceSpace.getActivityGroups()) {
			liveActivityGroupIds.add(group.getId());
		}
	}
}
