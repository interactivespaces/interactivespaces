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

package interactivespaces.domain.space.pojo;

import interactivespaces.domain.basic.LiveActivityGroup;
import interactivespaces.domain.pojo.SimpleObject;
import interactivespaces.domain.space.Space;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * A POJO implementation of a {@link Space}.
 * 
 * @author Keith M. Hughes
 */
public class SimpleSpace extends SimpleObject implements Space {
	/**
	 * For serialization.
	 */
	private static final long serialVersionUID = 3369646895663222791L;

	/**
	 * Name of this space.
	 */
	private String name;

	/**
	 * Description of the space.
	 */
	private String description;
	
	/**
	 * The meta data for this space.
	 */
	private Map<String, Object> metadata = Maps.newHashMap();

	/**
	 * The child spaces of this space.
	 */
	private List<Space> spaces = Lists.newArrayList();

	/**
	 * The activity groups in this space.
	 */
	private List<LiveActivityGroup> activityGroups = Lists.newArrayList();;

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public void setMetadata(Map<String, Object> metadata) {
		this.metadata = metadata;
	}

	@Override
	public Map<String, Object> getMetadata() {
		return metadata;
	}

	@Override
	public List<Space> getSpaces() {
		synchronized (spaces) {
			return new ArrayList<Space>(spaces);
		}
	}

	@Override
	public Space addSpace(Space space) {
		synchronized (spaces) {
			spaces.add(space);
		}
		
		return this;
	}

	@Override
	public Space addSpaces(Space... spaces) {
		synchronized (spaces) {
			for (Space space : spaces) {
				addSpace(space);
			}
		}
		
		return this;
	}

	@Override
	public void removeSpace(Space space) {
		synchronized (spaces) {
			spaces.remove(space);
		}
	}

	@Override
	public void clearSpaces() {
		synchronized (spaces) {
			spaces.clear();
		}
	}

	@Override
	public List<LiveActivityGroup> getActivityGroups() {
		synchronized (activityGroups) {
			return new ArrayList<LiveActivityGroup>(activityGroups);
		}
	}

	@Override
	public Space addActivityGroup(LiveActivityGroup activityGroup) {
		synchronized (activityGroups) {
			activityGroups.add(activityGroup);
		}
		
		return this;
	}

	@Override
	public Space addActivityGroups(LiveActivityGroup... activityGroups) {
		synchronized (activityGroups) {
			for (LiveActivityGroup activityGroup : activityGroups) {
				this.activityGroups.add(activityGroup);
			}
		}
		
		return this;
	}

	@Override
	public void removeActivityGroup(LiveActivityGroup activityGroup) {
		synchronized (activityGroups) {
			activityGroups.remove(activityGroup);
		}
	}

	@Override
	public void clearActivityGroups() {
		synchronized (activityGroups) {
			activityGroups.clear();
		}
	}
}
